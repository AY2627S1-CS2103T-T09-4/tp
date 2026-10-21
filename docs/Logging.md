---
  layout: default.md
  title: "Logging guide"
---

# Logging guide

* The project uses the `java.util.logging` package for logging.
* `LogsCenter` manages logging levels and destinations.
* A class obtains a `Logger` with `LogsCenter.getLogger(Class)`, which logs messages at the configured level.
* Log messages are written to the console and a `.log` file.
* The output logging level can be controlled by changing the `LOG_LEVEL` constant in the `LogsCenter` class.
* **When choosing a level for a log message**, follow the conventions given in [_[se-edu/guides] Java: Logging conventions_](https://se-education.org/guides/conventions/java/logging.html).

## Patient updates and save failures

* `AddCommand` logs which optional fields are recorded at `FINE` after adding the patient to the model.
* `EditCommand` logs supplied date-of-birth, blood-type, alcohol-history, smoking-status, and emergency-contact fields as set or cleared at `FINE`
  after the model accepts the edit. Cancelled or rejected edits do not produce these update messages.
* These diagnostic messages contain field names and presence flags rather than patient field values.
  They describe model changes; saving the data file happens afterward.
* `LogicManager` logs save failures at `WARNING`, with the exception for diagnosis, before reporting the error
  to the user. Access-denied failures are distinguished from other I/O failures.
* The default level is `INFO`. Set `LogsCenter.LOG_LEVEL` to `Level.FINE` to include optional-field diagnostics.
