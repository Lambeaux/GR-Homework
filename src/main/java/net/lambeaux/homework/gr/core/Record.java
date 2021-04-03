package net.lambeaux.homework.gr.core;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

/** Main data structure being managed by the application. */
public class Record {

  private static final String DATE_FORMAT = "MM/dd/yyyy";

  private final transient SimpleDateFormat dateFormat;

  private final String lastName;

  private final String firstName;

  private final String email;

  private final String favoriteColor;

  private final Date dateOfBirth;

  public Record(String[] fields) {
    this(fields[0], fields[1], fields[2], fields[3], fields[4]);
  }

  public Record(
      String lastName, String firstName, String email, String favoriteColor, String dateOfBirth) {
    this.dateFormat = new SimpleDateFormat(DATE_FORMAT);

    this.lastName = Objects.requireNonNull(lastName, "lastName cannot be null").trim();
    this.firstName = Objects.requireNonNull(firstName, "firstName cannot be null").trim();
    this.email = Objects.requireNonNull(email, "email cannot be null").trim();
    this.favoriteColor =
        Objects.requireNonNull(favoriteColor, "favoriteColor cannot be null").trim();

    this.dateOfBirth =
        parseDate(Objects.requireNonNull(dateOfBirth, "dateOfBirth cannot be null").trim());
  }

  public String getLastName() {
    return lastName;
  }

  public String getFirstName() {
    return firstName;
  }

  public String getEmail() {
    return email;
  }

  public String getFavoriteColor() {
    return favoriteColor;
  }

  public Date getDateOfBirth() {
    return dateOfBirth;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    Record record = (Record) o;

    if (!getLastName().equals(record.getLastName())) {
      return false;
    }
    if (!getFirstName().equals(record.getFirstName())) {
      return false;
    }
    if (!getEmail().equals(record.getEmail())) {
      return false;
    }
    if (!getFavoriteColor().equals(record.getFavoriteColor())) {
      return false;
    }
    return getDateOfBirth().equals(record.getDateOfBirth());
  }

  @Override
  public int hashCode() {
    int result = getLastName().hashCode();
    result = 31 * result + getFirstName().hashCode();
    result = 31 * result + getEmail().hashCode();
    result = 31 * result + getFavoriteColor().hashCode();
    result = 31 * result + getDateOfBirth().hashCode();
    return result;
  }

  @Override
  public String toString() {
    return String.format(
        "%s %s (%s), born %s, likes %s",
        firstName, lastName, email, dateFormat.format(dateOfBirth), favoriteColor);
  }

  private Date parseDate(String date) {
    try {
      return dateFormat.parse(date);
    } catch (ParseException e) {
      throw new IllegalArgumentException("date could not be parsed", e);
    }
  }
}
