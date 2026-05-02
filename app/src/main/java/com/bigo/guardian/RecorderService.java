package com.bigo.guardian;

import android.app.*;
import android.content.Intent;
import android.os.*;
import androidx.core.app.NotificationCompat;
import com.arthenica.ffmpegkit.FFmpegKitConfig;
import java.io.File;

public class RecorderService extends Service {
    public static boolean isRecording = false;
    public static long startTime = 0;
    public static String currentFile = "";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String url = intent.getStringExtra("url");
        startTime = System.currentTimeMillis();
        isRecording = true;

        File downloadDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "BigoGuardian");
        if (!downloadDir.exists()) downloadDir.mkdirs();

        currentFile = "Bigo_" + (startTime/1000) + ".mp4";
        String savePath = new File(downloadDir, currentFile).getAbsolutePath();

        startForeground(1, createNotification());

        new Thread(() -> {
            // PERINTAH SAKTI
            String cmd = "-y -i " + url + " -c copy -bsf:a aac_adtstoasc " + savePath;
            
            // Panggil Fungsi Native FFmpegKit yang kita temukan lewat strings/nm tadi
            FFmpegKitConfig.nativeFFmpegExecute(System.currentTimeMillis(), cmd);
            
            isRecording = false;
            stopSelf();
        }).start();

        return START_STICKY;
    }

    private Notification createNotification() {
        NotificationChannel chan = new NotificationChannel("recorder", "Bigo Recorder", NotificationManager.IMPORTANCE_LOW);
        getSystemService(NotificationManager.class).createNotificationChannel(chan);
        return new NotificationCompat.Builder(this, "recorder")
                .setContentTitle("Bigo Guardian")
                .setContentText("Merekam Stream ke MP4")
                .setSmallIcon(android.R.drawable.ic_media_play)
                .build();
    }

    @Override
    public void onDestroy() {
        isRecording = false;
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) { return null; }
}
