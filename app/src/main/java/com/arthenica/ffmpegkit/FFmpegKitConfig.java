package com.arthenica.ffmpegkit;

import android.util.Log;

public class FFmpegKitConfig {
    static {
        try {
            // Urutan load yang wajib sesuai dependensi FFmpeg
            System.loadLibrary("c++_shared");
            System.loadLibrary("avutil");
            System.loadLibrary("swresample");
            System.loadLibrary("avcodec");
            System.loadLibrary("avformat");
            System.loadLibrary("swscale");
            System.loadLibrary("avfilter");
            System.loadLibrary("avdevice");
            System.loadLibrary("ffmpegkit_abidetect");
            System.loadLibrary("ffmpegkit");
            Log.d("BIGO_DEBUG", "Semua library FFmpeg berhasil dimuat!");
        } catch (UnsatisfiedLinkError e) {
            Log.e("BIGO_DEBUG", "Gagal muat library: " + e.getMessage());
        }
    }

    // Pastikan nama fungsi native ini persis seperti di mesin aslinya
    public static native int nativeFFmpegExecute(long sessionId, String command);
    public static native void nativeFFmpegCancel(long sessionId);
}
