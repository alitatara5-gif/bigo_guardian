#include <jni.h>
#include <string>

extern "C" JNIEXPORT jstring JNICALL
Java_com_example_bigoguardian_MainActivity_testOtot(JNIEnv *env, jobject thiz) {
    return env->NewStringUTF("✅ 7 Pasukan .so Berhasil Terbaca!");
}
