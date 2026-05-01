package com.bigo.guardian;

import android.app.Activity;
import android.content.*;
import android.os.*;
import android.view.View;
import android.widget.*;

public class MainActivity extends Activity {
    TextView txtStatus, listRekaman;
    Button btnStart, btnStop;
    EditText inputUrl;
    Handler handler = new Handler();
    
    String[] libs = {"c++_shared", "avutil", "swresample", "avcodec", "avformat", "swscale", "avfilter", "avdevice", "bigoguardian_engine"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtStatus = findViewById(R.id.txtStatus);
        listRekaman = findViewById(R.id.listRekaman);
        btnStart = findViewById(R.id.btnStart);
        btnStop = findViewById(R.id.btnStop);
        inputUrl = findViewById(R.id.inputUrl);

        checkEngine();

        btnStart.setOnClickListener(v -> startRecording());
        btnStop.setOnClickListener(v -> stopRecording());

        // Update Durasi Real-time setiap 1 detik
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                updateDurationUI();
                handler.postDelayed(this, 1000);
            }
        }, 1000);
    }

    private void checkEngine() {
        StringBuilder sb = new StringBuilder("=== KONFIRMASI MESIN ===\n");
        for (String lib : libs) {
            try {
                System.loadLibrary(lib);
                sb.append("✅ ").append(lib).append("\n");
            } catch (Throwable e) {
                sb.append("❌ ").append(lib).append(" (ERROR)\n");
            }
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
        startService(it);
        
        listRekaman.setText("⏺️ Sedang merekam stream...");
    }

    private void stopRecording() {
        btnStop.setVisibility(View.GONE);
        btnStart.setVisibility(View.VISIBLE);
        
        stopService(new Intent(this, RecorderService.class));
        listRekaman.setText("⏹️ Rekaman dihentikan.");
    }

    private void updateDurationUI() {
        // Logika mengambil durasi dari Service atau Engine
        if (btnStop.getVisibility() == View.VISIBLE) {
            long sec = (System.currentTimeMillis() - RecorderService.startTime) / 1000;
            String time = String.format("%02d:%02d:%02d", sec/3600, (sec%3600)/60, sec%60);
            listRekaman.setText("⏺️ LIVE: " + time + "\n📁 Menyimpan ke Download/BigoGuardian");
        }
    }
}
