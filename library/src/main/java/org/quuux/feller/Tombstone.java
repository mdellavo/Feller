package org.quuux.feller;


import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Tombstone {

    private static final String TAG = Log.buildTag(Tombstone.class);

    private static ApplicationInfo app;

    public final String uid;
    public final DeviceInfo device;
    public final ThreadInfo thread;
    public final ExceptionInfo exception;

    public Tombstone(final String uid, final DeviceInfo deviceInfo, final ThreadInfo threadInfo, final ExceptionInfo exceptionInfo) {
        this.uid = uid;
        this.device = deviceInfo;
        this.thread = threadInfo;
        this.exception = exceptionInfo;
    }

    public static void setApplicationInfo(final Context context) {
        Tombstone.app = getApplicationInfo(context);
    }

    public static class DeviceInfo {
        public int api = Build.VERSION.SDK_INT;
        public String device = Build.DEVICE;
        public String model = Build.MODEL;
        public String product = Build.PRODUCT;
    }

    public static class ApplicationInfo {
        public String packageName;
        public int versionCode;
        public String versionName;
    }

    public static class ThreadInfo {
        public String name;
        public long id;
    }

    public static class ExceptionInfo {
        public List<StackTraceInfo> stacktrace = new ArrayList<>();
        public String className;
        public String message;
    }

    public static class StackTraceInfo {
        public String className;
        public String methodName;
        public int lineNumber;
        public String filename;
        public boolean isNative;
    }

    public static Tombstone build(final String uuid, Thread thread, Throwable ex) {
        return new Tombstone(uuid, getDeviceInfo(), getThreadInfo(thread), getExceptionInfo(ex));
    }

    private static ExceptionInfo getExceptionInfo(final Throwable ex) {
        final ExceptionInfo exceptionInfo = new ExceptionInfo();

        exceptionInfo.message = ex.getMessage();
        exceptionInfo.className = ex.getClass().getName();

        for (StackTraceElement element : ex.getStackTrace()) {
            final StackTraceInfo traceInfo = new StackTraceInfo();
            traceInfo.className = element.getClassName();
            traceInfo.filename = element.getFileName();
            traceInfo.methodName = element.getMethodName();
            traceInfo.lineNumber = element.getLineNumber();
            traceInfo.isNative = element.isNativeMethod();
            exceptionInfo.stacktrace.add(traceInfo);
        }

        return exceptionInfo;
    }

    private static ThreadInfo getThreadInfo(final Thread thread) {
        final ThreadInfo threadInfo = new ThreadInfo();
        threadInfo.name = thread.getName();
        threadInfo.id = thread.getId();
        return threadInfo;
    }

    public static ApplicationInfo getApplicationInfo(final Context context) {
        final ApplicationInfo appInfo = new ApplicationInfo();
        appInfo.packageName = context.getPackageName();

        try {
            final PackageInfo packageInfo = context.getPackageManager().getPackageInfo(appInfo.packageName, 0);
            appInfo.versionCode = packageInfo.versionCode;
            appInfo.versionName = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "could not get device information: %s", e.getMessage(), e);
        }
        return appInfo;
    }

    public static DeviceInfo getDeviceInfo() {
        return new DeviceInfo();
    }
}
