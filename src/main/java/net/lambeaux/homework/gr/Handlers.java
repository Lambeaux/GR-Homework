package net.lambeaux.homework.gr;

import static net.lambeaux.homework.gr.MiscValidation.validateThat;

import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;
import net.lambeaux.homework.gr.core.ContentReader;
import net.lambeaux.homework.gr.core.Record;
import net.lambeaux.homework.gr.persistence.InMemoryDatabase;
import org.jetbrains.annotations.NotNull;

public class Handlers {

  // Adds REST handlers to Javalin app, keeps path definitions next to called code
  public static void inject(Javalin app, InMemoryDatabase db, ContentReader reader) {
    app.get("/records/:sort", new Get(db));
    app.post("/records", new Create(reader, db));
  }

  public static class Get implements Handler {

    private final InMemoryDatabase db;

    public Get(InMemoryDatabase db) {
      this.db = db;
    }

    @Override
    public void handle(@NotNull Context ctx) throws Exception {
      String sort = ctx.pathParam("sort");
      validateThat(() -> !sort.isEmpty(), String.format("invalid sort param, '%s'", sort));
      SortedSet<Record> results;
      switch (sort) {
        case "email":
          results = new TreeSet<>(Comparator.comparing(Record::getEmail));
          break;
        case "birthdate":
          results = new TreeSet<>(Comparator.comparing(Record::getDateOfBirth));
          break;
        case "name":
          results =
              new TreeSet<>(
                  Comparator.comparing(Record::getLastName).thenComparing(Record::getFirstName));
          break;
        default:
          throw new IllegalArgumentException(
              String.format("invalid sort was specified, '%s'", sort));
      }
      results.addAll(db.allValues());
      ctx.json(results);
    }
  }

  public static class Create implements Handler {

    private final InMemoryDatabase db;

    private final ContentReader contentReader;

    public Create(ContentReader contentReader, InMemoryDatabase db) {
      this.contentReader = contentReader;
      this.db = db;
    }

    @Override
    public void handle(@NotNull Context ctx) throws Exception {
      String mimeType = ctx.contentType();
      validateThat(
          () -> contentReader.canHandle(ctx), "cannot process request with mimetype " + mimeType);
      Record rec = contentReader.read(ctx);
      db.put(rec.getEmail(), rec);
    }
  }
}
