#include <jni.h>
#include <string>
#include <atomic>
#include <map>
#include <mutex>
#include <vector>

// Bungkus FFmpeg dengan extern "C"
extern "C" {
#include <libavformat/avformat.h>
#include <libavutil/mathematics.h>
#include <libavutil/time.h>
}

using namespace std;

map<int, atomic<bool>*> stop_flags;
map<int, atomic<long>*> durations;
mutex mtx;

extern "C" {

JNIEXPORT jint JNICALL
Java_com_example_bigoguardian_RecorderService_startNativeRecording(JNIEnv *env, jobject thiz, jint id, jstring jurl, jstring jpath) {
    const char *url = env->GetStringUTFChars(jurl, 0);
    const char *path = env->GetStringUTFChars(jpath, 0);
    
    auto* s = new atomic<bool>(false);
    auto* d = new atomic<long>(0);
    
    { lock_guard<mutex> lock(mtx); stop_flags[id] = s; durations[id] = d; }

    AVFormatContext *ictx = nullptr, *octx = nullptr;
    if (avformat_open_input(&ictx, url, nullptr, nullptr) < 0) return -1;
    if (avformat_find_stream_info(ictx, nullptr) < 0) return -2;
    if (avformat_alloc_output_context2(&octx, nullptr, nullptr, path) < 0 || !octx) return -3;

    for (int i = 0; i < ictx->nb_streams; i++) {
        AVStream *out = avformat_new_stream(octx, nullptr);
        avcodec_parameters_copy(out->codecpar, ictx->streams[i]->codecpar);
        out->codecpar->codec_tag = 0;
    }

    if (!(octx->oformat->flags & AVFMT_NOFILE)) {
        if (avio_open(&octx->pb, path, AVIO_FLAG_WRITE) < 0) return -4;
    }
    
    avformat_write_header(octx, nullptr);

    AVPacket pkt;
    int64_t start_pts = -1;
    while (!s->load() && av_read_frame(ictx, &pkt) >= 0) {
        AVStream *in_s = ictx->streams[pkt.stream_index];
        AVStream *out_s = octx->streams[pkt.stream_index];
        if (start_pts == -1 && pkt.pts != AV_NOPTS_VALUE) start_pts = pkt.pts;
        if (pkt.pts != AV_NOPTS_VALUE) d->store((long)((pkt.pts - start_pts) * av_q2d(in_s->time_base)));
        pkt.pts = av_rescale_q(pkt.pts, in_s->time_base, out_s->time_base);
        pkt.dts = av_rescale_q(pkt.dts, in_s->time_base, out_s->time_base);
        pkt.duration = av_rescale_q(pkt.duration, in_s->time_base, out_s->time_base);
        pkt.pos = -1;
        av_interleaved_write_frame(octx, &pkt);
        av_packet_unref(&pkt);
    }

    av_write_trailer(octx);
    avformat_close_input(&ictx);
    if (octx && !(octx->oformat->flags & AVFMT_NOFILE)) avio_closep(&octx->pb);
    avformat_free_context(octx);
    env->ReleaseStringUTFChars(jurl, url); 
    env->ReleaseStringUTFChars(jpath, path);
    return 0;
}

JNIEXPORT jlong JNICALL
Java_com_example_bigoguardian_RecorderService_getNativeDuration(JNIEnv *env, jobject thiz, jint id) {
    lock_guard<mutex> lock(mtx);
    return (durations.count(id)) ? durations[id]->load() : 0;
}

JNIEXPORT void JNICALL
Java_com_example_bigoguardian_RecorderService_stopNativeRecording(JNIEnv *env, jobject thiz, jint id) {
    lock_guard<mutex> lock(mtx);
    if (stop_flags.count(id)) stop_flags[id]->store(true);
}
}
