package com.example.bigoguardian;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends Activity {
    static {
        // Load 7 Pasukan + Engine JNI
        try {
            System.loadLibrary("avutil");
            System.loadLibrary("swresample");
            System.loadLibrary("avcodec");
            System.loadLibrary("avformat");
            System.loadLibrary("swscale");
            System.loadLibrary("avfilter");
            System.loadLibrary("avdevice");
            System.loadLibrary("bigoguardian_engine");
        } catch (UnsatisfiedLinkError e) {
            android.util.Log.e("BIGO_TEST", "Gagal muat .so: " + e.getMessage());
        }
    }

    public native String testOtot();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TextView tv = new TextView(this);
        tv.setTextSize(25);
        tv.setPadding(20, 20, 20, 20);
        
        try {
            tv.setText(testOtot());
        } catch (Exception e) {
            tv.setText("❌ Gagal memanggil JNI: " + e.getMessage());
        }
        
        setContentView(tv);
    }
}
