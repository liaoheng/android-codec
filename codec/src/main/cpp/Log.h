//
// Created by liaoheng on 2018/6/29.
//

#ifndef IMARAD_ANDROID_LOG_H
#define IMARAD_ANDROID_LOG_H

#include <android/log.h>

#define TAG "AFFmpeg"


#ifndef A_DEBUG
#ifdef NDEBUG
#define A_DEBUG 0
#else
#define A_DEBUG 1
#endif
#endif


#ifdef NDEBUG
#define LOGI(...)
#else
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,TAG,__VA_ARGS__)
#endif

#ifdef NDEBUG
#define LOGD(...)
#else
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, TAG, __VA_ARGS__)
#endif

#ifdef NDEBUG
#define LOGE(...)
#else
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR,TAG,__VA_ARGS__)
#endif

#endif //IMARAD_ANDROID_LOG_H
