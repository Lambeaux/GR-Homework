package net.lambeaux.homework.gr;

import static net.lambeaux.homework.gr.MiscValidation.validateThat;

import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import net.lambeaux.homework.gr.core.ContentReader;
import net.lambeaux.homework.gr.core.Record;
import net.lambeaux.homework.gr.persistence.InMemoryDatabase;
import org.jetbrains.annotations.NotNull;

public class Handlers {

  // Adds REST handlers to Javalin app, keeps path definitions next to called code
  public static void inject(Javalin app, InMemoryDatabase db, ContentReader reader) {
    app.post("/records", new Create(reader, db));
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
