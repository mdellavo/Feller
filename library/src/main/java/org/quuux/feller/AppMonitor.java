package org.quuux.feller;

import android.app.Application;
import android.content.Context;
import android.os.Trace;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

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
    private Tombstone.ApplicationInfo app;
    private File graveyardPath;
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

        graveyardPath = new File(context.getExternalCacheDir(), ".feller-sessions");
        app = Tombstone.getApplicationInfo(context);

        Log.setHandlers(new DefaultHandler(), new FileHandler("\t", "\r\n", getLogPath()));

        defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);

        new Thread(new Reaper(graveyardPath, uuid)).start();
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
        return new File(getSessionPath(), "log.txt");
    }

    private File getTombstonePath() {
        return new File(getSessionPath(), "tombstone.json");
    }

    private File getSessionPath() {
        return new File(graveyardPath, uuid);
    }

    @Override
    public void uncaughtException(final Thread thread, final Throwable ex) {

        //Log.e("Log", "UNCAUGHT EXCEPTION IN THREAD %s/%s: %s", thread.getId(), thread.getName(), ex);

        final Tombstone tombstone = Tombstone.build(System.currentTimeMillis(), uuid, app, thread, ex);
        new GraveDigger(tombstone, getTombstonePath()).run();
        //new Thread().start();

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
            final Gson gson = new GsonBuilder()
                    .setDateFormat("yyyy-MM-dd'T'HH:mm:ssX")
                    .setPrettyPrinting()
                    .create();
            final String json = gson.toJson(tombstone);

            Writer writer = null;
            try {
                Log.d(TAG, "writing tombstone %s", path);
                writer = new BufferedWriter(new FileWriter(path));
                writer.write(json);
                writer.close();
            } catch (IOException e) {
                Log.e(TAG, "error committing tombstone: " + e, e);
            }
        }
    }

    static class Reaper implements Runnable {

        private static final String HOST = "192.168.1.9:6543";
        private final File graveyardPath;
        private final String currentSession;

        public Reaper(final File path, final String currentSession) {
            this.graveyardPath = path;
            this.currentSession = currentSession;
        }

        @Override
        public void run() {
            final File currentSesison = new File(graveyardPath, this.currentSession);

            File paths[] = graveyardPath.listFiles();

            Log.d(TAG, "paths=%s", paths);

            if (paths == null)
                return;

            for (File path : paths) {

                if (!path.isDirectory() || path.equals(currentSesison))
                    continue;

                final File tombstonePath = new File(path, "tombstone.json");

                boolean cleanup = true;
                if (tombstonePath.exists()) {
                    Log.d(TAG, "processing tombstone %s...", tombstonePath);
                    cleanup = process(path);
                }

                if (cleanup)
                    cleanup(path);
            }
        }

        private boolean process(final File session) {

            boolean rv = true;

            Log.d(TAG, "processing %s...", session.getName());
            for (File path : session.listFiles()) {
                String parts[] = path.getName().split("\\.(?=[^\\.]+$)");
                rv &= processPart(session.getName(), parts[0], parts[1], path);
            }

            return rv;
        }

        private boolean processPart(final String sessionName, final String resourceName, final String resourceType, final File path) {
            final OkHttpClient client = new OkHttpClient();

            final String url = "http://" + HOST + "/" + resourceName + "s/" + sessionName;
            final String contentType =  "json".equals(resourceType) ? "application/json" : "text/plain";

            final Request request = new Request.Builder()
                    .url(url)
                    .post(RequestBody.create(MediaType.parse(contentType), path))
                    .build();

            boolean rv = false;
            try {
                final long t1 = System.currentTimeMillis();
                final Response response = client.newCall(request).execute();
                final long t2 = System.currentTimeMillis();
                Log.d(TAG, "POST %s -> %s (%sms)",  url, response.code(), t2-t1);
                rv = response.isSuccessful();
            } catch (IOException e) {
                Log.e(TAG, "error posting %s to %s: %s", resourceName, url, e);
            }

            return rv;
        }

        private void cleanup(final File session) {
            Log.d(TAG, "cleaning %s...", session);
            for (File path : session.listFiles()) {
                //noinspection ResultOfMethodCallIgnored
                path.delete();
            }
            //noinspection ResultOfMethodCallIgnored
            session.delete();
        }
    }
}
