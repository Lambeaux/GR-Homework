# GR-Homework

Repository for the submission. Details to follow.

### Assumptions

#### Instruction Clarifications

- By "display dates in the format `M/D/YYYY`" it meant `MM/DD/YYYY` since months and days are not
  limited to single digits.

#### Input

- Input files will have extension `.txt` regardless of the internal delimiter.
- Input files are reasonably small enough such that the full contents of 10 - 200 documents can fit
  into memory at once (an approximation derived from Jetty's default threading configuration -
  refer to https://www.eclipse.org/jetty/documentation/jetty-9/index.php).
- If commas (`,`) appear in the document, then pipes (`|`) cannot appear, and vice versa.
- No fields within the input files will be `null` or empty.
- Dates as input to the system are in the form `MM/DD/YYYY`.
- Emails are unique and can be keyed on; since the system doesn't yet support updates, duplicate
  emails result in error (for now, assume operation failure, not per-record failure).
- **Right now only commas (`,`) are valid delimiters.**
