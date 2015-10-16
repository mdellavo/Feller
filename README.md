# Feller


Feller is a set of tools to help with debugging applications.

## Log

Log is a drop in replacement for logging in your application. It aims for high performance and extensibility.
Log supports multiple log handlers for collecting output.  Log also has support for Watchers to
gather additional logging information such as unhandled exceptions and Activity lifecycle. Log also
a number of helpful utilities.

### Features
- String.format style log formatting
- Traceback if last argument is a Throwable
- Log.AUTO() autotagger via introspection
- Extensible log handlers
- Extensible event watchers

### TODO

