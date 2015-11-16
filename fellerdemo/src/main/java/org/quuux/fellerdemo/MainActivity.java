package org.quuux.fellerdemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import org.quuux.feller.Log;
import org.quuux.feller.Trace;
import org.quuux.feller.handler.DefaultHandler;
import org.quuux.feller.handler.FileHandler;

import java.io.File;

import static org.quuux.feller.Log.AUTO;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.setHandlers(new DefaultHandler(), new FileHandler(new File(getExternalCacheDir(), "app.log")));
        Trace.setTraceFile(new File(getExternalCacheDir(), "trace.log"));
        Log.d(AUTO(), "hello %s", "world");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Trace.beginSection("main");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Trace.endSection();
    }
}
