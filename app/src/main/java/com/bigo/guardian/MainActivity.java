package com.bigo.guardian;

import android.app.Activity;
import android.content.*;
import android.os.*;
import android.util.Log;
import android.view.View;
import android.widget.*;
import android.Manifest;
import android.content.pm.PackageManager;

public class MainActivity extends Activity {
    TextView txtStatus, listRekaman;
    Button btnStart, btnStop;
    EditText inputUrl;

    // Daftar 10 Library FFmpegKit
    String[] libs = {
        "c++_shared", "avutil", "swresample", "avcodec", 
        "avformat", "swscale", "avfilter", "avdevice", 
        "ffmpegkit_abidetect", "ffmpegkit"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtStatus = findViewById(R.id.txtStatus);
        listRekaman = findViewById(R.id.listRekaman);
        btnStart = findViewById(R.id.btnStart);
        btnStop = findViewById(R.id.btnStop);
        inputUrl = findViewById(R.id.inputUrl);

        // Cek Izin Notifikasi (Android 13+)
        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
        }

        checkEngine();

        btnStart.setOnClickListener(v -> startRecording());
        btnStop.setOnClickListener(v -> stopService(new Intent(this, RecorderService.class)));
    }

    private void checkEngine() {
        StringBuilder sb = new StringBuilder("=== MONITOR FFMPEG KIT ===\n");
        boolean ready = true;
        for (String lib : libs) {
            try {
                System.loadLibrary(lib);
                sb.append("✅ ").append(lib).append("\n");
            } catch (Throwable e) {
                sb.append("❌ ").append(lib).append("\n");
                ready = false;
            }
        }
        txtStatus.setText(sb.toString());
        txtStatus.setTextColor(ready ? 0xFF00FF00 : 0xFFFF0000);
    }

    private void startRecording() {
        String url = inputUrl.getText().toString();
        if (url.isEmpty()) return;

        Intent it = new Intent(this, RecorderService.class);
        it.putExtra("url", url);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(it);
        } else {
            startService(it);
        }
        btnStart.setVisibility(View.GONE);
        btnStop.setVisibility(View.VISIBLE);
        listRekaman.setText("⏺️ FFmpegKit Sedang Merekam...");
    }
}
