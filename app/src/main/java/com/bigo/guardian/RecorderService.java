package com.bigo.guardian;

import android.app.*;
import android.content.Intent;
import android.os.*;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import com.arthenica.ffmpegkit.FFmpegKitConfig;
import java.io.File;

public class RecorderService extends Service {
    private long sessionId = 0;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String url = intent.getStringExtra("url");
        if (url == null) return START_NOT_STICKY;

        sessionId = System.currentTimeMillis();
        
        // KRUSIAL: Berikan sinyal agar mesin native tidak panik
        try {
            FFmpegKitConfig.ignoreNativeSignal(13);
        } catch (Exception e) {
            Log.e("BIGO_DEBUG", "Gagal set signal");
        }

        File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "BigoGuardian");
        if (!dir.exists()) dir.mkdirs();
        String path = new File(dir, "Live_" + (sessionId/1000) + ".mp4").getAbsolutePath();

        NotificationChannel chan = new NotificationChannel("rec", "Recorder", NotificationManager.IMPORTANCE_LOW);
        getSystemService(NotificationManager.class).createNotificationChannel(chan);
        startForeground(1, new NotificationCompat.Builder(this, "rec")
                .setContentTitle("Bigo Guardian")
                .setContentText("Merekam Stream...")
                .setSmallIcon(android.R.drawable.ic_media_play).build());

        // Jalankan di Thread, tapi pastikan JNIEnv stabil
        new Thread(() -> {
            // JANGAN pakai tanda kutip tambahan di link, biar library yang handle
            String cmd = "-y -i " + url + " -c copy -bsf:a aac_adtstoasc " + path;
            Log.d("BIGO_DEBUG", "CMD: " + cmd);
            
            try {
                // Panggil mesin native
                int result = FFmpegKitConfig.nativeFFmpegExecute(sessionId, cmd);
                Log.d("BIGO_DEBUG", "Mesin Selesai dengan hasil: " + result);
            } catch (Throwable t) {
                Log.e("BIGO_DEBUG", "Mesin native menyerah: " + t.getMessage());
            }
            stopSelf();
        }).start();

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        try { FFmpegKitConfig.nativeFFmpegCancel(sessionId); } catch (Exception e) {}
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) { return null; }
}
