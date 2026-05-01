cmake_minimum_required(VERSION 3.22.1)
project("bigoguardian")

# Lokasi file .so (Otot)
set(ffmpeg_DIR ${CMAKE_SOURCE_DIR}/../jniLibs/${ANDROID_ABI})

# Daftarkan semua library FFmpeg sebagai IMPORTED
foreach(lib avcodec avdevice avfilter avformat avutil swresample swscale)
    add_library(${lib} SHARED IMPORTED)
    set_target_properties(${lib} PROPERTIES IMPORTED_LOCATION ${ffmpeg_DIR}/lib${lib}.so)
endforeach()

# Jembatan Utama
add_library(bigoguardian_engine SHARED native-lib.cpp)

# PENTING: Kasih tahu lokasi semua folder include agar Robot gak nyasar
include_directories(
    include
    include/libavcodec
    include/libavformat
    include/libavutil
    include/libswscale
    include/libswresample
    include/libavdevice
    include/libavfilter
)

# Ikat semuanya jadi satu mesin utuh
target_link_libraries(bigoguardian_engine
    avcodec avformat avutil swresample swscale avfilter avdevice
    android log z)
