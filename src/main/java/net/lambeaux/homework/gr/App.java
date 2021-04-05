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
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import net.lambeaux.homework.gr.core.ContentReader;
import net.lambeaux.homework.gr.persistence.InMemoryDatabase;
import org.jetbrains.annotations.NotNull;
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

  public static void main(String[] args) throws Exception {
    ContentReader contentReader = new ContentReader();
    InMemoryDatabase db = new InMemoryDatabase();

    LOGGER.info("Booting up server");
    Javalin app = Javalin.create(App::configureJavalin).start(PORT);
    startAppWithDependencies(app, contentReader, db, true);
  }

  public static void startAppWithDependencies(
      Javalin app, ContentReader contentReader, InMemoryDatabase db, boolean cli)
      throws IOException {
    JavalinJson.setFromJsonMapper(GSON::fromJson);
    JavalinJson.setToJsonMapper(GSON::toJson);

    LOGGER.info("Registering handlers");
    app.get("/extras/request-summary", new RequestSummaryHandler());

    Handlers.inject(app, db, contentReader);
    if (cli) {
      CommandLine commandLine = new CommandLine(db);
      commandLine.loop();
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
