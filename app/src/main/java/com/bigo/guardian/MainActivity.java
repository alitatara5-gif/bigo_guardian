package com.bigo.guardian;

import android.app.Activity;
import android.content.*;
import android.os.*;
import android.view.View;
import android.widget.*;
import android.Manifest;
import android.content.pm.PackageManager;

public class MainActivity extends Activity {
    TextView txtStatus, listRekaman;
    Button btnStart, btnStop;
    EditText inputUrl;
    Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtStatus = findViewById(R.id.txtStatus);
        listRekaman = findViewById(R.id.listRekaman);
        btnStart = findViewById(R.id.btnStart);
        btnStop = findViewById(R.id.btnStop);
        inputUrl = findViewById(R.id.inputUrl);

        if (Build.VERSION.SDK_INT >= 33) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }

        checkEngine();
        syncUI();
        handler.post(durationUpdater);
    }

    private void syncUI() {
        if (RecorderService.isRecording) {
            btnStart.setVisibility(View.GONE);
            btnStop.setVisibility(View.VISIBLE);
        } else {
            btnStop.setVisibility(View.GONE);
            btnStart.setVisibility(View.VISIBLE);
        }
    }

    private final Runnable durationUpdater = new Runnable() {
        @Override
        public void run() {
            if (RecorderService.isRecording && RecorderService.startTime > 0) {
                long sec = (System.currentTimeMillis() - RecorderService.startTime) / 1000;
                String time = String.format("%02d:%02d:%02d", sec/3600, (sec%3600)/60, sec%60);
                listRekaman.setText("⏺️ LIVE: " + time + "\n📄 File: " + RecorderService.currentFile);
                if (btnStart.getVisibility() == View.VISIBLE) syncUI();
            }
            handler.postDelayed(this, 1000);
        }
    };

    private void checkEngine() {
        String[] libs = {"c++_shared", "avutil", "swresample", "avcodec", "avformat", "swscale", "avfilter", "avdevice", "bigoguardian_engine"};
        StringBuilder sb = new StringBuilder("=== KONFIRMASI MESIN ===\n");
        for (String lib : libs) {
            try { System.loadLibrary(lib); sb.append("✅ ").append(lib).append("\n"); }
            catch (Throwable e) { sb.append("❌ ").append(lib).append("\n"); }
        }
        txtStatus.setText(sb.toString());
    }

    private void startRecording() {
        String url = inputUrl.getText().toString();
        if (url.isEmpty()) return;
        btnStart.setVisibility(View.GONE);
        btnStop.setVisibility(View.VISIBLE);
        Intent it = new Intent(this, RecorderService.class);
        it.putExtra("url", url);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(it);
        } else {
            startService(it);
        }
    }

    private void stopRecording() {
        stopService(new Intent(this, RecorderService.class));
        btnStop.setVisibility(View.GONE);
        btnStart.setVisibility(View.VISIBLE);
        listRekaman.setText("⏹️ Rekaman Selesai.");
    }
}
