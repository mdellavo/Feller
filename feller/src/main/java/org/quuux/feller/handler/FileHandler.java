package org.quuux.feller.handler;

import org.quuux.feller.Log;
import org.quuux.feller.util.LineWriter;

import java.io.File;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class FileHandler implements Log.LogHandler {

    private static final String TAG = "FileHandler";

    private final BlockingQueue<Log.LogEntry> queue = new LinkedBlockingQueue<>();
    private final File logPath;
    private final String terminator;

    private Thread writerThread = null;
    private String separator;
    private LogWriter writer;

    public FileHandler(final String separator, final String terminator, final File logPath) {
        this.separator = separator;
        this.terminator = terminator;
        this.logPath = logPath;
    }

    public FileHandler(final File logPath) {
        this(" ", "\n", logPath);
    }

    @Override
    public void start() {
        writer = new LogWriter(logPath, queue);
        writerThread = new Thread(writer);
        writerThread.start();
    }

    @Override
    public void stop() {
        writer.stop();
        try {
            writerThread.join();
        } catch (InterruptedException e) {
            Log.e(TAG, "error stopping writer thread", e);
        }
    }

    @Override
    public void println(final Log.LogEntry entry) {
        queue.offer(entry);
    }

    private class LogWriter extends LineWriter<Log.LogEntry> {

        final StringBuilder buffer = new StringBuilder();

        public LogWriter(final File path, final BlockingQueue<Log.LogEntry> queue) {
            super(path, queue);
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

        protected String write(final Log.LogEntry entry) {
            buffer.setLength(0);

            buffer.append(getTimestamp(entry.timestamp));
            buffer.append(separator);
            buffer.append(getPriority(entry));
            buffer.append(separator);
            buffer.append(entry.tag);
            buffer.append(separator);
            buffer.append(getMessage(entry));
            buffer.append(terminator);
            return buffer.toString();
        }

        private String getMessage(final Log.LogEntry entry) {
            return entry.throwable != null ? entry.message + "\n" + Log.getStackTraceString(entry.throwable) : entry.message;
        }

        @Override
        protected void recycle(final Log.LogEntry entry) {
            super.recycle(entry);
            Log.recycleEntry(entry);
        }
    }
}
