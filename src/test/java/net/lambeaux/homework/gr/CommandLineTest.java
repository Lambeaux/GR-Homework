package net.lambeaux.homework.gr;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import net.lambeaux.homework.gr.persistence.InMemoryDatabase;
import org.jline.reader.LineReader;
import org.jline.terminal.Terminal;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(MockitoJUnitRunner.class)
public class CommandLineTest {

  private static final Logger LOGGER = LoggerFactory.getLogger(CommandLineTest.class);

  private static final String DIR_SAMPLE_DATA = "/sample-data/";

  private static final String FILE_EMPTY_DOT_CSV = "empty.csv";

  private static final String FILE_SAMPLE_CSV_DOT_TXT = "sample-csv.txt";

  @Rule public TemporaryFolder folder = new TemporaryFolder();

  @Mock private InMemoryDatabase mockDb;

  @Mock private LineReader mockLineReader;

  @Mock private Terminal mockTerminal;

  @Mock private PrintWriter mockPrintWriter;

  private CommandLine commandLine;

  @Before
  public void before() throws IOException {
    File dirRoot = folder.getRoot();
    File fileCsv = folder.newFile(FILE_EMPTY_DOT_CSV);
    File fileTxt = folder.newFile(FILE_SAMPLE_CSV_DOT_TXT);

    folder.newFolder("folder-that-exists");

    try (InputStream in =
        CommandLineTest.class.getResourceAsStream(DIR_SAMPLE_DATA + FILE_EMPTY_DOT_CSV)) {
      Files.copy(in, fileCsv.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }

    try (InputStream in =
        CommandLineTest.class.getResourceAsStream(DIR_SAMPLE_DATA + FILE_SAMPLE_CSV_DOT_TXT)) {
      Files.copy(in, fileTxt.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }

    doReturn(mockTerminal).when(mockLineReader).getTerminal();
    doReturn(mockPrintWriter).when(mockTerminal).writer();

    commandLine = new CommandLine(mockDb, mockLineReader, dirRoot.toPath());
  }

  @Test
  public void testEmptyCommand() throws IOException {
    commandLine.handleInput("");
    verify(mockLineReader).getTerminal();
    verifyNoMoreInteractions(mockLineReader);
    verifyZeroInteractions(mockDb, mockTerminal, mockPrintWriter);
  }

  @Test
  public void testIngestCommandRel() throws IOException {
    commandLine.handleInput("ingest sample-csv.txt");
    verify(mockLineReader).getTerminal();
    verify(mockTerminal).writer();
    verify(mockPrintWriter)
        .println(
            eq(
                String.format(
                    "Successfully ingested '%s'",
                    folder.getRoot().toPath().resolve("sample-csv.txt").toString())));
    verifyNoMoreInteractions(mockLineReader, mockTerminal, mockPrintWriter);

    verify(mockDb)
        .put(
            eq("bob.smith@example.net"),
            eq(record("Smith", "Bob", "bob.smith@example.net", "red", "01/23/1972")));
    verify(mockDb)
        .put(
            eq("ted.weaver@example.net"),
            eq(record("Weaver", "Ted", "ted.weaver@example.net", "green", "03/13/1988")));
    verify(mockDb)
        .put(
            eq("redacted@example.net"),
            eq(record("Ames", "Richard", "redacted@example.net", "unknown", "11/01/1923")));

    verifyNoMoreInteractions(mockDb);
  }

  @Test
  public void testIngestCommandAbs() throws IOException {
    String input =
        String.format(
            "ingest %s",
            folder.getRoot().toPath().resolve("sample-csv.txt").toFile().getAbsolutePath());
    LOGGER.info("Running command '{}'", input);
    commandLine.handleInput(input);
    verify(mockLineReader).getTerminal();
    verify(mockTerminal).writer();
    verify(mockPrintWriter)
        .println(
            eq(
                String.format(
                    "Successfully ingested '%s'",
                    folder.getRoot().toPath().resolve("sample-csv.txt").toString())));
    verifyNoMoreInteractions(mockLineReader, mockTerminal, mockPrintWriter);

    verify(mockDb)
        .put(
            eq("bob.smith@example.net"),
            eq(record("Smith", "Bob", "bob.smith@example.net", "red", "01/23/1972")));
    verify(mockDb)
        .put(
            eq("ted.weaver@example.net"),
            eq(record("Weaver", "Ted", "ted.weaver@example.net", "green", "03/13/1988")));
    verify(mockDb)
        .put(
            eq("redacted@example.net"),
            eq(record("Ames", "Richard", "redacted@example.net", "unknown", "11/01/1923")));

    verifyNoMoreInteractions(mockDb);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testIngestCommandNoArg() throws IOException {
    commandLine.handleInput("ingest");
    verify(mockLineReader).getTerminal();
    verifyNoMoreInteractions(mockLineReader);
    verifyZeroInteractions(mockDb, mockTerminal, mockPrintWriter);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testIngestCommandRelInvalidPathArg() throws IOException {
    commandLine.handleInput("ingest does-not-exist/");
    verify(mockLineReader).getTerminal();
    verifyNoMoreInteractions(mockLineReader);
    verifyZeroInteractions(mockDb, mockTerminal, mockPrintWriter);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testIngestCommandAbsInvalidPathArg() throws IOException {
    String input =
        String.format(
            "ingest %s",
            folder.getRoot().toPath().resolve("does-not-exist").toFile().getAbsolutePath());
    LOGGER.info("Running command '{}'", input);
    commandLine.handleInput(input);
    verify(mockLineReader).getTerminal();
    verifyNoMoreInteractions(mockLineReader);
    verifyZeroInteractions(mockDb, mockTerminal, mockPrintWriter);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testIngestCommandRelDirectoryPath() throws IOException {
    commandLine.handleInput("ingest folder-that-exists/");
    verify(mockLineReader).getTerminal();
    verifyNoMoreInteractions(mockLineReader);
    verifyZeroInteractions(mockDb, mockTerminal, mockPrintWriter);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testIngestCommandAbsDirectoryPath() throws IOException {
    String input =
        String.format(
            "ingest %s",
            folder.getRoot().toPath().resolve("folder-that-exists").toFile().getAbsolutePath());
    LOGGER.info("Running command '{}'", input);
    commandLine.handleInput(input);
    verify(mockLineReader).getTerminal();
    verifyNoMoreInteractions(mockLineReader);
    verifyZeroInteractions(mockDb, mockTerminal, mockPrintWriter);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testIngestCommandRelNotTxtExtension() throws IOException {
    commandLine.handleInput("ingest empty.csv");
    verify(mockLineReader).getTerminal();
    verifyNoMoreInteractions(mockLineReader);
    verifyZeroInteractions(mockDb, mockTerminal, mockPrintWriter);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testIngestCommandAbsNotTxtExtension() throws IOException {
    String input =
        String.format(
            "ingest %s", folder.getRoot().toPath().resolve("empty.csv").toFile().getAbsolutePath());
    LOGGER.info("Running command '{}'", input);
    commandLine.handleInput(input);
    verify(mockLineReader).getTerminal();
    verifyNoMoreInteractions(mockLineReader);
    verifyZeroInteractions(mockDb, mockTerminal, mockPrintWriter);
  }

  @Test
  public void testUnrecognizedCommand() throws IOException {
    commandLine.handleInput("hi");
    verify(mockLineReader).getTerminal();
    verify(mockTerminal).writer();
    verify(mockPrintWriter).println(eq("Unrecognized command"));
    verifyNoMoreInteractions(mockLineReader, mockTerminal, mockPrintWriter);
    verifyZeroInteractions(mockDb);
  }

  private static Map<String, String> record(
      String lastName, String firstName, String email, String favoriteColor, String dateOfBirth) {
    Map<String, String> entry = new HashMap<>();
    entry.put("lastName", lastName);
    entry.put("firstName", firstName);
    entry.put("email", email);
    entry.put("favoriteColor", favoriteColor);
    entry.put("dateOfBirth", dateOfBirth);
    return entry;
  }
}
