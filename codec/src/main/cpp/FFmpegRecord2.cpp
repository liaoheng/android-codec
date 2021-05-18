//
// Created by liaoheng on 2018/7/2.
//

#include "FFmpegRecord2.h"


FFmpegRecord2::FFmpegRecord2() {}

FFmpegRecord2::~FFmpegRecord2() {}

void FFmpegRecord2::write_video_frame(unsigned char *data, int length) {
    if (!isInit) {
        return;
    }

    int ret;
    AVCodecContext *c;
    AVPacket pkt = {};

    OutputStream2 *ost = &video_st;

    c = ost->enc;

    av_init_packet(&pkt);

    pkt.size = length;
    pkt.data = data;
    pkt.flags |= check_key_frame(oc->oformat->video_codec, data, length) ? AV_PKT_FLAG_KEY : 0;
    if (ost->st) {
        pkt.stream_index = ost->st->index;
    }

    if (ost->next_pts >= INT64_MAX) {
        ost->next_pts = 0;
    } else {
        ost->next_pts++;
    }

    //计算pts :https://www.jianshu.com/p/bf323cee3b8e
    pkt.pts = av_rescale_q(ost->next_pts, c->time_base,
                           c->time_base);
    pkt.dts = pkt.pts;
    ret = write_frame(oc, &c->time_base, ost->st, &pkt);
    if (ret < 0) {
        LOGE("Error while writing video frame: %s\n", av_err2str(ret));
    }
}

void FFmpegRecord2::write_audio_frame(unsigned char *data, int length) {
    if (!isInit) {
        return;
    }

    AVCodecContext *c;
    AVPacket pkt = {};
    AVFrame *frame;
    int ret;
    int got_packet;
    int dst_nb_samples;

    OutputStream2 *ost = &audio_st;

    av_init_packet(&pkt);
    c = ost->enc;

    frame = ost->tmp_frame;

    if (frame) {
        memcpy(frame->data[0], data, length);
        frame->pts = ost->next_pts;
        ost->next_pts += frame->nb_samples;

        /* convert samples from native format to destination codec format, using the resampler */
        /* compute destination number of samples */
        dst_nb_samples = av_rescale_rnd(
                swr_get_delay(ost->swr_ctx, c->sample_rate) + frame->nb_samples,
                c->sample_rate, c->sample_rate, AV_ROUND_UP);
        av_assert0(dst_nb_samples == frame->nb_samples);

        /* when we pass a frame to the encoder, it may keep a reference to it
         * internally;
         * make sure we do not overwrite it here
         */
        ret = av_frame_make_writable(ost->frame);
        if (ret < 0)
            return;

        /* convert to destination format */
        ret = swr_convert(ost->swr_ctx,
                          ost->frame->data, dst_nb_samples,
                          (const uint8_t **) frame->data, frame->nb_samples);
        if (ret < 0) {
            LOGE("Error while converting\n");
            return;
        }
        frame = ost->frame;

        frame->pts = av_rescale_q(ost->samples_count, (AVRational) {1, c->sample_rate},
                                  c->time_base);
        ost->samples_count += dst_nb_samples;

        ret = avcodec_encode_audio2(c, &pkt, frame, &got_packet);
        if (ret < 0) {
            LOGE("Error encoding audio frame: %s\n", av_err2str(ret));
            return;
        }
        if (got_packet) {
            ret = write_frame(oc, &c->time_base, ost->st, &pkt);
            if (ret < 0) {
                LOGE("Error while writing audio frame: %s\n",
                     av_err2str(ret));
                return;
            }
        }
    }
}

bool FFmpegRecord2::add_stream(OutputStream2 *ost, AVFormatContext *oc, AVCodec **codec,
                               enum AVCodecID codec_id) {

    AVCodecContext *c;
    int i;
    /* find the encoder */
    *codec = avcodec_find_encoder(codec_id);
    if (!(*codec)) {
        LOGE("Could not find encoder for '%s'",
             avcodec_get_name(codec_id));
        return false;
    }

    ost->st = avformat_new_stream(oc, NULL);
    if (!ost->st) {
        LOGE("Could not allocate stream");
        return false;
    }
    ost->st->id = oc->nb_streams - 1;
    c = avcodec_alloc_context3(*codec);
    if (!c) {
        LOGE("Could not alloc an encoding context\n");
        return false;
    }
    ost->enc = c;

    switch ((*codec)->type) {
        case AVMEDIA_TYPE_AUDIO:
            c->sample_fmt = (*codec)->sample_fmts ?
                            (*codec)->sample_fmts[0] : AV_SAMPLE_FMT_FLTP;
            c->bit_rate = 64000;
            c->sample_rate = 8000;//不能改变
            if ((*codec)->supported_samplerates) {
                c->sample_rate = (*codec)->supported_samplerates[0];
                for (i = 0; (*codec)->supported_samplerates[i]; i++) {
                    if ((*codec)->supported_samplerates[i] == 8000)
                        c->sample_rate = 8000;
                }
            }
            c->channels = av_get_channel_layout_nb_channels(c->channel_layout);
            c->channel_layout = AV_CH_LAYOUT_MONO;
            if ((*codec)->channel_layouts) {
                c->channel_layout = (*codec)->channel_layouts[0];
                for (i = 0; (*codec)->channel_layouts[i]; i++) {
                    if ((*codec)->channel_layouts[i] == AV_CH_LAYOUT_MONO)
                        c->channel_layout = AV_CH_LAYOUT_MONO;
                }
            }
            c->channels = av_get_channel_layout_nb_channels(c->channel_layout);
            ost->st->time_base = (AVRational) {1, c->sample_rate};
            break;

        case AVMEDIA_TYPE_VIDEO:
            c->codec_id = codec_id;

            c->bit_rate = mSetting.bitRate;
            /* Resolution must be a multiple of two. */
            c->width = mSetting.width;
            c->height = mSetting.height;
            /* timebase: This is the fundamental unit of time (in seconds) in terms
             * of which frame timestamps are represented. For fixed-fps content,
             * timebase should be 1/framerate and timestamp increments should be
             * identical to 1. */
            ost->st->time_base = (AVRational) {1, mSetting.fps};
            c->time_base = ost->st->time_base;

            c->gop_size = mSetting.gop; /* emit one intra frame every twelve frames at most */
            c->pix_fmt = STREAM_PIX_FMT;
            if (c->codec_id == AV_CODEC_ID_MPEG2VIDEO) {
                /* just for testing, we also add B-frames */
                c->max_b_frames = 2;
            }
            if (c->codec_id == AV_CODEC_ID_MPEG1VIDEO) {
                /* Needed to avoid using macroblocks in which some coeffs overflow.
                 * This does not happen with normal video, it just happens here as
                 * the motion of the chroma plane does not match the luma plane. */
                c->mb_decision = 2;
            }
            break;

        default:
            break;
    }

    /* Some formats want stream headers to be separate. */
    if (oc->oformat->flags & AVFMT_GLOBALHEADER)
        c->flags |= AV_CODEC_FLAG_GLOBAL_HEADER;

    return true;
}


bool FFmpegRecord2::open_audio(AVFormatContext *oc, AVCodec *codec, OutputStream2 *ost,
                               AVDictionary *opt_arg) {
    AVCodecContext *c;
    int nb_samples;
    int ret;
    AVDictionary *opt = NULL;
    c = ost->enc;

    LOGD("open audio codec: %s", avcodec_get_name(codec->id));
    av_dict_copy(&opt, opt_arg, 0);
    /* open it */
    ret = avcodec_open2(c, codec, &opt);
    av_dict_free(&opt);
    if (ret < 0) {
        LOGE("Could not open audio codec: %s\n", av_err2str(ret));
        return false;
    }

    /* init signal generator */
    ost->t = 0;
    ost->tincr = 2 * M_PI * 110.0 / c->sample_rate;
    /* increment frequency by 110 Hz per second */
    ost->tincr2 = 2 * M_PI * 110.0 / c->sample_rate / c->sample_rate;

    if (c->codec->capabilities & AV_CODEC_CAP_VARIABLE_FRAME_SIZE)
        nb_samples = 1024;
    else
        nb_samples = c->frame_size;//aac 默认1024

    ost->frame = alloc_audio_frame(c->sample_fmt, c->channel_layout,
                                   c->sample_rate, nb_samples);
    ost->tmp_frame = alloc_audio_frame(AV_SAMPLE_FMT_S16, c->channel_layout,
                                       c->sample_rate, nb_samples);

    /* copy the stream parameters to the muxer */
    ret = avcodec_parameters_from_context(ost->st->codecpar, c);
    if (ret < 0) {
        LOGE("Could not copy the stream parameters\n");
        return false;
    }

    /* create resampler context */
    ost->swr_ctx = swr_alloc();
    if (!ost->swr_ctx) {
        LOGE("Could not allocate resampler context\n");
        return false;
    }

    /* set options */
    av_opt_set_int(ost->swr_ctx, "in_channel_count", c->channels, 0);
    av_opt_set_int(ost->swr_ctx, "in_sample_rate", c->sample_rate, 0);
    av_opt_set_sample_fmt(ost->swr_ctx, "in_sample_fmt", AV_SAMPLE_FMT_S16, 0);
    av_opt_set_int(ost->swr_ctx, "out_channel_count", c->channels, 0);
    av_opt_set_int(ost->swr_ctx, "out_sample_rate", c->sample_rate, 0);
    av_opt_set_sample_fmt(ost->swr_ctx, "out_sample_fmt", c->sample_fmt, 0);

    /* initialize the resampling context */
    if ((ret = swr_init(ost->swr_ctx)) < 0) {
        LOGE("Failed to initialize the resampling context\n");
        return false;
    }

    return true;
}

bool FFmpegRecord2::open_video(AVFormatContext *oc, AVCodec *codec, OutputStream2 *ost,
                               AVDictionary *opt_arg) {
    int ret;
    AVCodecContext *c = ost->enc;

    LOGD("open video codec: %s", avcodec_get_name(codec->id));

    AVDictionary *opt = NULL;

    if (codec->id == AV_CODEC_ID_HEVC) {
//        av_dict_set(&opt, "preset", "ultrafast", 0);
//        av_dict_set(&opt, "tune", "zero-latency", 0);
        av_dict_set(&opt, "x265-params", "log-level=debug", 0);
    }
    av_dict_copy(&opt, opt_arg, 0);
    /* open the codec */
    ret = avcodec_open2(c, codec, &opt);
    av_dict_free(&opt);
    if (ret < 0) {
        LOGE("Could not open video codec: %s", av_err2str(ret));
        return false;
    }

    /* allocate and init a re-usable frame */
    ost->frame = alloc_picture(c->pix_fmt, c->width, c->height);
    if (!ost->frame) {
        LOGE("Could not allocate video frame\n");
        return false;
    }

    /* If the output format is not YUV420P, then a temporary YUV420P
     * picture is needed too. It is then converted to the required
     * output format. */
    ost->tmp_frame = NULL;
    if (c->pix_fmt != AV_PIX_FMT_YUV420P) {
        ost->tmp_frame = alloc_picture(AV_PIX_FMT_YUV420P, c->width, c->height);
        if (!ost->tmp_frame) {
            LOGE("Could not allocate temporary picture\n");
            return false;
        }
    }

    /* copy the stream parameters to the muxer */
    ret = avcodec_parameters_from_context(ost->st->codecpar, c);
    if (ret < 0) {
        LOGE("Could not copy the stream parameters\n");
        return false;
    }

    return true;
}


bool FFmpegRecord2::init_ffmpeg_record() {
    int ret;
    AVCodec *audio_codec = {0}, *video_codec = {0};
    AVOutputFormat *fmt;
    const char *filename = mSetting.filename;
    AVDictionary *opt = NULL;

    av_dict_set(&opt, "max_muxing_queue_size", "1024", 0);

    LOGD("FFmpegRecord init > filename : %s , width : %d ,height : %d ,bitRate : %d ,gop: %d ,frameRate(fps) %d",
         filename,
         mSetting.width, mSetting.height, mSetting.bitRate, mSetting.gop, mSetting.fps);

    /* allocate the output media context */
    avformat_alloc_output_context2(&oc, NULL, NULL, filename);
    if (!oc) {
        LOGE("Could not deduce output format from file extension");
        return false;
    }

    fmt = oc->oformat;
    fmt->video_codec = mSetting.videoCodecId;

    LOGD("oformat video_codec : %s  audio_codec : %s", avcodec_get_name(fmt->video_codec),
         avcodec_get_name(fmt->audio_codec));

    /* Add the audio and video streams using the default format codecs
        * and initialize the codecs. */
    if (fmt->video_codec != AV_CODEC_ID_NONE) {
        if (!add_stream(&video_st, oc, &video_codec, fmt->video_codec)) {
            return false;
        }
        have_video = 1;
    }
    if (fmt->audio_codec != AV_CODEC_ID_NONE) {
        if (!add_stream(&audio_st, oc, &audio_codec, fmt->audio_codec)) {
            return false;
        }
        have_audio = 1;
    }


    /* Now that all the parameters are set, we can open the audio and
     * video codecs and allocate the necessary encode buffers. */
    if (have_video) {
        if (!open_video(oc, video_codec, &video_st, opt)) {
            return false;
        }
    }
    if (have_audio) {
        if (!open_audio(oc, audio_codec, &audio_st, opt)) {
            return false;
        }
    }

    av_dump_format(oc, 0, filename, 1);

    /* open the output file, if needed */
    if (!(fmt->flags & AVFMT_NOFILE)) {
        ret = avio_open(&oc->pb, filename, AVIO_FLAG_WRITE);
        if (ret < 0) {
            LOGE("Could not open '%s': %s\n", filename,
                 av_err2str(ret));
            return false;
        }
    }

    /* Write the stream header, if any. */
    ret = avformat_write_header(oc, &opt);
    if (ret < 0) {
        LOGE("Error occurred when opening output file: %s\n",
             av_err2str(ret));
        return false;
    }

    return true;
}

bool FFmpegRecord2::init(const char *fileName, const int width, const int height,
                         const int bitRate, const int gop, const int frameRate,
                         const int videoCodecId) {
    mSetting.filename = fileName;
    mSetting.width = width;
    mSetting.height = height;
    mSetting.bitRate = bitRate;
    mSetting.gop = gop;
    mSetting.fps = frameRate;
    if (videoCodecId == 1) {
        mSetting.videoCodecId = AV_CODEC_ID_HEVC;
    } else {
        mSetting.videoCodecId = AV_CODEC_ID_H264;
    }
    isInit = init_ffmpeg_record();
    return isInit;
}

int FFmpegRecord2::release_ffmpeg_record() {
    /* Write the trailer, if any. The trailer must be written before you
     * close the CodecContexts open when you wrote the header; otherwise
     * av_write_trailer() may try to use memory that was freed on
     * av_codec_close(). */
    av_write_trailer(oc);

    if (have_video) {
        close_stream(oc, &video_st);
    }
    if (have_audio) {
        close_stream(oc, &audio_st);
    }

    if (!(oc->oformat->flags & AVFMT_NOFILE))
        /* Close the output file. */
        avio_closep(&oc->pb);
    /* free the stream */
    avformat_free_context(oc);
    return 0;
}

void FFmpegRecord2::release() {
    if (isInit) {
        isInit = false;
        LOGD("FFmpegRecord release");
        release_ffmpeg_record();
    }
}


void FFmpegRecord2::log_packet(const AVFormatContext *fmt_ctx, const AVPacket *pkt) {
    AVRational *time_base = &fmt_ctx->streams[pkt->stream_index]->time_base;

    LOGD("pts:%s pts_time:%s dts:%s dts_time:%s duration:%s duration_time:%s stream_index:%d",
         av_ts2str(pkt->pts), av_ts2timestr(pkt->pts, time_base),
         av_ts2str(pkt->dts), av_ts2timestr(pkt->dts, time_base),
         av_ts2str(pkt->duration), av_ts2timestr(pkt->duration, time_base),
         pkt->stream_index);
}

int
FFmpegRecord2::write_frame(AVFormatContext *fmt_ctx, const AVRational *time_base, AVStream *st,
                           AVPacket *pkt) {
    /* rescale output packet timestamp values from codec to stream timebase */
    av_packet_rescale_ts(pkt, *time_base, st->time_base);
    pkt->stream_index = st->index;

    /* Write the compressed frame to the media file. */
    log_packet(fmt_ctx, pkt);
    if (!isInit) {
        return -1;
    }
    return av_interleaved_write_frame(fmt_ctx, pkt);
}

AVFrame *FFmpegRecord2::alloc_audio_frame(AVSampleFormat sample_fmt, uint64_t channel_layout,
                                          int sample_rate, int nb_samples) {
    AVFrame *frame = av_frame_alloc();
    int ret;

    if (!frame) {
        LOGE("Error allocating an audio frame\n");
        return NULL;
    }

    frame->format = sample_fmt;
    frame->channel_layout = channel_layout;
    frame->sample_rate = sample_rate;
    frame->nb_samples = nb_samples;

    if (nb_samples) {
        ret = av_frame_get_buffer(frame, 0);
        if (ret < 0) {
            LOGE("Error allocating an audio buffer\n");
            return NULL;
        }
    }

    return frame;
}

AVFrame *FFmpegRecord2::alloc_picture(AVPixelFormat pix_fmt, int width, int height) {
    AVFrame *picture;
    int ret;

    picture = av_frame_alloc();
    if (!picture)
        return NULL;

    picture->format = pix_fmt;
    picture->width = width;
    picture->height = height;

    /* allocate the buffers for the frame data */
    ret = av_frame_get_buffer(picture, 32);
    if (ret < 0) {
        LOGE("Could not allocate frame data.\n");
        return NULL;
    }

    return picture;
}

void FFmpegRecord2::close_stream(AVFormatContext *oc, OutputStream2 *ost) {
    avcodec_flush_buffers(ost->enc);
    avcodec_free_context(&ost->enc);
    av_frame_free(&ost->frame);
    av_frame_free(&ost->tmp_frame);
    sws_freeContext(ost->sws_ctx);
    swr_free(&ost->swr_ctx);
    ost->next_pts = 0;
    ost->samples_count = 0;
}


bool FFmpegRecord2::check_key_frame(enum AVCodecID id, unsigned char *pData, int nDataSize) {
    if (id == AV_CODEC_ID_H265) {
        LOGD("%02X:%02X:%02X:%02X:%02X", pData[0], pData[1], pData[2], pData[3], pData[4]);
        return (pData[0] == 0 && pData[1] == 0 && pData[2] == 0
                && pData[3] == 1 && pData[4] == 38)
               || (pData[0] == 0 && pData[1] == 0 && pData[2] == 1
                   && pData[3] == 38);
    } else {
        unsigned long totalcnt = 0;
        while (true) { // H.264.2D
            if (0 == pData[0] && 0 == pData[1]) {
                if ((1 == pData[2]) ||
                    (0 == pData[2] && 1 == pData[3])) {
                    unsigned long nStartLength = (1 == pData[2]) ? 3 : 4;
                    unsigned long temp = pData[nStartLength] & 0x1f;
                    if ((5 == temp) ||
                        (9 == temp && pData[nStartLength + 1] == 0x10)) {
                        return true;
                    }

                    pData += nStartLength;
                    totalcnt += nStartLength;
                } else {
                    pData++;
                    totalcnt++;
                }
            } else {
                pData++;
                totalcnt++;
            }
            if (totalcnt > (nDataSize - 4)) {
                break;
            }
        }
        return false;
    }
}