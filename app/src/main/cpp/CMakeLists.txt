cmake_minimum_required(VERSION 3.4.1)

# 设置include文件夹的地址
include_directories(C:\\Jetbrains\\AS\\Opencv\\OpenCV-android-sdk\\sdk\\native\\jni\\include)

# 设置opencv的动态库
add_library(libopencv_java3 SHARED IMPORTED)
set_target_properties(libopencv_java3 PROPERTIES IMPORTED_LOCATION
            C:\\Jetbrains\\AS\\Opencv\\OpenCV-android-sdk\\sdk\\native\\libs\\${ANDROID_ABI}\\libopencv_java3.so)

target_link_libraries( # Specifies the target library.
                       native-lib   libopencv_java3
                       jnigraphics
                       # Links the target library to the log library
                       # included in the NDK.
                       ${log-lib} )
