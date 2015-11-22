package org.quuux.feller;

import org.quuux.feller.handler.DefaultHandler;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;


public class Log {

    private static final String TAG = buildTag(Log.class);

    public static final int POOL_SIZE = 1024;

    public static class LogEntry {
        public long timestamp;
        public int priority;
        public String tag;
        public String message;
        public Throwable throwable;

        void recycle() {
            timestamp = 0;
            priority = 0;
            tag = null;
            message = null;
            throwable = null;
        }

        public void set(final long timestamp, final int priority, final String tag, final String msg, final Throwable throwable) {
            this.timestamp = timestamp;
            this.priority = priority;
            this.tag = tag;
            this.message = msg;
            this.throwable = throwable;
        }
    }

    public interface LogHandler {
        void start();
        void stop();
        void println(LogEntry entry);
    }

    public interface AutoTagger {
        String format(StackTraceElement loc);
    }

    private static BlockingQueue<LogEntry> pool = new ArrayBlockingQueue<>(POOL_SIZE, true);

    static {
        for (int i = 0; i < pool.remainingCapacity(); i++)
            pool.add(new LogEntry());
    }

    private static LogHandler[] handlers = new LogHandler[]{new DefaultHandler()};
    private static AutoTagger tagger = new DefaultAutoTagger();

    private final String mTag;
    private static String sPrefix;

    public static void setsPrefix(final String prefix) {
        sPrefix = prefix;
    }

    public static void setAutoTagger(final AutoTagger tagger) {
        Log.tagger = tagger;
    }

    public static void setHandlers(final LogHandler... handlers) {
        stopHandlers();
        Log.handlers = handlers;
        startHandlers();
    }

    private static void startHandlers() {
        for (LogHandler handler : handlers)
            handler.start();
    }

    private static void stopHandlers() {
        for (LogHandler handler : handlers)
            handler.stop();
    }

    public static void shutdown() {
        stopHandlers();
    }

    private static LogEntry getLogEntry(final long timestamp, final int priority, final String tag, final String msg, final Throwable throwable) {
        LogEntry entry = null;
        try {
            entry = pool.poll(0, TimeUnit.MILLISECONDS);
            if (entry != null)
                entry.set(timestamp, priority, tag, msg, throwable);
            else
                android.util.Log.e(TAG, "no available log entries in pool");
        } catch (InterruptedException ignored) {
        }
        return entry;
    }

    public static void recycleEntry(final LogEntry entry) {
        entry.recycle();
        pool.offer(entry);
    }

    public static String buildTag(final String tag) {
        return sPrefix == null ? tag : sPrefix + ":" + tag;
    }

    public static String buildTag(final Class klass) {
        return buildTag(klass.getSimpleName());
    }

    public static void println(final int priority, final String tag, final String fmt, final Object... args) {

        final long timestamp = System.currentTimeMillis();
        final String msg = String.format(fmt, args);
        final Throwable throwable = args.length > 0 && args[args.length - 1] instanceof Throwable ? (Throwable) args[args.length - 1] : null;

        for (int i = 0; i < handlers.length; i++) {
            final LogEntry entry = getLogEntry(timestamp, priority, tag, msg, throwable);
            if (entry != null)
                handlers[i].println(entry);
        }
    }

    public static void d(final String tag, final String message, final Object... args) {
        println(android.util.Log.DEBUG, tag, message, args);
    }

    public static void v(final String tag, final String message, final Object... args) {
        println(android.util.Log.VERBOSE, tag, message, args);
    }

    public static void i(final String tag, final String message, final Object... args) {
        println(android.util.Log.INFO, tag, message, args);
    }

    public static void e(final String tag, final String message, final Object... args) {
        println(android.util.Log.ERROR, tag, message, args);
    }

    public static void w(final String tag, final String message, final Object... args) {
        println(android.util.Log.WARN, tag, message, args);
    }

    public Log(final String tag) {
        mTag = tag;
    }

    public Log(final Class klass) {
        this(klass.getName());
    }

    public void d(final String message, final Object... args) {
        Log.d(mTag, message, args);
    }

    public void v(final String message, final Object... args) {
        Log.v(mTag, message, args);
    }

    public void i(final String message, final Object... args) {
        Log.i(mTag, message, args);
    }

    public void e(final String message, final Object... args) {
        Log.e(mTag, message, args);
    }

    public void w(final String message, final Object... args) {
        Log.w(mTag, message, args);
    }

    public static String getStackTraceString(Throwable tr) {
        return android.util.Log.getStackTraceString(tr);
    }

    public static String AUTO() {
        final Throwable tr = new Throwable();
        final StackTraceElement[] stacktrace = tr.getStackTrace();
        final StackTraceElement e = stacktrace[1];
        return tagger.format(e);
    }

    public static class DefaultAutoTagger implements AutoTagger {
        @Override
        public String format(final StackTraceElement e) {
            final String className = e.getClassName();
            final String simpleName = className.substring(className.lastIndexOf('.') + 1);
            return String.format("%s.%s:%s", simpleName, e.getMethodName(), e.getLineNumber());
        }
    }

}
