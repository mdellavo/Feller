package org.quuux.feller.util;


import org.quuux.feller.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.FieldPosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

abstract public class LineWriter<T> implements Runnable {

    private static final String TAG = Log.buildTag(LineWriter.class);
    private final File path;
    private final BlockingQueue<T> queue;

    private boolean isWriting;

    final StringBuffer timestampBuffer = new StringBuffer();
    final FieldPosition fieldPosition = new FieldPosition(0);
    final Date timestamp = new Date();
    final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSS", Locale.getDefault());

    public LineWriter(final File path, final BlockingQueue<T> queue) {
        this.path = path;
        this.queue = queue;
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    public Writer open() {

        final File parent = path.getParentFile();
        if (!parent.exists()) {
            final boolean rv = parent.mkdirs();
            if (!rv)
                Log.e(TAG, "could not create log dir: " + parent);
        }

        try {
            return new BufferedWriter(new FileWriter(path));
        } catch (IOException e) {
            Log.e(TAG, "could not open log for writing: " + path, e);
        }
        return null;
    }

    public void stop() {
        isWriting = false;
    }

    @Override
    public void run() {
        final Writer writer = open();
        if (writer == null) {
            return;
        }

        isWriting = true;

        final List<T> entries = new ArrayList<>();
        while (isWriting || queue.peek() != null) {
            try {
                entries.clear();
                T entry =  queue.poll(100, TimeUnit.MILLISECONDS);
                if (entry == null)
                    continue;
                entries.add(entry);
                queue.drainTo(entries);

                for (int i=0; i<entries.size(); i++) {
                    entry = entries.get(i);
                    writer.write(write(entry));
                }

                writer.flush();

            } catch (InterruptedException e) {
                android.util.Log.e(TAG, "error taking log entry for writing" + e);
            } catch (IOException e) {
                android.util.Log.e(TAG, "error writing log message" + e);
            }
        }

        try {
            writer.close();
        } catch (IOException e) {
            android.util.Log.e(TAG, "error closing log" + e);
        }
    }

    protected abstract String write(final T entry);
    protected void recycle(final T entry) {
    }

    public String getTimestamp(final long millis) {
        timestampBuffer.setLength(0);
        timestamp.setTime(millis);
        return dateFormat.format(timestamp, timestampBuffer, fieldPosition).toString();
    }

}
