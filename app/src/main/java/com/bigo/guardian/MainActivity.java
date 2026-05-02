package com.bigo.guardian;

import android.app.Activity;
import android.content.Intent;
import android.os.*;
import android.view.View;
import android.widget.*;

public class MainActivity extends Activity {
    Button btnStart, btnStop;
    EditText inputUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnStart = findViewById(R.id.btnStart);
        btnStop = findViewById(R.id.btnStop);
        inputUrl = findViewById(R.id.inputUrl);

        btnStart.setOnClickListener(v -> {
            String url = inputUrl.getText().toString();
            if (url.isEmpty()) return;
            Intent it = new Intent(this, RecorderService.class);
            it.putExtra("url", url);
            startForegroundService(it);
            btnStart.setVisibility(View.GONE);
            btnStop.setVisibility(View.VISIBLE);
        });

        btnStop.setOnClickListener(v -> {
            stopService(new Intent(this, RecorderService.class));
            btnStop.setVisibility(View.GONE);
            btnStart.setVisibility(View.VISIBLE);
        });
    }
}
