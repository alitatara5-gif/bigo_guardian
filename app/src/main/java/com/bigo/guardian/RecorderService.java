package com.bigo.guardian;

import android.app.*;
import android.content.Intent;
import android.os.*;
import androidx.core.app.NotificationCompat;
import com.arthenica.ffmpegkit.FFmpegKitConfig;
import java.io.File;

public class RecorderService extends Service {
    public static boolean isRecording = false;
    private long sessionId = 0;
    private PowerManager.WakeLock wakeLock;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String url = intent.getStringExtra("url");
        sessionId = System.currentTimeMillis();
        isRecording = true;

        PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "BigoGuardian:Lock");
        wakeLock.acquire();

        File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "BigoGuardian");
        if (!dir.exists()) dir.mkdirs();
        String path = new File(dir, "Rec_" + (sessionId/1000) + ".mp4").getAbsolutePath();

        startForeground(1, new NotificationCompat.Builder(this, "recorder")
                .setContentTitle("Bigo Guardian")
                .setContentText("Merekam stream...")
                .setSmallIcon(android.R.drawable.ic_media_play).build());

        new Thread(() -> {
            String cmd = "-y -i \"" + url + "\" -c copy -bsf:a aac_adtstoasc \"" + path + "\"";
            FFmpegKitConfig.nativeFFmpegExecute(sessionId, cmd);
            isRecording = false;
            stopSelf();
        }).start();

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        FFmpegKitConfig.nativeFFmpegCancel(sessionId);
        if (wakeLock != null && wakeLock.isHeld()) wakeLock.release();
        isRecording = false;
        super.onDestroy();
    }
    @Override
    public IBinder onBind(Intent intent) { return null; }
}
