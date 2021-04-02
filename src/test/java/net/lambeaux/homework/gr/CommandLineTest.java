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
import java.nio.file.Path;
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

  private static final String FILE_UNSUPPORTED_DOT_TXT = "unsupported.txt";

  private static final String FILE_SAMPLE_DOT_CSV = "sample.csv";

  private static final String FILE_SAMPLE_DOT_PSV = "sample.psv";

  private static final String FILE_SAMPLE_DOT_SSV = "sample.ssv";

  private static final String FOLDER_THAT_EXISTS = "folder-that-exists";

  private static final String DOES_NOT_EXIST = "does-not-exist";

  @Rule public TemporaryFolder folder = new TemporaryFolder();

  @Mock private InMemoryDatabase mockDb;

  @Mock private LineReader mockLineReader;

  @Mock private Terminal mockTerminal;

  @Mock private PrintWriter mockPrintWriter;

  private CommandLine commandLine;

  private static void copyTestResource(String fileName, File dest) throws IOException {
    try (InputStream in = CommandLineTest.class.getResourceAsStream(DIR_SAMPLE_DATA + fileName)) {
      Files.copy(in, dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }
  }

  @Before
  public void before() throws IOException {
    File dirRoot = folder.getRoot();
    folder.newFolder(FOLDER_THAT_EXISTS);

    copyTestResource(FILE_UNSUPPORTED_DOT_TXT, folder.newFile(FILE_UNSUPPORTED_DOT_TXT));
    copyTestResource(FILE_SAMPLE_DOT_CSV, folder.newFile(FILE_SAMPLE_DOT_CSV));
    copyTestResource(FILE_SAMPLE_DOT_PSV, folder.newFile(FILE_SAMPLE_DOT_PSV));
    copyTestResource(FILE_SAMPLE_DOT_SSV, folder.newFile(FILE_SAMPLE_DOT_SSV));

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
  public void testIngestCsvRel() throws IOException {
    Path abs = folder.getRoot().toPath().resolve(FILE_SAMPLE_DOT_CSV);
    String input = String.format("ingest %s", FILE_SAMPLE_DOT_CSV);
    LOGGER.info("Running command '{}'", input);

    commandLine.handleInput(input);

    verify(mockLineReader).getTerminal();
    verify(mockTerminal).writer();
    verify(mockPrintWriter)
        .println(eq(String.format("Successfully ingested '%s'", abs.toString())));
    verifyNoMoreInteractions(mockLineReader, mockTerminal, mockPrintWriter);

    verifyMockDbSampleData();
  }

  @Test
  public void testIngestCsvAbs() throws IOException {
    Path abs = folder.getRoot().toPath().resolve(FILE_SAMPLE_DOT_CSV);
    String input = String.format("ingest %s", abs.toString());
    LOGGER.info("Running command '{}'", input);

    commandLine.handleInput(input);

    verify(mockLineReader).getTerminal();
    verify(mockTerminal).writer();
    verify(mockPrintWriter)
        .println(eq(String.format("Successfully ingested '%s'", abs.toString())));
    verifyNoMoreInteractions(mockLineReader, mockTerminal, mockPrintWriter);

    verifyMockDbSampleData();
  }

  @Test
  public void testIngestPsvRel() throws IOException {
    Path abs = folder.getRoot().toPath().resolve(FILE_SAMPLE_DOT_PSV);
    String input = String.format("ingest %s", FILE_SAMPLE_DOT_PSV);
    LOGGER.info("Running command '{}'", input);

    commandLine.handleInput(input);

    verify(mockLineReader).getTerminal();
    verify(mockTerminal).writer();
    verify(mockPrintWriter)
        .println(eq(String.format("Successfully ingested '%s'", abs.toString())));
    verifyNoMoreInteractions(mockLineReader, mockTerminal, mockPrintWriter);

    verifyMockDbSampleData();
  }

  @Test
  public void testIngestPsvAbs() throws IOException {
    Path abs = folder.getRoot().toPath().resolve(FILE_SAMPLE_DOT_PSV);
    String input = String.format("ingest %s", abs.toString());
    LOGGER.info("Running command '{}'", input);

    commandLine.handleInput(input);

    verify(mockLineReader).getTerminal();
    verify(mockTerminal).writer();
    verify(mockPrintWriter)
        .println(eq(String.format("Successfully ingested '%s'", abs.toString())));
    verifyNoMoreInteractions(mockLineReader, mockTerminal, mockPrintWriter);

    verifyMockDbSampleData();
  }

  @Test
  public void testIngestSsvRel() throws IOException {
    Path abs = folder.getRoot().toPath().resolve(FILE_SAMPLE_DOT_SSV);
    String input = String.format("ingest %s", FILE_SAMPLE_DOT_SSV);
    LOGGER.info("Running command '{}'", input);

    commandLine.handleInput(input);

    verify(mockLineReader).getTerminal();
    verify(mockTerminal).writer();
    verify(mockPrintWriter)
        .println(eq(String.format("Successfully ingested '%s'", abs.toString())));
    verifyNoMoreInteractions(mockLineReader, mockTerminal, mockPrintWriter);

    verifyMockDbSampleData();
  }

  @Test
  public void testIngestSsvAbs() throws IOException {
    Path abs = folder.getRoot().toPath().resolve(FILE_SAMPLE_DOT_SSV);
    String input = String.format("ingest %s", abs.toString());
    LOGGER.info("Running command '{}'", input);

    commandLine.handleInput(input);

    verify(mockLineReader).getTerminal();
    verify(mockTerminal).writer();
    verify(mockPrintWriter)
        .println(eq(String.format("Successfully ingested '%s'", abs.toString())));
    verifyNoMoreInteractions(mockLineReader, mockTerminal, mockPrintWriter);

    verifyMockDbSampleData();
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
    String input = String.format("ingest %s", DOES_NOT_EXIST);
    LOGGER.info("Running command '{}'", input);

    commandLine.handleInput(input);

    verify(mockLineReader).getTerminal();
    verifyNoMoreInteractions(mockLineReader);
    verifyZeroInteractions(mockDb, mockTerminal, mockPrintWriter);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testIngestCommandAbsInvalidPathArg() throws IOException {
    Path abs = folder.getRoot().toPath().resolve(DOES_NOT_EXIST);
    String input = String.format("ingest %s", abs.toString());
    LOGGER.info("Running command '{}'", input);

    commandLine.handleInput(input);

    verify(mockLineReader).getTerminal();
    verifyNoMoreInteractions(mockLineReader);
    verifyZeroInteractions(mockDb, mockTerminal, mockPrintWriter);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testIngestCommandRelDirectoryPath() throws IOException {
    String input = String.format("ingest %s", FOLDER_THAT_EXISTS);
    LOGGER.info("Running command '{}'", input);

    commandLine.handleInput(input);

    verify(mockLineReader).getTerminal();
    verifyNoMoreInteractions(mockLineReader);
    verifyZeroInteractions(mockDb, mockTerminal, mockPrintWriter);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testIngestCommandAbsDirectoryPath() throws IOException {
    Path abs = folder.getRoot().toPath().resolve(FOLDER_THAT_EXISTS);
    String input = String.format("ingest %s", abs.toString());
    LOGGER.info("Running command '{}'", input);

    commandLine.handleInput(input);

    verify(mockLineReader).getTerminal();
    verifyNoMoreInteractions(mockLineReader);
    verifyZeroInteractions(mockDb, mockTerminal, mockPrintWriter);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testIngestCommandRelUnsupportedExtension() throws IOException {
    String input = String.format("ingest %s", FILE_UNSUPPORTED_DOT_TXT);
    LOGGER.info("Running command '{}'", input);

    commandLine.handleInput(input);

    verify(mockLineReader).getTerminal();
    verifyNoMoreInteractions(mockLineReader);
    verifyZeroInteractions(mockDb, mockTerminal, mockPrintWriter);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testIngestCommandAbsUnsupportedExtension() throws IOException {
    Path abs = folder.getRoot().toPath().resolve(FILE_UNSUPPORTED_DOT_TXT);
    String input = String.format("ingest %s", abs.toString());
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

  // Set of verifications that map to valid files in src/test/resources/sample-data
  private void verifyMockDbSampleData() {
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
