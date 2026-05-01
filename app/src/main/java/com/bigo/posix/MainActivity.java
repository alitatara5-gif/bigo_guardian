package com.bigo.posix;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    static {
        System.loadLibrary("native-lib");
    }

    public native String stringFromJNI();
    public native int startRecording(String url, String output);
    public native void stopRecording();

    private TextView tvStatus;

    // Fungsi ini bakal dipanggil dari C++ buat ngirim pesan ke layar
    public void updateStatusFromNative(final String message) {
        runOnUiThread(() -> {
            if (tvStatus != null) {
                tvStatus.append("\n" + message);
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 40, 40, 40);

        tvStatus = new TextView(this);
        tvStatus.setText("Engine: " + stringFromJNI() + "\n--- LOG ---");

        final EditText etUrl = new EditText(this);
        etUrl.setHint("Masukkan URL m3u8...");
        layout.addView(etUrl);

        Button btnStart = new Button(this);
        btnStart.setText("MULAI REKAM");
        layout.addView(btnStart);

        Button btnStop = new Button(this);
        btnStop.setText("STOP");
        layout.addView(btnStop);

        ScrollView scroller = new ScrollView(this);
        scroller.addView(tvStatus);
        layout.addView(scroller);

        setContentView(layout);

        btnStart.setOnClickListener(v -> {
            String url = etUrl.getText().toString().trim();
            // Simpan ke folder internal biar PASTI berhasil (gak butuh izin storage)
            File internalDir = getExternalFilesDir(null);
            String path = new File(internalDir, "live_" + System.currentTimeMillis() + ".mp4").getAbsolutePath();
            
            updateStatusFromNative("[Info] Path: " + path);
            startRecording(url, path);
        });

        btnStop.setOnClickListener(v -> stopRecording());
    }
}
