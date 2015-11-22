package org.quuux.feller;

import android.app.Application;
import android.os.Environment;
import android.test.ApplicationTestCase;

import org.quuux.feller.handler.DefaultHandler;
import org.quuux.feller.handler.FileHandler;

import java.io.File;

public class ApplicationTest extends ApplicationTestCase<Application> {
    public ApplicationTest() {
        super(Application.class);
    }

    public void testDefaultHandler() {
        Log.setHandlers(new DefaultHandler());
        Log.d(Log.AUTO(), "hello world");
    }

    public void testAutoTagger() {
        Log.setHandlers(new DefaultHandler());
        assertEquals("ApplicationTest.testAutoTagger", Log.AUTO());
    }

    public void testFileHandler() {
        final File path = new File(Environment.getExternalStorageDirectory(), "test.log");
        Log.setHandlers(new FileHandler(path));

        for (int i = 0; i < 10; i++) {
            Log.d(Log.AUTO(), "hello world: i=%s", i, new Throwable("hi" + i));
        }

        Log.shutdown();
    }

}