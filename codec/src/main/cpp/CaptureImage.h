//
// Created by liaoheng on 2018/7/9.
//

#ifndef IMARAD_ANDROID_CAPTUREIMAGE_H
#define IMARAD_ANDROID_CAPTUREIMAGE_H
#ifdef __cplusplus
extern "C"
{
#endif
#include "libavcodec/avcodec.h"
#include "libavformat/avformat.h"
#ifdef __cplusplus
}
#endif
#include "Log.h"
class CaptureImage {
public:
    bool capture(const char* filename,int width,int height, unsigned char* data,int length,enum AVCodecID id);
    bool captureH264(const char* filename,int width,int height, unsigned char* data,int length);
    bool captureH265(const char* filename,int width,int height, unsigned char* data,int length);

    int writeJPEG(const char * out_file,AVFrame* pFrame, int width, int height);
};


#endif //IMARAD_ANDROID_CAPTUREIMAGE_H
