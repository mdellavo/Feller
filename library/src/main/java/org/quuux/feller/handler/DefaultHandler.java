package org.quuux.feller.handler;

import org.quuux.feller.Log;

public class DefaultHandler implements Log.LogHandler {

    @Override
    public void start() {}

    @Override
    public void stop() {}

    @Override
    public void println(final Log.LogEntry entry) {
        android.util.Log.println(entry.priority, entry.tag, entry.message);
        try {
            Log.recycleEntry(entry);
        } catch (InterruptedException e) {
            android.util.Log.e("DefaultHandler", "error queuing entry, dropping message!!!");
        }
    }
}
