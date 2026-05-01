cmake_minimum_required(VERSION 3.22.1)
project("bigoguardian")

# 1. Daftarkan libffmpeg.so sebagai library impor
add_library(ffmpeg-lib SHARED IMPORTED)
set_target_properties(ffmpeg-lib PROPERTIES IMPORTED_LOCATION
    ${CMAKE_SOURCE_DIR}/../jniLibs/${ANDROID_ABI}/libffmpeg.so)

# 2. Sertakan folder include FFmpeg
include_directories(${CMAKE_SOURCE_DIR}/include)

# 3. Buat library utama native-lib
add_library(native-lib SHARED native-lib.cpp)

# 4. Cari library sistem Android
find_library(log-lib log)
find_library(android-lib android)

# 5. Link semuanya
target_link_libraries(
    native-lib
    ffmpeg-lib
    ${log-lib}
    ${android-lib}
)
