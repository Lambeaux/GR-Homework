package net.lambeaux.homework.gr;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import io.javalin.Javalin;
import io.restassured.RestAssured;
import io.restassured.http.Header;
import io.restassured.response.Response;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;
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

  private static final String SORTED_BY_EMAIL = "/app-test/sorted-by-email.json";

  private static final String SORTED_BY_BIRTHDATE = "/app-test/sorted-by-birthdate.json";

  private static final String SORTED_BY_NAME = "/app-test/sorted-by-name.json";

  private static final InMemoryDatabase MOCK_DB = mock(InMemoryDatabase.class);

  private static Javalin app;

  private static String appUrl;

  @BeforeClass
  public static void beforeClass() throws IOException {
    app = Javalin.create().start();
    appUrl = String.format("http://localhost:%d/records", app.port());
    App.startAppWithDependencies(app, new ContentReader(), MOCK_DB, false);

    LOGGER.info("Running app tests at {}", appUrl);
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
            .post(appUrl)
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
            .post(appUrl)
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
            .post(appUrl)
            .statusCode();

    assertThat(statusCode, is(200));
    verify(MOCK_DB)
        .put(
            "fred.george@example.net",
            new Record("George", "Fred", "fred.george@example.net", "blue", "08/12/1901"));
    verifyNoMoreInteractions(MOCK_DB);
  }

  @Test
  public void testGetByEmail() throws Exception {
    doReturn(cannedRecords()).when(MOCK_DB).allValues();
    Response response = RestAssured.given().get(appUrl.concat("/email"));
    assertThat(response.getStatusCode(), is(200));
    assertThat(response.getBody().asString(), is(testResource(SORTED_BY_EMAIL)));
  }

  @Test
  public void testGetByBirthdate() throws Exception {
    doReturn(cannedRecords()).when(MOCK_DB).allValues();
    Response response = RestAssured.given().get(appUrl.concat("/birthdate"));
    assertThat(response.getStatusCode(), is(200));
    assertThat(response.getBody().asString(), is(testResource(SORTED_BY_BIRTHDATE)));
  }

  @Test
  public void testGetByName() throws Exception {
    doReturn(cannedRecords()).when(MOCK_DB).allValues();
    Response response = RestAssured.given().get(appUrl.concat("/name"));
    assertThat(response.getStatusCode(), is(200));
    assertThat(response.getBody().asString(), is(testResource(SORTED_BY_NAME)));
  }

  private static String testResource(String resourceName) throws IOException, URISyntaxException {
    return new String(
        Files.readAllBytes(Paths.get(AppTest.class.getResource(resourceName).toURI())),
        StandardCharsets.UTF_8);
  }

  private static Collection<Record> cannedRecords() {
    return Stream.of(
            new Record("George", "Fred", "kool.dude@example.net", "blue", "08/12/1901"),
            new Record("Zimmer", "Joey", "joey.zimmer@example.net", "purple", "07/01/1944"),
            new Record("Lars", "Owen", "owen.lars@example.net", "yellow", "08/12/2950"))
        .collect(Collectors.toList());
  }
}
