package com.example.bigoguardian;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
public class MainActivity extends AppCompatActivity {
    static {
        System.loadLibrary("avutil"); System.loadLibrary("swresample");
        System.loadLibrary("avcodec"); System.loadLibrary("avformat");
        System.loadLibrary("swscale"); System.loadLibrary("avfilter");
        System.loadLibrary("avdevice"); System.loadLibrary("bigoguardian_engine");
    }
    public native String testOtot();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TextView tv = new TextView(this);
        tv.setTextSize(25);
        try { tv.setText(testOtot()); } catch (Exception e) { tv.setText("❌ Gagal"); }
        setContentView(tv);
    }
}
