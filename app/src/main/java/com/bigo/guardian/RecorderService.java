package com.bigo.guardian;

import android.app.*;
import android.content.Intent;
import android.os.*;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import java.io.File;

public class RecorderService extends Service {
    private static final String TAG = "BIGO_SERVICE";
    public static boolean isRecording = false;
    public static long startTime = 0;
    public static String currentFile = "";

    static {
        try {
            System.loadLibrary("c++_shared");
            
            Log.d(TAG, "Native libraries loaded successfully");
        } catch (Throwable e) {
            Log.e(TAG, "Native library load failed: " + e.getMessage());
        }
    }

    public native int startNativeRecording(String url, String savePath);
    public native void stopNativeRecording();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Service: onStartCommand dipanggil");
        String url = intent.getStringExtra("url");
        
        startTime = System.currentTimeMillis();
        isRecording = true;

        // Cek Folder
        File downloadDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "BigoGuardian");
        if (!downloadDir.exists()) {
            boolean created = downloadDir.mkdirs();
            Log.d(TAG, "Buat folder BigoGuardian: " + created);
        }

        currentFile = "Bigo_" + (startTime/1000) + ".mp4";
        String finalPath = new File(downloadDir, currentFile).getAbsolutePath();
        Log.d(TAG, "Jalur simpan: " + finalPath);

        startForeground(1, createNotification());

        new Thread(() -> {
            Log.d(TAG, "Thread: Memulai startNativeRecording...");
            try {
                int res = startNativeRecording(url, finalPath);
                Log.d(TAG, "Native Engine keluar dengan kode: " + res);
            } catch (Throwable e) {
                Log.e(TAG, "CRASH di Native Thread: " + e.getMessage());
            }
            isRecording = false;
            stopSelf();
        }).start();

        return START_NOT_STICKY;
    }

    private Notification createNotification() {
        String channelId = "recorder_channel";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel chan = new NotificationChannel(channelId, "Recorder", NotificationManager.IMPORTANCE_LOW);
            getSystemService(NotificationManager.class).createNotificationChannel(chan);
        }
        return new NotificationCompat.Builder(this, channelId)
                .setContentTitle("Bigo Guardian")
                .setContentText("Merekam: " + currentFile)
                .setSmallIcon(android.R.drawable.ic_media_play)
                .build();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "Service: onDestroy dipanggil (Stop Recording)");
        stopNativeRecording();
        isRecording = false;
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) { return null; }
}
