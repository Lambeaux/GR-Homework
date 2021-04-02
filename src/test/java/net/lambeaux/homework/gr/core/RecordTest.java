package net.lambeaux.homework.gr.core;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.util.HashMap;
import java.util.Map;
import org.junit.Test;

public class RecordTest {

  @Test
  public void testCreateRecord() {
    String[] fields = new String[] {"Smith", "Bob", "bob.smith@example.net", "red", "01/23/1972"};
    assertThat(
        Record.create(fields),
        is(record("Smith", "Bob", "bob.smith@example.net", "red", "01/23/1972")));
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
