package net.lambeaux.homework.gr.core;

import static net.lambeaux.homework.gr.MiscValidation.validateThat;

import io.javalin.http.Context;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class ContentReader {

  private static final String WHITE_SPACE = " ";

  private static final String EXT_CSV = "csv";

  private static final String EXT_PSV = "psv";

  private static final String EXT_SSV = "ssv";

  private final Map<String, ParseStrategy> parsers;

  public ContentReader() {
    this.parsers = new HashMap<>();
    this.parsers.put(EXT_CSV, new ParseStrategy(","));
    this.parsers.put(EXT_PSV, new ParseStrategy("\\|"));
    this.parsers.put(EXT_SSV, new ParseStrategy(WHITE_SPACE));
  }

  public boolean canHandle(Path filePath) {
    return parsers.containsKey(getExt(filePath));
  }

  public boolean canHandle(Context context) {
    return parsers.containsKey(getExt(context));
  }

  public List<Record> read(Path filePath) throws IOException {
    ParseStrategy parser = parsers.get(getExt(filePath));
    validateThat(
        () -> parser != null,
        "file " + filePath.toAbsolutePath().toString() + " is not a supported format");
    return parser.read(filePath);
  }

  public Record read(Context context) {
    String body = context.body();
    String line = body.split(System.lineSeparator())[0];
    ParseStrategy parser = parsers.get(getExt(context));
    validateThat(
        () -> parser != null,
        String.format("unsupported content-type '%s'", context.contentType()));
    return parser.read(line);
  }

  private String getExt(Path path) {
    String absPathStr = path.toFile().getAbsolutePath();
    return absPathStr.substring(absPathStr.lastIndexOf('.') + 1);
  }

  private String getExt(Context context) {
    String contentType =
        Objects.requireNonNull(context.contentType(), "content type cannot be null");
    String mimeType = contentType.split(";")[0];
    String[] parts = mimeType.split("/");
    validateThat(
        () -> parts.length == 2, String.format("malformed content-type '%s'", contentType));
    validateThat(
        () -> "text".equals(parts[0]), String.format("unsupported content-type '%s'", contentType));
    return parts[1];
  }

  /** Can parse input files based upon the provided delimiter. */
  private static class ParseStrategy {

    private final String delimiter;

    private ParseStrategy(String delimiter) {
      if (Objects.requireNonNull(delimiter, "delimiter cannot be null").isEmpty()) {
        throw new IllegalArgumentException("delimiter cannot be empty");
      }
      this.delimiter = delimiter;
    }

    private List<Record> read(Path filePath) throws IOException {
      return Files.lines(filePath)
          .map(line -> line.split(delimiter))
          .map(Record::new)
          .collect(Collectors.toList());
    }

    private Record read(String entity) {
      return new Record(entity.split(delimiter));
    }
  }
}
