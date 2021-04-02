package net.lambeaux.homework.gr.core;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/** Main data structure being managed by the application. */
public class Record {

  /**
   * Creates a record as a map from an array of fields. Order of fields must be:
   *
   * <ol>
   *   <li>{@code lastName}
   *   <li>{@code firstName}
   *   <li>{@code email}
   *   <li>{@code favoriteColor}
   *   <li>{@code dateOfBirth}
   * </ol>
   *
   * Note the fields are still zero based despite the rendering of the above list.
   *
   * @param fields array of strings containing the record's values.
   * @return a record as a map.
   */
  public static Map<String, String> create(String[] fields) {
    Map<String, String> entry = new HashMap<>();
    entry.put("lastName", Objects.requireNonNull(fields[0]));
    entry.put("firstName", Objects.requireNonNull(fields[1]));
    entry.put("email", Objects.requireNonNull(fields[2]));
    entry.put("favoriteColor", Objects.requireNonNull(fields[3]));
    entry.put("dateOfBirth", Objects.requireNonNull(fields[4]));
    return entry;
  }
}
