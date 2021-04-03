package net.lambeaux.homework.gr;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import net.lambeaux.homework.gr.core.Record;
import net.lambeaux.homework.gr.persistence.InMemoryDatabase;
import org.apache.commons.lang3.StringUtils;
import org.jline.builtins.Completers;
import org.jline.reader.Completer;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;
import org.jline.reader.impl.completer.AggregateCompleter;
import org.jline.reader.impl.completer.ArgumentCompleter;
import org.jline.reader.impl.completer.NullCompleter;
import org.jline.reader.impl.completer.StringsCompleter;
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

  private static final String EXT_CSV = "csv";

  private static final String EXT_PSV = "psv";

  private static final String EXT_SSV = "ssv";

  private static final String CMD_INGEST = "ingest";

  private static final String CMD_LIST = "list";

  private static final String ARG_OUTPUT_1 = "output1-email-desc-lastname-asc";

  private static final String ARG_OUTPUT_2 = "output2-birthdate-asc";

  private static final String ARG_OUTPUT_3 = "output3-lastname-desc";

  private final Map<String, IngestStrategy> parsers;

  private final Path systemWorkingDir;

  private final InMemoryDatabase db;

  private final LineReader lineReader;

  private final Terminal terminal;

  private static Terminal defaultTerminal() throws IOException {
    return TerminalBuilder.builder().system(true).build();
  }

  private static Completer defaultAutoComplete() {
    return new AggregateCompleter(
        new ArgumentCompleter(
            new StringsCompleter(CMD_LIST),
            new StringsCompleter(ARG_OUTPUT_1, ARG_OUTPUT_2, ARG_OUTPUT_3)),
        new ArgumentCompleter(
            new StringsCompleter(CMD_INGEST),
            new AggregateCompleter(
                new Completers.DirectoriesCompleter(SYS_CURR_WORKING_DIR),
                new Completers.FilesCompleter(SYS_CURR_WORKING_DIR)),
            new NullCompleter()));
  }

  public CommandLine(InMemoryDatabase db) throws IOException {
    this(
        db,
        LineReaderBuilder.builder()
            .terminal(defaultTerminal())
            .completer(defaultAutoComplete())
            .build(),
        SYS_CURR_WORKING_DIR);
  }

  CommandLine(InMemoryDatabase db, LineReader lineReader, Path systemWorkingDir) {
    this.systemWorkingDir = systemWorkingDir;
    this.db = Objects.requireNonNull(db, "database cannot be null");
    this.lineReader = Objects.requireNonNull(lineReader, "line reader cannot be null");
    this.terminal = Objects.requireNonNull(lineReader.getTerminal(), "terminal cannot be null");

    this.parsers = new HashMap<>();
    this.parsers.put(EXT_CSV, new IngestStrategy(","));
    this.parsers.put(EXT_PSV, new IngestStrategy("\\|"));
    this.parsers.put(EXT_SSV, new IngestStrategy(WHITE_SPACE));
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
      ingest(ingestFileToUse).forEach(rec -> db.put(rec.getEmail(), rec));
      terminal
          .writer()
          .println(String.format("Successfully ingested '%s'", ingestFileToUse.toString()));
      return;
    }

    if (CMD_LIST.equals(command.get(0))) {
      validateList(command);
      SortedSet<Record> results = list(command.get(1));
      terminal.writer().println(" ---------------- Listing entries -----------------");
      results.forEach((rec) -> terminal.writer().println(rec.toString()));
      terminal.writer().println(" --------------------------------------------------");
      return;
    }

    terminal.writer().println("Unrecognized command");
  }

  private SortedSet<Record> list(String outputType) {
    SortedSet<Record> results;
    switch (outputType) {
      case ARG_OUTPUT_1:
        results =
            new TreeSet<>(
                Comparator.comparing(Record::getEmail, Comparator.reverseOrder())
                    .thenComparing(Record::getLastName, Comparator.naturalOrder()));
        break;
      case ARG_OUTPUT_2:
        results =
            new TreeSet<>(Comparator.comparing(Record::getDateOfBirth, Comparator.naturalOrder()));
        break;
      case ARG_OUTPUT_3:
        results =
            new TreeSet<>(Comparator.comparing(Record::getLastName, Comparator.reverseOrder()));
        break;
      default:
        throw new IllegalArgumentException(
            String.format(
                "invalid output format, expected %s, %s, or %s",
                ARG_OUTPUT_1, ARG_OUTPUT_2, ARG_OUTPUT_3));
    }
    results.addAll(db.allValues());
    return results;
  }

  private static void validateList(List<String> cmd) {
    assertThat(() -> cmd.size() == 2, "expecting 1 argument for 'list' command");
  }

  private List<Record> ingest(Path filePath) throws IOException {
    File file = Objects.requireNonNull(filePath, "filePath cannot be null").toFile();
    if (!file.exists()) {
      throw new IllegalArgumentException("file " + file.toString() + " must exist");
    }
    if (!file.isFile()) {
      throw new IllegalArgumentException("file " + file.toString() + " must be a file with data");
    }
    String absPath = file.getAbsolutePath();
    String ext = absPath.substring(absPath.lastIndexOf('.') + 1);
    if (!parsers.containsKey(ext)) {
      throw new IllegalArgumentException("file " + file.toString() + " is not a supported format");
    }
    return parsers.get(ext).read(filePath);
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

  /** Can parse input files based upon the provided delimiter. */
  private static class IngestStrategy {

    private final String delimiter;

    public IngestStrategy(String delimiter) {
      if (Objects.requireNonNull(delimiter, "delimiter cannot be null").isEmpty()) {
        throw new IllegalArgumentException("delimiter cannot be empty");
      }
      this.delimiter = delimiter;
    }

    public List<Record> read(Path filePath) throws IOException {
      return Files.lines(filePath)
          .map(line -> line.split(delimiter))
          .map(Record::new)
          .collect(Collectors.toList());
    }
  }
}
