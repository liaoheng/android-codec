//
// Created by liaoheng on 2018/6/29.
//

#ifndef IMARAD_ANDROID_AFFMPEG_H
#define IMARAD_ANDROID_AFFMPEG_H

#include "FFmpegRecord2.h"
#include "CaptureImage.h"
#include "ImageToYUV.h"

#ifdef __cplusplus
extern "C"
{
#endif
#include "g711_table.h"
#ifdef __cplusplus
}
#endif
#define FRAME_LENGTH 2048

FFmpegRecord2* mRecord;
CaptureImage mCaptureImage;
ImageToYUV imageToYUV;

#endif //IMARAD_ANDROID_AFFMPEG_H
