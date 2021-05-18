//
// Created by liaoheng on 2019/1/8.
//

#ifndef IMARAD_ANDROID_IMAGETOYUV_H
#define IMARAD_ANDROID_IMAGETOYUV_H

#ifdef __cplusplus
extern "C"
{
#endif
#include "libavcodec/avcodec.h"
#include "libavformat/avformat.h"
#include "libswscale/swscale.h"
#include "libavutil/imgutils.h"
#include "libavutil/fifo.h"
#ifdef __cplusplus
}
#endif

#include "Log.h"

class ImageToYUV
{
public:
    bool decode(const char *filename, const int width, const int height, const char *outfilename);
    unsigned char *decode(unsigned char *data,size_t length, const int width, const int height);
    bool init();
    void release();

private:
    AVCodec *codec;
    AVCodecContext *avctx;
    void yuv420p_save(AVFrame *pFrame, const char *outfilename);
};


#endif //IMARAD_ANDROID_IMAGETOYUV_H
