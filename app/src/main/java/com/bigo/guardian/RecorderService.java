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
    private PowerManager.WakeLock wakeLock;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String url = intent.getStringExtra("url");
        if (url == null || url.isEmpty()) {
            stopSelf();
            return START_NOT_STICKY;
        }

        sessionId = System.currentTimeMillis();
        
        // Jaga HP agar tetap menyala saat merekam
        PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "BigoGuardian:RecLock");
        wakeLock.acquire();

        File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "BigoGuardian");
        if (!dir.exists()) dir.mkdirs();
        String path = new File(dir, "Bigo_" + (sessionId/1000) + ".mp4").getAbsolutePath();

        // Notifikasi agar service tidak dimatikan Android
        NotificationChannel chan = new NotificationChannel("rec", "Bigo Recorder", NotificationManager.IMPORTANCE_LOW);
        getSystemService(NotificationManager.class).createNotificationChannel(chan);
        startForeground(1, new NotificationCompat.Builder(this, "rec")
                .setContentTitle("Bigo Guardian Aktif")
                .setContentText("Sedang merekam stream...")
                .setSmallIcon(android.R.drawable.ic_media_play).build());

        new Thread(() -> {
            try {
                // Gunakan tanda kutip agar URL yang panjang tidak bikin crash
                String cmd = "-y -i \"" + url + "\" -c copy -bsf:a aac_adtstoasc \"" + path + "\"";
                Log.d("BIGO_DEBUG", "Menjalankan perintah: " + cmd);
                
                FFmpegKitConfig.nativeFFmpegExecute(sessionId, cmd);
            } catch (Exception e) {
                Log.e("BIGO_DEBUG", "Error saat merekam: " + e.getMessage());
            } finally {
                stopSelf();
            }
        }).start();

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        FFmpegKitConfig.nativeFFmpegCancel(sessionId);
        if (wakeLock != null && wakeLock.isHeld()) wakeLock.release();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) { return null; }
}
