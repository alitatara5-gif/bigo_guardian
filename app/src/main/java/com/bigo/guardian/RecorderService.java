package com.bigo.guardian;

import android.app.*;
import android.content.Intent;
import android.os.*;
import androidx.core.app.NotificationCompat;
import java.io.File;

public class RecorderService extends Service {
    // VARIABEL KRUSIAL UNTUK SINKRONISASI UI
    public static long startTime = 0;
    public static boolean isRecording = false;
    public static String currentFile = "";

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
        if (url == null || url.isEmpty()) return START_NOT_STICKY;

        startTime = System.currentTimeMillis();
        isRecording = true;

        File downloadDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "BigoGuardian");
        if (!downloadDir.exists()) downloadDir.mkdirs();
        
        currentFile = "Bigo_" + (startTime/1000) + ".mp4";
        String finalPath = new File(downloadDir, currentFile).getAbsolutePath();

        startForeground(1, createNotification());

        new Thread(() -> {
            startNativeRecording(url, finalPath);
            isRecording = false;
            stopForeground(true);
            stopSelf();
        }).start();

        return START_STICKY;
    }

    private Notification createNotification() {
        String channelId = "recorder";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "Bigo Recorder", NotificationManager.IMPORTANCE_LOW);
            getSystemService(NotificationManager.class).createNotificationChannel(channel);
        }
        return new NotificationCompat.Builder(this, channelId)
                .setContentTitle("Bigo Guardian Aktif")
                .setContentText("Sedang merekam stream...")
                .setSmallIcon(android.R.drawable.ic_media_play)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();
    }

    @Override
    public void onDestroy() {
        stopNativeRecording();
        isRecording = false;
        startTime = 0;
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) { return null; }
}
