# Feller


Feller is a set of tools to help with debugging applications.

## Log

Log is a drop in replacement for the Android Log class. It aims for high performance and extensibility.
Log supports multiple log handlers for collecting output.  Log also has support for Watchers to
gather additional logging information such as unhandled exceptions and Activity lifecycle. Log also
a number of helpful utilities.

## Trace

Trace is a drop in replacement for the Android Trace class.

### Features
- String.format style log formatting
- Traceback if last argument is a Throwable
- Log.AUTO() autotagger via introspection
- Extensible log handlers
- Trace replacement with fallback to Log on unsupported platforms

### Examples
```java

// Simple usage
import static org.quuux.fellter.Log.AUTO;
Log.d(AUTO(), "hello %s", world, new Throwable());

String TAG = Log.buildTag(Foo.class);

// Create an instance with fixed tag
Log logger = new Log("tag");
logger.d("hello world");

// Setup handlers
Log.setHandlers(new DefaultHandler(), new FileHandler(new File("/path/to/somewhere")));


// Trace support
Trace.setTraceFile(new File("path/to/elsewhere"));
Trace.beginSection("foo");
// do stuff..
Trace.endSection();
```

### TODO

