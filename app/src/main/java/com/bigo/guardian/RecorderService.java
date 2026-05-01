package com.bigo.guardian;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Binder;
import android.os.Environment;
import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RecorderService extends Service {
    private ExecutorService executor = Executors.newFixedThreadPool(50);
    private final IBinder binder = new LocalBinder();

    public class LocalBinder extends Binder {
        RecorderService getService() { return RecorderService.this; }
    }

    static {
        System.loadLibrary("c++_shared");
        System.loadLibrary("avutil");
        System.loadLibrary("swresample");
        System.loadLibrary("avcodec");
        System.loadLibrary("avformat");
        System.loadLibrary("swscale");
        System.loadLibrary("avfilter");
        System.loadLibrary("avdevice");
        System.loadLibrary("bigoguardian_engine");
    }

    public native int startNativeRecording(int id, String url, String path);
    public native void stopNativeRecording(int id);
    public native long getNativeDuration(int id);

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && "STOP".equals(intent.getAction())) {
            stopNativeRecording(intent.getIntExtra("id", -1));
            return START_NOT_STICKY;
        }

        if (intent != null) {
            int id = intent.getIntExtra("id", 0);
            String url = intent.getStringExtra("url");
            File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES), "BigoGuardian");
            if (!dir.exists()) dir.mkdirs();
            String path = dir.getAbsolutePath() + "/rec_" + id + ".ts";
            executor.execute(() -> startNativeRecording(id, url, path));
        }
        return START_STICKY;
    }

    @Override public IBinder onBind(Intent intent) { return binder; }
}
