#include <jni.h>
#include <string>
#include <thread>
#include <atomic>
#include <android/log.h>

extern "C" {
#include <libavformat/avformat.h>
}

std::atomic<bool> keep_running(false);
JavaVM* g_vm = nullptr;
jobject g_obj = nullptr;

// Fungsi buat kirim text ke layar HP
void send_to_java(const char* msg) {
    JNIEnv* env;
    if (g_vm->AttachCurrentThread(&env, NULL) == JNI_OK) {
        jclass clazz = env->GetObjectClass(g_obj);
        jmethodID methodId = env->GetMethodID(clazz, "updateStatusFromNative", "(Ljava/lang/String;)V");
        jstring jmsg = env->NewStringUTF(msg);
        env->CallVoidMethod(g_obj, methodId, jmsg);
        env->DeleteLocalRef(jmsg);
        g_vm->DetachCurrentThread();
    }
}

void record_stream(std::string url, std::string out) {
    AVFormatContext *ifmt = NULL, *ofmt = NULL;
    AVPacket pkt;
    
    if (avformat_open_input(&ifmt, url.c_str(), NULL, NULL) < 0) {
        send_to_java("[Error] Gagal konek ke URL. URL mati?");
        return;
    }
    
    send_to_java("[Info] Stream terhubung! Menyiapkan file...");

    avformat_alloc_output_context2(&ofmt, NULL, NULL, out.c_str());
    if (!ofmt) {
        send_to_java("[Error] Gagal buat file output.");
        return;
    }

    // ... (Logika remuxing sama seperti sebelumnya) ...
    // Tambahkan send_to_java di titik-titik krusial
    
    avformat_write_header(ofmt, NULL);
    send_to_java("[Status] SEDANG MEREKAM...");

    while (keep_running) {
        if (av_read_frame(ifmt, &pkt) < 0) break;
        // ... simpan paket ...
        av_packet_unref(&pkt);
    }

    av_write_trailer(ofmt);
    avformat_close_input(&ifmt);
    if (ofmt) avio_closep(&ofmt->pb);
    avformat_free_context(ofmt);
    send_to_java("[Status] BERHASIL DISIMPAN!");
}

jint JNI_OnLoad(JavaVM* vm, void* reserved) {
    g_vm = vm;
    return JNI_VERSION_1_6;
}

extern "C" JNIEXPORT jstring JNICALL Java_com_bigo_posix_MainActivity_stringFromJNI(JNIEnv* env, jobject thiz) {
    return env->NewStringUTF("POSIX v1.2");
}

extern "C" JNIEXPORT jint JNICALL Java_com_bigo_posix_MainActivity_startRecording(JNIEnv* env, jobject thiz, jstring url, jstring out) {
    if (g_obj) env->DeleteGlobalRef(g_obj);
    g_obj = env->NewGlobalRef(thiz);
    
    const char *c_url = env->GetStringUTFChars(url, 0);
    const char *c_out = env->GetStringUTFChars(out, 0);
    
    keep_running = true;
    std::thread(record_stream, std::string(c_url), std::string(c_out)).detach();
    
    env->ReleaseStringUTFChars(url, c_url);
    env->ReleaseStringUTFChars(out, c_out);
    return 0;
}

extern "C" JNIEXPORT void JNICALL Java_com_bigo_posix_MainActivity_stopRecording(JNIEnv* env, jobject thiz) {
    keep_running = false;
}
