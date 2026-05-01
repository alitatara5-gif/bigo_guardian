#include <jni.h>
#include <string>
#include <android/log.h>
#include <thread>
#include <atomic>

extern "C" {
#include <libavformat/avformat.h>
#include <libavutil/avutil.h>
#include <libavutil/mathematics.h>
}

#define LOG_TAG "BigoGuardianNative"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

std::atomic<bool> keep_running(false);

void record_stream(std::string input_url, std::string output_path) {
    AVFormatContext *ifmt_ctx = NULL, *ofmt_ctx = NULL;
    AVPacket pkt;
    int ret, i;

    if ((ret = avformat_open_input(&ifmt_ctx, input_url.c_str(), NULL, NULL)) < 0) {
        LOGE("Gagal buka input stream!");
        return;
    }
    if ((ret = avformat_find_stream_info(ifmt_ctx, NULL)) < 0) return;

    avformat_alloc_output_context2(&ofmt_ctx, NULL, NULL, output_path.c_str());
    if (!ofmt_ctx) return;

    for (i = 0; i < ifmt_ctx->nb_streams; i++) {
        AVStream *in_stream = ifmt_ctx->streams[i];
        AVStream *out_stream = avformat_new_stream(ofmt_ctx, NULL);
        avcodec_parameters_copy(out_stream->codecpar, in_stream->codecpar);
        out_stream->codecpar->codec_tag = 0;
    }

    if (!(ofmt_ctx->oformat->flags & AVFMT_NOFILE)) {
        // FIX: Pakai AVIO_FLAG_WRITE
        if (avio_open(&ofmt_ctx->pb, output_path.c_str(), AVIO_FLAG_WRITE) < 0) return;
    }

    // Pakai return check buat ngilangin warning
    ret = avformat_write_header(ofmt_ctx, NULL);
    if (ret < 0) return;

    LOGI("Perekaman dimulai...");

    while (keep_running) {
        ret = av_read_frame(ifmt_ctx, &pkt);
        if (ret < 0) break;

        AVStream *in_stream  = ifmt_ctx->streams[pkt.stream_index];
        AVStream *out_stream = ofmt_ctx->streams[pkt.stream_index];

        // FIX: Pakai av_rescale_q (gak ada _nd nya)
        pkt.pts = av_rescale_q(pkt.pts, in_stream->time_base, out_stream->time_base);
        pkt.dts = av_rescale_q(pkt.dts, in_stream->time_base, out_stream->time_base);
        pkt.duration = av_rescale_q(pkt.duration, in_stream->time_base, out_stream->time_base);
        pkt.pos = -1;

        av_interleaved_write_frame(ofmt_ctx, &pkt);
        av_packet_unref(&pkt);
    }

    av_write_trailer(ofmt_ctx);
    avformat_close_input(&ifmt_ctx);
    if (ofmt_ctx && !(ofmt_ctx->oformat->flags & AVFMT_NOFILE))
        avio_closep(&ofmt_ctx->pb);
    avformat_free_context(ofmt_ctx);
    
    LOGI("Perekaman berhenti aman.");
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_bigo_posix_MainActivity_stringFromJNI(JNIEnv* env, jobject /* this */) {
    return env->NewStringUTF("Engine POSIX Ready | FFmpeg v8.0.1");
}

extern "C" JNIEXPORT jint JNICALL
Java_com_bigo_posix_MainActivity_startRecording(JNIEnv* env, jobject /* this */, jstring url, jstring output) {
    if (keep_running) return -1;
    
    const char *nativeUrl = env->GetStringUTFChars(url, 0);
    const char *nativeOutput = env->GetStringUTFChars(output, 0);

    keep_running = true;
    std::thread(record_stream, std::string(nativeUrl), std::string(nativeOutput)).detach();

    env->ReleaseStringUTFChars(url, nativeUrl);
    env->ReleaseStringUTFChars(output, nativeOutput);
    return 0;
}

extern "C" JNIEXPORT void JNICALL
Java_com_bigo_posix_MainActivity_stopRecording(JNIEnv* env, jobject /* this */) {
    keep_running = false;
}
