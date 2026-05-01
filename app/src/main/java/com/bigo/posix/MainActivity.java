package com.bigo.posix;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    // Load library native kita
    static {
        System.loadLibrary("native-lib");
    }

    // Deklarasi fungsi dari C++
    public native String stringFromJNI();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // UI Sederhana buat ngetes JNI
        TextView tv = new TextView(this);
        tv.setTextSize(24);
        tv.setPadding(50, 50, 50, 50);
        
        // Panggil fungsi C++ dan tampilkan di layar
        tv.setText(stringFromJNI());
        
        setContentView(tv);
    }
}
