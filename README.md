# 🛡️ Bigo Guardian (Native)

Aplikasi perekam stream Bigo berbasis **Native JNI** untuk performa maksimal dan bypass batasan Android.

### 🏗️ Arsitektur
* **Engine:** FFmpeg 8.0.1 (Shared Libraries)
* **Pasukan:** 7 File `.so` (avcodec, avformat, avutil, swresample, swscale, avfilter, avdevice)
* **Bridge:** C++ JNI untuk komunikasi Java-FFmpeg.

### ✨ Keunggulan
* **High Performance:** Jalan langsung di RAM, bukan via shell/binary.
* **Bypass W^X:** Aman untuk Android 10 sampai 14+.
* **Automated:** Build otomatis via GitHub Actions.

### 🚀 Cara Build
1. **Push** kode ke GitHub.
2. Buka tab **Actions** > **Build APK Native Test**.
3. **Run Workflow** dan download APK di bagian **Artifacts**.

---
**Build with Power by Alitatara.**
