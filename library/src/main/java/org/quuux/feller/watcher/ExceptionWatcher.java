package org.quuux.feller.watcher;

import org.quuux.feller.Log;

public class ExceptionWatcher implements Log.Watcher, Thread.UncaughtExceptionHandler {
    private Thread.UncaughtExceptionHandler defaultHandler;

    @Override
    public void start() {
        defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    @Override
    public void stop() {
        Thread.setDefaultUncaughtExceptionHandler(defaultHandler);
    }

    @Override
    public void uncaughtException(final Thread thread, final Throwable ex) {
        Log.e("Log", "UNCAUGHT EXCEPTION IN THREAD %s: %s", thread.getId(), ex);

        if (defaultHandler != null)
            defaultHandler.uncaughtException(thread, ex);
    }
}
