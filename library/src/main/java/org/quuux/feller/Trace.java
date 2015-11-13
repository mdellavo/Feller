package org.quuux.feller;

import android.os.Build;

import org.quuux.feller.util.LineWriter;

import java.io.File;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Trace {

    private static final String TAG = Log.buildTag(Trace.class);

    public static final int POOL_SIZE = 1024;
    private static final int MAX_TRACE_DEPTH = 32;

    public static class TraceRecord {
        String name;
        long start, end;

        TraceRecord() {}
        TraceRecord(final TraceRecord other) {
            copy(other);
        }

        void recycle() {
            name = null;
            start = end = 0;
        }

        void begin(final String name) {
            this.name = name;
            start = System.currentTimeMillis();
        }

        void end() {
            end = System.currentTimeMillis();
        }

        void copy(TraceRecord other) {
            name = other.name;
            start = other.start;
            end = other.end;
        }

        long elapsed() {
            return end - start;
        }
    }

    static int depth = -1;
    static TraceRecord traces[] = new TraceRecord[MAX_TRACE_DEPTH];

    private static String separator = " ";
    private static String terminator = "\n";
    private static final BlockingQueue<TraceRecord> queue = new LinkedBlockingQueue<>();
    private static BlockingQueue<TraceRecord> pool = new ArrayBlockingQueue<>(POOL_SIZE, true);
    private static TraceWriter writer;
    private static Thread writerThread;

    static {
        for (int i=0; i<traces.length; i++) {
            traces[i] = new TraceRecord();
        }

        for (int i = 0; i < pool.remainingCapacity(); i++)
            pool.add(new TraceRecord());
    }

    public static void setTraceFile(final File path) {
        writer = new TraceWriter(path, queue);
        writerThread = new Thread(writer);
        writerThread.start();
    }

    public static void stop() {
        if (writer != null) {
            writer.stop();
            try {
                writerThread.join();
            } catch (InterruptedException e) {
                Log.e(TAG, "error stopping writer thread", e);
            }
        }
    }

    private static boolean hasSysTrace() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2;
    }

    public static void beginSection(final String sectionName) {
        if (hasSysTrace())
            android.os.Trace.beginSection(sectionName);
        depth++;
        traces[depth].begin(sectionName);
    }

    public static void endSection() {
        if (hasSysTrace())
            android.os.Trace.endSection();
        if (depth >= 0) {
            traces[depth].end();
            logTrace(traces[depth]);
            commitTrace(traces[depth]);
            traces[depth].recycle();
            depth--;
        }
    }

    private static void commitTrace(final TraceRecord trace) {
        try {
            final TraceRecord entry = pool.take();
            entry.copy(trace);
            queue.put(entry);
        } catch (InterruptedException e) {
            Log.e(TAG, "error committing trace", e);
        }
    }

    private static void logTrace(final TraceRecord trace) {
        Log.d(TAG, "Trace %s completed in %sms", trace.name, trace.elapsed());
    }

    static class TraceWriter extends LineWriter<TraceRecord> {
        final StringBuilder buffer = new StringBuilder();

        public TraceWriter(final File path, final BlockingQueue<TraceRecord> queue) {
            super(path, queue);
        }

        @Override
        protected String write(final TraceRecord entry) {
            buffer.setLength(0);
            buffer.append(entry.name);
            buffer.append(separator);
            buffer.append(getTimestamp(entry.start));
            buffer.append(separator);
            buffer.append(getTimestamp(entry.end));
            buffer.append(terminator);
            return buffer.toString();
        }

        @Override
        protected void recycle(final TraceRecord entry) {
            super.recycle(entry);
            entry.recycle();
            try {
                pool.put(entry);
            } catch (InterruptedException e) {
                Log.e(TAG, "error recycling trace", e);
            }
        }
    }
}
