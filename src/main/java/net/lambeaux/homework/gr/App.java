package net.lambeaux.homework.gr;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.javalin.Javalin;
import io.javalin.core.JavalinConfig;
import io.javalin.core.util.RouteOverviewPlugin;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.javalin.http.staticfiles.Location;
import io.javalin.plugin.json.JavalinJson;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Contains all routes and functions to provide the entire REST service. The service consists of:
 *
 * <ul>
 *   <li>A static home page and related static resources.
 *   <li>A page for viewing all registered routes on this service.
 *   <li>A service that echos request information back to the caller as JSON.
 * </ul>
 */
public class App {

  private static final Logger LOGGER = LoggerFactory.getLogger(App.class);

  private static final Gson GSON = new GsonBuilder().create();

  private static final Integer PORT = 8080;

  private static final String SYS_CURR_WORKING_DIR = System.getProperty("user.dir");

  private static final String WHITE_SPACE = " ";

  private static final String EXT_TXT = "txt";

  private static final String CMD_INGEST = "ingest";

  // for debugging purposes, will be superseded by list commands
  private static final String CMD_SHOW = "show";

  public static void main(String[] args) throws Exception {
    LOGGER.info("Initializing dependencies");
    Terminal terminal = TerminalBuilder.builder().system(true).build();
    LineReader lineReader = LineReaderBuilder.builder().terminal(terminal).build();
    Map<String, Map<String, String>> data = new HashMap<>();

    JavalinJson.setFromJsonMapper(GSON::fromJson);
    JavalinJson.setToJsonMapper(GSON::toJson);

    LOGGER.info("Booting up server");
    Javalin app = Javalin.create(App::configureJavalin).start(PORT);

    LOGGER.info("Registering handlers");
    app.get("/extras/request-summary", new RequestSummaryHandler());

    while (true) {
      String line;
      try {
        line = lineReader.readLine("prompt>");
      } catch (UserInterruptException e) {
        LOGGER.info("User interrupted operation");
        System.exit(1);
        break;
      } catch (EndOfFileException e) {
        LOGGER.info("End of file");
        System.exit(1);
        break;
      }

      LOGGER.debug("Received cmd input: {}", line);
      try {
        handleCommandLine(terminal, line, data);
      } catch (RuntimeException e) {
        LOGGER.debug("Exception on cmd thread", e);
        terminal.writer().println(StringUtils.capitalize(e.getMessage()));
      }
    }
  }

  private static void handleCommandLine(
      Terminal terminal, String line, Map<String, Map<String, String>> data) throws IOException {
    List<String> command =
        Arrays.stream(line.split(WHITE_SPACE))
            .map(String::trim)
            .filter(str -> !str.isEmpty())
            .collect(Collectors.toList());

    if (command.isEmpty()) {
      return;
    }

    if (CMD_INGEST.equals(command.get(0))) {
      validateIngest(command);
      Path ingestFile = Paths.get(command.get(1));
      Path ingestFileToUse =
          ingestFile.isAbsolute()
              ? ingestFile
              : Paths.get(SYS_CURR_WORKING_DIR).resolve(ingestFile);
      ingest(ingestFileToUse).forEach(rec -> data.put(rec.get("email"), rec));
      return;
    }

    if (CMD_SHOW.equals(command.get(0))) {
      terminal.writer().println(" --------------- All stored records ---------------");
      data.forEach(
          (email, record) -> {
            terminal.writer().println(email + ":");
            record.forEach((key, value) -> terminal.writer().println("  " + key + ": " + value));
          });
      terminal.writer().println(" --------------------------------------------------");
      return;
    }

    terminal.writer().println("Unrecognized command");
  }

  private static List<Map<String, String>> ingest(Path filePath) throws IOException {
    File file = Objects.requireNonNull(filePath, "filePath cannot be null").toFile();
    if (!file.exists()) {
      throw new IllegalArgumentException("file " + file.toString() + " must exist");
    }
    if (!file.isFile()) {
      throw new IllegalArgumentException("file " + file.toString() + " must be a file with data");
    }
    String absPath = file.getAbsolutePath();
    if (!EXT_TXT.equals(absPath.substring(absPath.lastIndexOf('.') + 1))) {
      throw new IllegalArgumentException("file " + file.toString() + " is not a .txt file");
    }
    return Files.lines(filePath)
        .map(line -> line.split(","))
        .map(App::createEntry)
        .collect(Collectors.toList());
  }

  private static Map<String, String> createEntry(String[] fields) {
    Map<String, String> entry = new HashMap<>();
    entry.put("lastName", Objects.requireNonNull(fields[0]));
    entry.put("firstName", Objects.requireNonNull(fields[1]));
    entry.put("email", Objects.requireNonNull(fields[2]));
    entry.put("favoriteColor", Objects.requireNonNull(fields[3]));
    entry.put("dateOfBirth", Objects.requireNonNull(fields[4]));
    return entry;
  }

  private static void validateIngest(List<String> cmd) {
    assertThat(() -> cmd.size() == 2, "expecting 1 argument for 'ingest' command");
    assertThat(() -> Paths.get(cmd.get(1)).toFile().exists(), "argument must be a valid path");
  }

  private static void assertThat(Supplier<Boolean> cond, String errMsg) {
    boolean check;
    try {
      check = cond.get();
    } catch (RuntimeException e) {
      throw new IllegalArgumentException("error executing command, " + errMsg, e);
    }
    if (!check) {
      throw new IllegalArgumentException("error executing command, " + errMsg);
    }
  }

  private static void configureJavalin(JavalinConfig config) {
    config.registerPlugin(new RouteOverviewPlugin("/extras/routes"));
    config.addStaticFiles("/", "/home", Location.CLASSPATH);
  }

  /**
   * Handles requests for the "request summary" page which just prints request metadata as JSON for
   * every response.
   */
  public static class RequestSummaryHandler implements Handler {
    @Override
    public void handle(@NotNull Context ctx) throws Exception {
      Map<String, String> metadata = new HashMap<>();
      metadata.put("path", ctx.path());
      metadata.put("contextPath", ctx.contextPath());
      metadata.put("endpointPath", ctx.endpointHandlerPath());
      Map<String, Object> response = new HashMap<>();
      response.put("metadata", metadata);
      response.put("queryParams", ctx.queryParamMap());
      response.put("formParams", ctx.formParamMap());
      response.put("headers", ctx.headerMap());
      response.put("cookies", ctx.cookieMap());
      ctx.json(response);
    }
  }
}
