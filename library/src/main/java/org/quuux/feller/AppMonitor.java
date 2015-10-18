package org.quuux.feller;

import android.app.Application;
import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.quuux.feller.handler.DefaultHandler;
import org.quuux.feller.handler.FileHandler;
import org.quuux.feller.watcher.ActivityWatcher;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.UUID;

public class AppMonitor implements Thread.UncaughtExceptionHandler {

    private static final String TAG = Log.buildTag(AppMonitor.class);

    public interface Watcher {
        void start();
        void stop();
    }

    private static AppMonitor instance = null;

    private final String uuid;
    private File tombstonesBasePath;

    private Watcher[] watchers = new Watcher[] {};

    private Thread.UncaughtExceptionHandler defaultHandler;

    protected AppMonitor() {
        uuid = UUID.randomUUID().toString();
    }

    public static AppMonitor getInstance() {
        if (instance == null)
            instance = new AppMonitor();

        return instance;
    }

    public void attach(final Context context) {
        Log.d(TAG, "attaching (uid=%s)", uuid);

        tombstonesBasePath = new File(context.getExternalCacheDir(), ".feller-sessions");
        Tombstone.setApplicationInfo(context);

        Log.setHandlers(new DefaultHandler(), new FileHandler(getLogPath()));

        defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    public void attach(final Application application) {
        attach(application.getApplicationContext());
        setWatchers(new ActivityWatcher(application));
    }

    public void setWatchers(final Watcher... watchers) {
        stopWatchers();
        this.watchers = watchers;
        startWatchers();
    }

    private void startWatchers() {
        for (Watcher watcher : watchers)
            watcher.start();
    }

    private void stopWatchers() {
        for (Watcher watcher : watchers)
            watcher.stop();
    }

    private File getLogPath() {
        return new File(getGraveyardPath(), "app.log");
    }

    private File getTombstonePath() {
        return new File(getGraveyardPath(), "tombstone.json");
    }

    private File getGraveyardPath() {
        return new File(tombstonesBasePath, uuid);
    }

    @Override
    public void uncaughtException(final Thread thread, final Throwable ex) {

        Log.e("Log", "UNCAUGHT EXCEPTION IN THREAD %s/%s: %s", thread.getId(), thread.getName(), ex);

        final Tombstone tombstone = Tombstone.build(uuid, thread, ex);
        new Thread(new GraveDigger(tombstone, getTombstonePath())).start();

        if (defaultHandler != null)
            defaultHandler.uncaughtException(thread, ex);
    }

    static class GraveDigger implements Runnable {
        private final Tombstone tombstone;
        private final File path;

        public GraveDigger(final Tombstone tombstone, final File path) {
            this.tombstone = tombstone;
            this.path = path;
        }

        @Override
        public void run() {
            final Gson gson = new GsonBuilder().setPrettyPrinting().create();
            final String json = gson.toJson(tombstone);

            Writer writer = null;
            try {
                writer = new BufferedWriter(new FileWriter(path));
                writer.write(json);
                writer.close();
            } catch (IOException e) {
                Log.e(TAG, "error committing tombstone: " + e, e);
            }

            Log.shutdown();
        }
    }
}
