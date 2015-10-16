package org.quuux.feller.watcher;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Application;
import android.os.Build;
import android.os.Bundle;

import org.quuux.feller.Log;

import java.lang.ref.WeakReference;

public class ActivityWatcher implements Log.Watcher {

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

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private Object getCallbacks() {
        return new Application.ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(final Activity activity, final Bundle savedInstanceState) {
                Log.d(TAG, "Activity %s created (savedInstanceState=%s)", activity.getClass().getSimpleName(), savedInstanceState);
            }

            @Override
            public void onActivityStarted(final Activity activity) {
                Log.d(TAG, "Activity %s started", activity.getClass().getSimpleName());
            }

            @Override
            public void onActivityResumed(final Activity activity) {
                Log.d(TAG, "Activity %s resumed", activity.getClass().getSimpleName());
            }

            @Override
            public void onActivityPaused(final Activity activity) {
                Log.d(TAG, "Activity %s paused", activity.getClass().getSimpleName());
            }

            @Override
            public void onActivityStopped(final Activity activity) {
                Log.d(TAG, "Activity %s stopped", activity.getClass().getSimpleName());
            }

            @Override
            public void onActivitySaveInstanceState(final Activity activity, final Bundle outState) {
                Log.d(TAG, "Activity %s save instance state (outState=%s)", activity.getClass().getSimpleName(), outState);
            }

            @Override
            public void onActivityDestroyed(final Activity activity) {
                Log.d(TAG, "Activity %s destroyed", activity.getClass().getSimpleName());
            }
        };
    }
}
