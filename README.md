# GR Homework

Repository for the submission.

## Building and Running the Solution

Run all provided commands from the project root directory.

### Prerequisites

- Java 8 or higher
- Maven

### Building

```text
mvn clean install
```

### Running

```text
java -jar target/lombardi-homework-gr-1.0-SNAPSHOT.jar
```

### Testing

Refer to the automated tests and exported Postman collection in the submission.

## Project Details

### Impl Notes

- This exercise took about 14 hours to accomplish, refer to git log for details.
- I chose Java over Clojure because I'm familiar with that ecosystem and did not want to prolong
the process; I was also short on time and that was a factor in my considerations. I could have
definitely done a hybrid but that would have been a poor fit to match with my library choices.

### Assumptions

#### Instruction Clarifications

- By "display dates in the format `M/D/YYYY`" it meant `MM/DD/YYYY` since months and days are not
  limited to single digits.
- Where files support multiple records, the REST service (POST) only supports one at a time.
- Sorting within the REST service will return natural order.
- Sorting by name gives last name higher priority than first name.

#### Input Content

- Input content always has their fields in the specified order from the instructions (`lastName`,
  `firstName`, `email`, `favoriteColor`, `dateOfBirth`).
- No fields within the input files will be `null` or empty.
- File format delimiters won't appear inside data values.
- Dates as input to the system are in the form `MM/DD/YYYY`.
- Emails are unique and can be keyed on; duplicate emails result in overwriting prior record.

#### Input Files

- Accepting CLI input one file at a time is acceptable, per "...takes as input _a file_...".
- Input files can have extensions `.csv`, `.psv`, or `.ssv` and it is assumed the proper delimiter,
  and **only** the proper delimiter, is used correctly in each. For example, if commas (`,`) appear
  in the document, then pipes (`|`) cannot appear, and vice versa.
- Input files are reasonably small enough such that the full contents of 10 - 200 documents can fit
  into memory at once (an approximation derived from Jetty's default threading configuration -
  refer to https://www.eclipse.org/jetty/documentation/jetty-9/index.php).

#### General notes

- If emails are unique, some ordering options aren't very useful and tests won't actually be able to
prove the ordering works.
- The same file extensions can be used as `text/~` mimetypes with the REST service.

### Next Steps

- Keep an eye on thread-safety of `InMemoryDatabase`.
- Work off assumptions as necessary to improve robustness of app.
- When appropriate, add actual interfaces and javadoc.
- Package javadoc with the project.
- Fix test assertion strategies to not couple against internal details.
- Better define 'units' to test as the app scales.
- Ensure specific error messages are only available to developers to prevent reverse-engineering.
- Provide a threat model for the entire solution.
- Upgrade to Java 11.
