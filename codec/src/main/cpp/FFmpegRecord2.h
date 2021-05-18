//
// Created by liaoheng on 2018/7/2.
//

#ifndef IMARAD_ANDROID_FFMPEGRECORD2_H
#define IMARAD_ANDROID_FFMPEGRECORD2_H

#ifdef __cplusplus
extern "C"
{
#endif
#include "libavcodec/avcodec.h"
#include "libavformat/avformat.h"
#include "libavutil/channel_layout.h"
#include "libavutil/opt.h"
#include "libavutil/mathematics.h"
#include "libavutil/timestamp.h"
#include "libswscale/swscale.h"
#include "libswresample/swresample.h"
#include "libavutil/avassert.h"
#ifdef __cplusplus
}
#endif

#include <atomic>
#include "Log.h"

#define STREAM_PIX_FMT    AV_PIX_FMT_YUV420P /* default pix_fmt */

// a wrapper around a single output AVStream
typedef struct OutputStream2 {
    AVStream *st;
    AVCodecContext *enc;

    /* pts of the next frame that will be generated */
    int64_t next_pts;
    int64_t samples_count;

    AVFrame *frame;
    AVFrame *tmp_frame;

    float t, tincr, tincr2;

    struct SwsContext *sws_ctx;
    struct SwrContext *swr_ctx;
} OutputStream2;

typedef struct Setting {
    int width = 1920;
    int height = 1080;
    int fps = 30;         //帧率，FPS
    const char *filename;//录制的文件名称
    int gop = 25;//采样关键帧间隔
    int bitRate = 1024 * 512;//码率
    enum AVCodecID videoCodecId;
} Setting;

class FFmpegRecord2 {
public:
    FFmpegRecord2();

    ~FFmpegRecord2();

    bool
    init(const char *fileName, const int width, const int height, const int bitRate, const int gop,
         const int frameRate, const int videoCodecId);

    void release();

    std::atomic<bool> isInit;

    int have_video = 0, have_audio = 0;

    //写入音频数据
    void write_audio_frame(unsigned char *data, int length);

    //写入视频数据
    void write_video_frame(unsigned char *data, int length);

private:
    bool add_stream(OutputStream2 *ost, AVFormatContext *oc,
                    AVCodec **codec,
                    enum AVCodecID codec_id);

    bool open_video(AVFormatContext *oc, AVCodec *codec, OutputStream2 *ost, AVDictionary *opt_arg);

    bool open_audio(AVFormatContext *oc, AVCodec *codec, OutputStream2 *ost, AVDictionary *opt_arg);

    bool init_ffmpeg_record();

    int release_ffmpeg_record();

    void log_packet(const AVFormatContext *fmt_ctx, const AVPacket *pkt);

    int write_frame(AVFormatContext *fmt_ctx, const AVRational *time_base, AVStream *st, AVPacket *pkt);

    AVFrame *alloc_audio_frame(AVSampleFormat sample_fmt, uint64_t channel_layout, int sample_rate,
                               int nb_samples);

    AVFrame *alloc_picture(AVPixelFormat pix_fmt, int width, int height);

    void close_stream(AVFormatContext *oc, OutputStream2 *ost);

    bool check_key_frame(enum AVCodecID id, unsigned char *pData, int nDataSize);

    Setting mSetting;
    OutputStream2 video_st = {0}, audio_st = {0};
    AVFormatContext *oc;
};

#endif //IMARAD_ANDROID_FFMPEGRECORD2_H
