package org.quuux.fellerdemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import org.quuux.feller.Log;
import org.quuux.feller.Trace;

import static org.quuux.feller.Log.AUTO;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(AUTO(), "hello %s", "world");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Trace.beginSection("foo");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Trace.endSection();
    }
}
