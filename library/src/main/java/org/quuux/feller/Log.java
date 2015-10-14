package org.quuux.feller;

import org.quuux.feller.handler.DefaultHandler;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class Log {

    private static final int POOL_SIZE = 1024;

    public static class LogEntry {
        public long timestamp;
        public int priority;
        public String tag;
        public String message;

        void recycle() {
            timestamp = 0;
            priority = 0;
            tag = null;
            message = null;
        }

        public void set(final long timestamp, final int priority, final String tag, final String msg) {
            this.timestamp = timestamp;
            this.priority = priority;
            this.tag = tag;
            this.message = msg;
        }
    }

    public interface LogHandler {
        void start();
        void stop();
        void println(LogEntry entry);
    }

    private static BlockingQueue<LogEntry> pool = new ArrayBlockingQueue<LogEntry>(POOL_SIZE, true);
    private static LogHandler[] handlers = new LogHandler[] {new DefaultHandler()};

    private final String mTag;
    private static String sPrefix;

    public static void setsPrefix(final String prefix) {
        sPrefix = prefix;
    }

    private static void init() {
        for (int i = 0; i < pool.remainingCapacity(); i++)
            pool.add(new LogEntry());
    }

    private static LogEntry getLogEntry(final long timestamp, final int priority, final String tag, final String msg) throws InterruptedException {
        LogEntry entry = pool.take();
        entry.set(timestamp, priority, tag, msg);
        return entry;
    }

    public static void recycleEntry(final LogEntry entry) throws InterruptedException {
        entry.recycle();
        pool.put(entry);
    }

    public static String buildTag(final String tag) {
        return sPrefix == null ? tag : sPrefix + ":" + tag;
    }

    public static String buildTag(final Class klass) {
        return buildTag(klass.getName());
    }

    public static void println(final int priority, final String tag, final String fmt, final Object... args) {
        final long timestamp = System.currentTimeMillis();
        final String msg = String.format(fmt, args);

        for (int i=0; i<handlers.length; i++) {
            try {
                final LogEntry entry = getLogEntry(timestamp, priority, tag, msg);
                handlers[i].println(entry);
            } catch (InterruptedException e) {
                android.util.Log.e("Log", "log entry pool underflow, dropping message!!!");
            }
        }
    }

    public static void d(final String tag, final String message, final Object...args) {
        if (BuildConfig.DEBUG) {
            println(android.util.Log.DEBUG, tag, message, args);
        }
    }

    public static void v(final String tag, final String message, final Object...args) {
        if (BuildConfig.DEBUG) {
            println(android.util.Log.VERBOSE, tag, message, args);
        }
    }

    public static void i(final String tag, final String message, final Object...args) {
        if (BuildConfig.DEBUG) {
            println(android.util.Log.INFO, tag, message, args);
        }
    }

    public static void e(final String tag, final String message, final Object...args) {
        println(android.util.Log.ERROR, tag, message, args);
    }

    public static void w(final String tag, final String message, final Object...args) {
        println(android.util.Log.WARN, tag, message, args);
    }

    public Log(final String tag) {
        mTag = tag;
    }

    public Log(final Class klass) {
        this(klass.getName());
    }

    public void d(final String message, final Object...args) {
        Log.d(mTag, message, args);
    }

    public void v(final String message, final Object...args) {
        Log.v(mTag, message, args);
    }

    public void i(final String message, final Object...args) {
        Log.i(mTag, message, args);
    }

    public void e(final String message, final Object...args) {
        Log.e(mTag, message, args);
    }

    public void w(final String message, final Object...args) {
        Log.w(mTag, message, args);
    }
}
