package org.quuux.feller.handler;

import org.quuux.feller.Log;

public class DefaultHandler implements Log.LogHandler {

    @Override
    public void start() {}

    @Override
    public void stop() {}

    @Override
    public void println(final Log.LogEntry entry) {
        final String msg = entry.throwable != null ? entry.message + "\n" + Log.getStackTraceString(entry.throwable) : entry.message;
        android.util.Log.println(entry.priority, entry.tag, msg);
        Log.recycleEntry(entry);
    }
}
