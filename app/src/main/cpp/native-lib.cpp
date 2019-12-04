//
// Created by Jarvis Zhang on 2019/11/21.
//

#include <jni.h>
#include <string>
#include <iostream>
#include <opencv2/opencv.hpp>

using namespace cv;

extern "C" JNIEXPORT jstring JNICALL
Java_com_example_stitch_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {

    std::string hello;

    if (true) {
        hello = "Hello from OpenCV " + (std::string)CV_VERSION;
    } else {
        hello = "Hello from C++";
    }

    return env->NewStringUTF(hello.c_str());

}
