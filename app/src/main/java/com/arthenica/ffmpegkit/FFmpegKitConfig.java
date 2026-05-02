package com.arthenica.ffmpegkit;

public class FFmpegKitConfig {
    static {
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
    }
    public static native int nativeFFmpegExecute(long sessionId, String command);
    public static native void nativeFFmpegCancel(long sessionId);
}
