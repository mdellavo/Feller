package org.quuux.fellerdemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import org.quuux.feller.AppMonitor;
import org.quuux.feller.Log;

import java.lang.RuntimeException;

import static org.quuux.feller.Log.AUTO;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AppMonitor.getInstance().attach(getApplication());

        Log.d(AUTO(), "helo %s", "world");

        throw new RuntimeException("goodbye world");
    }
}
