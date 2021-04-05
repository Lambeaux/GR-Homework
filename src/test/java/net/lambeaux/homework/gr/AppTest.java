package net.lambeaux.homework.gr;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import io.javalin.Javalin;
import io.restassured.RestAssured;
import io.restassured.http.Header;
import java.io.IOException;
import net.lambeaux.homework.gr.core.ContentReader;
import net.lambeaux.homework.gr.core.Record;
import net.lambeaux.homework.gr.persistence.InMemoryDatabase;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Be sure to only run these single threaded due to the shared mock. */
@RunWith(MockitoJUnitRunner.class)
public class AppTest {

  private static final Logger LOGGER = LoggerFactory.getLogger(AppTest.class);

  private static final InMemoryDatabase MOCK_DB = mock(InMemoryDatabase.class);

  private static Javalin app;

  private static String postUrl;

  @BeforeClass
  public static void beforeClass() throws IOException {
    app = Javalin.create().start();
    postUrl = String.format("http://localhost:%d/records", app.port());
    App.startAppWithDependencies(app, new ContentReader(), MOCK_DB, false);

    LOGGER.info("Running app tests at {}", postUrl);
  }

  @AfterClass
  public static void afterClass() {
    LOGGER.info("Shutting down test Javalin instance");
    app.stop();
  }

  @After
  public void after() {
    reset(MOCK_DB);
  }

  @Test
  public void testCreateCsv() {
    int statusCode =
        RestAssured.given()
            .header(new Header("Content-Type", "text/csv"))
            .body("George, Fred, fred.george@example.net, blue, 08/12/1901")
            .post(postUrl)
            .statusCode();

    assertThat(statusCode, is(200));
    verify(MOCK_DB)
        .put(
            "fred.george@example.net",
            new Record("George", "Fred", "fred.george@example.net", "blue", "08/12/1901"));
    verifyNoMoreInteractions(MOCK_DB);
  }

  @Test
  public void testCreatePsv() {
    int statusCode =
        RestAssured.given()
            .header(new Header("Content-Type", "text/psv"))
            .body("George | Fred | fred.george@example.net | blue | 08/12/1901")
            .post(postUrl)
            .statusCode();

    assertThat(statusCode, is(200));
    verify(MOCK_DB)
        .put(
            "fred.george@example.net",
            new Record("George", "Fred", "fred.george@example.net", "blue", "08/12/1901"));
    verifyNoMoreInteractions(MOCK_DB);
  }

  @Test
  public void testCreateSsv() {
    int statusCode =
        RestAssured.given()
            .header(new Header("Content-Type", "text/ssv"))
            .body("George Fred fred.george@example.net blue 08/12/1901")
            .post(postUrl)
            .statusCode();

    assertThat(statusCode, is(200));
    verify(MOCK_DB)
        .put(
            "fred.george@example.net",
            new Record("George", "Fred", "fred.george@example.net", "blue", "08/12/1901"));
    verifyNoMoreInteractions(MOCK_DB);
  }
}
