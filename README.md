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

// Setup watchers
Log.setWatchers(new ExceptionWatcher(), new ActivityWatcher(myApplication));

// Shutdown
Log.shutdown()
```

### TODO

