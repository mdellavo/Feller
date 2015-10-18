package org.quuux.fellerdemo;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import org.quuux.feller.AppMonitor;
import org.quuux.feller.Log;

import static org.quuux.feller.Log.AUTO;

public class MainActivity extends AppCompatActivity {

    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AppMonitor.getInstance().attach(getApplication());

        Log.d(AUTO(), "hello %s", "world");
    }

    @Override
    protected void onResume() {
        super.onResume();

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                throw new RuntimeException("goodbye world");
            }
        }, 1000);
    }
}
