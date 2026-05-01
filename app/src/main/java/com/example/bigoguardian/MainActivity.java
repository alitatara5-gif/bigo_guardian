package com.example.bigoguardian;

import android.app.Activity;
import android.content.*;
import android.net.Uri;
import android.os.*;
import android.provider.Settings;
import android.widget.*;
import android.Manifest;
import android.content.pm.PackageManager;
import android.util.Log;

public class MainActivity extends Activity {
    TextView txtStatus;
    // Daftar ini harus sama persis dengan yang ada di CMakeLists.txt
    String[] libs = {"avutil", "swresample", "avcodec", "avformat", "swscale", "avfilter", "avdevice", "bigoguardian_engine"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        txtStatus = findViewById(R.id.txtStatus);

        // 1. CEK IJIN STORAGE KHUSUS ANDROID 11-14
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivity(intent);
                Toast.makeText(this, "Aktifkan Izin Akses Semua File!", Toast.LENGTH_LONG).show();
            }
        }

        // 2. PROSES LOADING MESIN (Cek satu per satu)
        StringBuilder sb = new StringBuilder("--- MONITORING MESIN ---\n");
        boolean ready = true;

        for (String lib : libs) {
            try {
                System.loadLibrary(lib);
                sb.append("✅ ").append(lib).append(" : READY\n");
            } catch (UnsatisfiedLinkError e) {
                sb.append("❌ ").append(lib).append(" : FAILED\n");
                Log.e("BigoGuardian", "Gagal load: " + lib + " | Error: " + e.getMessage());
                ready = false;
            }
        }
        
        txtStatus.setText(sb.toString());

        if (ready) {
            txtStatus.setTextColor(0xFF00FF00); // Hijau Stabilo
            setupUI();
        } else {
            txtStatus.setTextColor(0xFFFF0000); // Merah Darah
            Toast.makeText(this, "MESIN TIDAK LENGKAP! Cek jniLibs di GitHub", Toast.LENGTH_LONG).show();
        }
    }

    private void setupUI() {
        EditText input = findViewById(R.id.inputUrl);
        findViewById(R.id.btnStart).setOnClickListener(v -> {
            String url = input.getText().toString();
            if (!url.isEmpty()) {
                Intent it = new Intent(this, RecorderService.class);
                it.putExtra("id", (int)(System.currentTimeMillis() % 10000));
                it.putExtra("url", url);
                startService(it);
                Toast.makeText(this, "Perintah Rekam Dikirim...", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Masukkan URL dulu Bang!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
