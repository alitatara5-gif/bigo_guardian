package com.bigo.guardian;

import android.app.Activity;
import android.content.*;
import android.os.*;
import android.view.View;
import android.widget.*;
import com.arthenica.ffmpegkit.FFmpegKitConfig;

public class MainActivity extends Activity {
    TextView txtStatus, listRekaman;
    Button btnStart, btnStop;
    EditText inputUrl;
    
    String[] libs = {"c++_shared", "avutil", "swresample", "avcodec", "avformat", "swscale", "avfilter", "avdevice", "ffmpegkit_abidetect", "ffmpegkit"};

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

        btnStart.setOnClickListener(v -> {
            String url = inputUrl.getText().toString();
            if(url.isEmpty()) return;
            
            Intent it = new Intent(this, RecorderService.class);
            it.putExtra("url", url);
            startForegroundService(it);
            
            btnStart.setVisibility(View.GONE);
            btnStop.setVisibility(View.VISIBLE);
            listRekaman.setText("⏺️ FFmpegKit Aktif...");
        });

        btnStop.setOnClickListener(v -> {
            stopService(new Intent(this, RecorderService.class));
            btnStop.setVisibility(View.GONE);
            btnStart.setVisibility(View.VISIBLE);
            listRekaman.setText("⏹️ Rekaman Berhenti.");
        });
    }

    private void checkEngine() {
        StringBuilder sb = new StringBuilder("=== MONITOR FFMPEGKIT ===\n");
        for (String lib : libs) {
            try {
                System.loadLibrary(lib);
                sb.append("✅ ").append(lib).append("\n");
            } catch (Throwable e) {
                sb.append("❌ ").append(lib).append("\n");
            }
        }
        txtStatus.setText(sb.toString());
    }
}
