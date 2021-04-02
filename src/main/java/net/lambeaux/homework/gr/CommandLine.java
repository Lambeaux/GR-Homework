package net.lambeaux.homework.gr;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import net.lambeaux.homework.gr.core.Record;
import net.lambeaux.homework.gr.persistence.InMemoryDatabase;
import org.apache.commons.lang3.StringUtils;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles all command line functionality for the app. Provides two options for interaction:
 *
 * <ul>
 *   <li>{@link #loop()}, to start the default CLI loop.
 *   <li>{@link #handleInput(String)}, to manage the loop externally.
 * </ul>
 */
public class CommandLine {

  private static final Logger LOGGER = LoggerFactory.getLogger(CommandLine.class);

  private static final Path SYS_CURR_WORKING_DIR = Paths.get(System.getProperty("user.dir"));

  private static final String WHITE_SPACE = " ";

  private static final String EXT_TXT = "txt";

  private static final String CMD_INGEST = "ingest";

  // for debugging purposes, will be superseded by list commands
  private static final String CMD_SHOW = "show";

  private final Path systemWorkingDir;

  private final InMemoryDatabase db;

  private final LineReader lineReader;

  private final Terminal terminal;

  public CommandLine(InMemoryDatabase db) throws IOException {
    this(
        db,
        LineReaderBuilder.builder()
            .terminal(TerminalBuilder.builder().system(true).build())
            .build(),
        SYS_CURR_WORKING_DIR);
  }

  CommandLine(InMemoryDatabase db, LineReader lineReader, Path systemWorkingDir) {
    this.systemWorkingDir = systemWorkingDir;
    this.db = Objects.requireNonNull(db, "database cannot be null");
    this.lineReader = Objects.requireNonNull(lineReader, "line reader cannot be null");
    this.terminal = Objects.requireNonNull(lineReader.getTerminal(), "terminal cannot be null");
  }

  /**
   * Begins execution of the CLI loop.
   *
   * @throws IOException if a cli-related error occurs.
   */
  public void loop() throws IOException {
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
        handleInput(line);
      } catch (RuntimeException e) {
        LOGGER.debug("Exception on cmd thread", e);
        terminal.writer().println(StringUtils.capitalize(e.getMessage()));
      }
    }
  }

  /**
   * Process the input string meant for the command line.
   *
   * @param line cli invocation.
   * @throws IOException if an error occurs processing console input.
   * @throws IllegalArgumentException if bad input was provided via cli.
   */
  public void handleInput(String line) throws IOException {
    List<String> command =
        Arrays.stream(line.split(WHITE_SPACE))
            .map(String::trim)
            .filter(str -> !str.isEmpty())
            .collect(Collectors.toList());

    if (command.isEmpty()) {
      LOGGER.debug("Command was empty [{}], no action taken", line);
      return;
    }

    if (CMD_INGEST.equals(command.get(0))) {
      validateIngest(command);
      Path ingestFile = Paths.get(command.get(1));
      Path ingestFileToUse =
          ingestFile.isAbsolute() ? ingestFile : systemWorkingDir.resolve(ingestFile);
      ingest(ingestFileToUse).forEach(rec -> db.put(rec.get("email"), rec));
      terminal
          .writer()
          .println(String.format("Successfully ingested '%s'", ingestFileToUse.toString()));
      return;
    }

    if (CMD_SHOW.equals(command.get(0))) {
      terminal.writer().println(" --------------- All stored records ---------------");
      db.allValues()
          .forEach(
              (entry) -> {
                terminal.writer().println(entry.getKey() + ":");
                entry
                    .getValue()
                    .forEach((key, value) -> terminal.writer().println("  " + key + ": " + value));
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
        .map(Record::create)
        .collect(Collectors.toList());
  }

  private static void validateIngest(List<String> cmd) {
    assertThat(() -> cmd.size() == 2, "expecting 1 argument for 'ingest' command");
    assertThat(() -> noError(() -> Paths.get(cmd.get(1))), "argument must be a valid path");
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

  private static boolean noError(Runnable test) {
    try {
      test.run();
      return true;
    } catch (RuntimeException e) {
      LOGGER.trace("noError test failed, returning false", e);
      LOGGER.debug(e.getMessage());
      return false;
    }
  }
}
