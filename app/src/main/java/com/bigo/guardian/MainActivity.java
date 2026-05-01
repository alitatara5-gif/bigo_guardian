package com.bigo.guardian;

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
    // URUTAN SAKTI: avutil WAJIB sebelum avcodec/avformat
    String[] libs = {
        "c++_shared", "avutil", "swresample", "avcodec", 
        "avformat", "swscale", "avfilter", "avdevice", "bigoguardian_engine"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        txtStatus = findViewById(R.id.txtStatus);

        // 1 & 2. Logika Izin
        handlePermissions();
        
        // 3. Logika Konfirmasi 7 File .so
        checkEngine();
        
        // 4. Logika Mulai Rekaman
        setupUI();
    }

    private void handlePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {
            Intent it = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
            it.setData(Uri.parse("package:" + getPackageName()));
            startActivity(it);
        }
        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
        }
    }

    private void checkEngine() {
        StringBuilder sb = new StringBuilder("=== MONITOR MESIN ===\n");
        boolean ready = true;
        for (String lib : libs) {
            try {
                System.loadLibrary(lib);
                sb.append("✅ ").append(lib).append("\n");
            } catch (Throwable e) {
                sb.append("❌ ").append(lib).append(" (Missing)\n");
                ready = false;
            }
        }
        txtStatus.setText(sb.toString());
        txtStatus.setTextColor(ready ? 0xFF00FF00 : 0xFFFF0000);
    }

    private void setupUI() {
        Button btnStart = findViewById(R.id.btnStart);
        EditText input = findViewById(R.id.inputUrl);
        if (btnStart != null) {
            btnStart.setOnClickListener(v -> {
                String url = input.getText().toString();
                if (!url.isEmpty()) {
                    Intent it = new Intent(this, RecorderService.class);
                    it.putExtra("url", url);
                    startService(it);
                    Toast.makeText(this, "Rekaman Dimulai...", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
