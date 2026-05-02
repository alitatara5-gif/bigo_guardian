package com.arthenica.ffmpegkit;

import android.util.Log;

public class FFmpegKitConfig {
    static {
        try {
            System.loadLibrary("c++_shared");
            System.loadLibrary("ffmpegkit");
            Log.d("FFMPEG_NATIVE", "Library FFmpegKit Berhasil di-load");
        } catch (Throwable e) {
            Log.e("FFMPEG_NATIVE", "Gagal load library: " + e.getMessage());
        }
    }
    // Fungsi ini harus persis namanya dengan yang ada di hasil nm -D kemarin
    public static native int nativeFFmpegExecute(long sessionId, String command);
}
