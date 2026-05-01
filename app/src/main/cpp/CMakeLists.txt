cmake_minimum_required(VERSION 3.22.1)
project("bigoguardian")

# Pastikan Robot mencari file .so di tempat yang benar
set(ffmpeg_DIR ${CMAKE_SOURCE_DIR}/../jniLibs/arm64-v8a)

foreach(lib avcodec avdevice avfilter avformat avutil swresample swscale)
    add_library(${lib} SHARED IMPORTED)
    set_target_properties(${lib} PROPERTIES IMPORTED_LOCATION ${ffmpeg_DIR}/lib${lib}.so)
endforeach()

add_library(bigoguardian_engine SHARED native-lib.cpp)

# Arahkan ke folder include yang tadi Abang cek (libavcodec, dll)
include_directories(include)

target_link_libraries(bigoguardian_engine
    avcodec avformat avutil swresample swscale avfilter avdevice
    android log z)
