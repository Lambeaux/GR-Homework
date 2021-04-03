package net.lambeaux.homework.gr.core;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class RecordTest {

  @Test
  public void testCreateRecord() {
    String[] fields = new String[] {"Smith", "Bob", "bob.smith@example.net", "red", "01/23/1972"};
    assertThat(
        new Record(fields),
        is(new Record("Smith", "Bob", "bob.smith@example.net", "red", "01/23/1972")));
  }
}
