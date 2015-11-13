package org.quuux.feller.watcher;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Application;
import android.os.Build;
import android.os.Bundle;

import org.quuux.feller.AppMonitor;
import org.quuux.feller.Log;
import org.quuux.feller.Trace;

import java.lang.ref.WeakReference;

public class ActivityWatcher implements AppMonitor.Watcher {

    private static final String TAG = Log.buildTag(ActivityWatcher.class);

    private final WeakReference<Application> applicationRef;
    private Object callbacks;

    public ActivityWatcher(final Application application) {
        applicationRef = new WeakReference<Application>(application);
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    public void start() {
        final Application application = applicationRef.get();
        if (application == null || Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH)
            return;
        callbacks = getCallbacks();
        application.registerActivityLifecycleCallbacks((Application.ActivityLifecycleCallbacks) callbacks);
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    public void stop() {
        final Application application = applicationRef.get();
        if (application == null || Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH)
            return;
        application.unregisterActivityLifecycleCallbacks((Application.ActivityLifecycleCallbacks) callbacks);
    }

    private String getTraceSection(final Activity activity, final String section) {
        return String.format("%s/%s", activity.getClass().getSimpleName(), section);
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private Object getCallbacks() {
        return new Application.ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(final Activity activity, final Bundle savedInstanceState) {
                Log.d(TAG, "Activity %s created (savedInstanceState=%s)", activity.getClass().getSimpleName(), savedInstanceState);
                Trace.beginSection(getTraceSection(activity, "Created"));
            }

            @Override
            public void onActivityStarted(final Activity activity) {
                Log.d(TAG, "Activity %s started", activity.getClass().getSimpleName());
                Trace.beginSection(getTraceSection(activity, "Started"));
            }

            @Override
            public void onActivityResumed(final Activity activity) {
                Log.d(TAG, "Activity %s resumed", activity.getClass().getSimpleName());
                Trace.beginSection(getTraceSection(activity, "Resumed"));
            }

            @Override
            public void onActivityPaused(final Activity activity) {
                Log.d(TAG, "Activity %s paused", activity.getClass().getSimpleName());
                Trace.endSection();
            }

            @Override
            public void onActivityStopped(final Activity activity) {
                Log.d(TAG, "Activity %s stopped", activity.getClass().getSimpleName());
                Trace.endSection();
            }

            @Override
            public void onActivitySaveInstanceState(final Activity activity, final Bundle outState) {
                Log.d(TAG, "Activity %s save instance state (outState=%s)", activity.getClass().getSimpleName(), outState);
            }

            @Override
            public void onActivityDestroyed(final Activity activity) {
                Log.d(TAG, "Activity %s destroyed", activity.getClass().getSimpleName());
                Trace.endSection();
            }
        };
    }
}
