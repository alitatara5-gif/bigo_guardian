package com.example.bigoguardian;

import android.app.Activity;
import android.content.*;
import android.os.*;
import android.widget.*;
import android.Manifest;
import android.content.pm.PackageManager;
import android.provider.Settings;
import android.net.Uri;

public class MainActivity extends Activity {
    TextView txtStatus;
    String[] libs = {"avutil", "swresample", "avcodec", "avformat", "swscale", "avfilter", "avdevice", "bigoguardian_engine"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        txtStatus = findViewById(R.id.txtStatus);

        // 1. Todong Izin Notifikasi & Video
        if (Build.VERSION.SDK_INT >= 33) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{
                    Manifest.permission.POST_NOTIFICATIONS,
                    Manifest.permission.READ_MEDIA_VIDEO
                }, 101);
            }
        }

        // 2. Akses Semua File (Kunci FFmpeg)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivity(intent);
            }
        }

        checkEngine();
        setupUI();
    }

    private void checkEngine() {
        StringBuilder sb = new StringBuilder("=== MONITOR MESIN ===\n");
        boolean ready = true;
        
        try { System.loadLibrary("c++_shared"); } catch (Throwable t) {}

        for (String lib : libs) {
            try {
                System.loadLibrary(lib);
                sb.append("✅ ").append(lib).append("\n");
            } catch (Throwable e) {
                sb.append("❌ ").append(lib).append("\n");
                ready = false;
            }
        }
        
        if (txtStatus != null) {
            txtStatus.setText(sb.toString());
            txtStatus.setTextColor(ready ? 0xFF00FF00 : 0xFFFF0000);
        }
    }

    private void setupUI() {
        EditText input = findViewById(R.id.inputUrl);
        Button btnStart = findViewById(R.id.btnStart);
        if (btnStart != null) {
            btnStart.setOnClickListener(v -> {
                String url = input.getText().toString();
                if (!url.isEmpty()) {
                    Intent it = new Intent(this, RecorderService.class);
                    it.putExtra("id", (int)(System.currentTimeMillis() % 10000));
                    it.putExtra("url", url);
                    startService(it);
                    Toast.makeText(this, "Mencoba merekam...", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
