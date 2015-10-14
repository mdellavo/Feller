package org.quuux.feller.handler;

import org.quuux.feller.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.FieldPosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class FileHandler implements Log.LogHandler {

    final private BlockingQueue<Log.LogEntry> queue = new LinkedBlockingQueue<>();
    final private File logPath;

    private boolean isWriting;
    private Thread writerThread = null;

    public FileHandler(final File logPath) {
        this.logPath = logPath;
    }

    @Override
    public void start() {
        isWriting = true;
        writerThread = new Thread(new LogWriter());
        writerThread.start();
    }

    @Override
    public void stop() {
        isWriting = false;
        try {
            writerThread.join();
        } catch (InterruptedException e) {
            Log.e("Log", "error stopping writer thread", e);
        }
    }

    @Override
    public void println(final Log.LogEntry entry) {
        try {
            queue.put(entry);
        } catch (InterruptedException e) {
            android.util.Log.e("Log", "error queuing entry, dropping message!!!");
        }
    }

    private class LogWriter implements Runnable {

        final StringBuilder buffer = new StringBuilder();
        final StringBuffer timestampBuffer = new StringBuffer();
        final FieldPosition fieldPosition = new FieldPosition(0);
        final Date timestamp = new Date();
        final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

        private BufferedWriter open() {
            try {
                return new BufferedWriter(new FileWriter(logPath));
            } catch (IOException e) {
                android.util.Log.e("LogWriter", "could not open log for writing: " + logPath, e);
            }
            return null;
        }

        private String getTimestamp(final Log.LogEntry entry) {
            timestampBuffer.setLength(0);
            timestamp.setTime(entry.timestamp);
            return dateFormat.format(timestamp, timestampBuffer, fieldPosition).toString();
        }

        private String getPriority(final Log.LogEntry entry) {
            final String rv;
            switch (entry.priority) {

                case android.util.Log.VERBOSE:
                    rv = "Verbose";
                    break;

                case android.util.Log.INFO:
                    rv = "Info";
                    break;

                case android.util.Log.DEBUG:
                    rv = "Debug";
                    break;

                case android.util.Log.WARN:
                    rv = "Warn";
                    break;

                case android.util.Log.ERROR:
                    rv = "Error";
                    break;

                case android.util.Log.ASSERT:
                    rv = "Assert";
                    break;

                default:
                    rv = "(UNKNOWN)";
                    break;
            }

            return rv;
        }

        private void write(final BufferedWriter writer, final Log.LogEntry entry) throws IOException {
            buffer.setLength(0);

            buffer.append("[");
            buffer.append(getTimestamp(entry));
            buffer.append("   ");
            buffer.append(getPriority(entry));
            buffer.append("/");
            buffer.append(entry.tag);
            buffer.append("] ");
            buffer.append(getMessage(entry));

            writer.write(buffer.toString());
        }

        private String getMessage(final Log.LogEntry entry) {
            return entry.throwable != null ? entry.message + "\n" + Log.getStackTraceString(entry.throwable) : entry.message;
        }

        @Override
        public void run() {
            final BufferedWriter writer = open();
            if (writer == null) {
                return;
            }

            while (isWriting) {
                try {
                    final Log.LogEntry entry = queue.take();
                    write(writer, entry);
                    Log.recycleEntry(entry);
                } catch (InterruptedException e) {
                    android.util.Log.e("LogWriter", "error taking log entry for writing" + e);
                } catch (IOException e) {
                    android.util.Log.e("LogWriter", "error writing log message" + e);
                }
            }
        }
    };
}
