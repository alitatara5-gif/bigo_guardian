package com.bigo.guardian;

import android.app.*;
import android.content.Intent;
import android.os.*;
import java.io.File;

public class RecorderService extends Service {
    public static long startTime = 0;
    private boolean isRunning = false;

    static {
        System.loadLibrary("c++_shared");
        System.loadLibrary("avutil");
        System.loadLibrary("avcodec");
        System.loadLibrary("avformat");
        System.loadLibrary("bigoguardian_engine");
    }

    public native int startNativeRecording(String url, String savePath);
    public native void stopNativeRecording();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String url = intent.getStringExtra("url");
        startTime = System.currentTimeMillis();
        isRunning = true;

        File downloadDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "BigoGuardian");
        if (!downloadDir.exists()) downloadDir.mkdirs();

        String finalPath = new File(downloadDir, "Bigo_" + startTime + ".mp4").getAbsolutePath();

        new Thread(() -> {
            startNativeRecording(url, finalPath);
        }).start();

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        stopNativeRecording();
        isRunning = false;
        startTime = 0;
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) { return null; }
}
