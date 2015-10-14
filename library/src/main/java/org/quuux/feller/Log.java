package org.quuux.feller;


public class Log {

    private final String mTag;
    private static String sPrefix;

    public static void setsPrefix(final String prefix) {
        sPrefix = prefix;
    }

    public static String buildTag(final String tag) {
        return sPrefix == null ? tag : sPrefix + ":" + tag;
    }

    public static String buildTag(final Class klass) {
        return buildTag(klass.getName());
    }

    public static void println(final int priority, final String tag, final String fmt, final Object... args) {
        android.util.Log.println(priority, tag, String.format(fmt, args));
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
