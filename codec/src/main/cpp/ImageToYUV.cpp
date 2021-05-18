//
// Created by liaoheng on 2019/1/8.
//

#include "ImageToYUV.h"

bool ImageToYUV::init()
{
    AVCodecID id = AV_CODEC_ID_MJPEG;
    codec = avcodec_find_decoder(id);
    if (!codec)
    {
        LOGE("Could not find encoder for '%s'", avcodec_get_name(id));
        return false;
    }

    avctx = avcodec_alloc_context3(codec);
    if (!avctx)
    {
        LOGE("Could not allocate video codec context\n");
        return false;
    }

    if (avcodec_open2(avctx, codec, NULL) < 0)
    {
        LOGE("Could not open codec\n");
        return false;
    }
    return true;
}

void ImageToYUV::release()
{
    avcodec_free_context(&avctx);
}

unsigned char *
ImageToYUV::decode(unsigned char *data, size_t length, const int width, const int height)
{
    if (length < 1)
        return nullptr;
    AVFrame *frame;
    AVPacket *pkt;
    pkt = av_packet_alloc();
    if (!pkt)
        return nullptr;
    frame = av_frame_alloc();
    if (!frame)
        return nullptr;
    pkt->size = length;
    pkt->data = data;
    if (avctx->width == 0)
    {
        avctx->width = width;
    }
    if (avctx->height == 0)
    {
        avctx->height = height;
    }

    int ret = avcodec_send_packet(avctx, pkt);
    if (ret < 0)
    {
        LOGE("Error avcodec_send_packet : %s\n", av_err2str(ret));
        return nullptr;
    }

    ret = avcodec_receive_frame(avctx, frame);
    if (ret < 0)
    {
        LOGE("Error avcodec_receive_frame : %s\n", av_err2str(ret));
        return nullptr;
    }

    AVFrame *pFrameYUV = av_frame_alloc();
    pFrameYUV->format = AV_PIX_FMT_YUV420P;
    pFrameYUV->width = width;
    pFrameYUV->height = height;

    uint8_t *out_buffer = (uint8_t *) av_malloc(
            static_cast<size_t>(av_image_get_buffer_size(AV_PIX_FMT_YUV420P, avctx->width,
                                                         avctx->height, 1)));
    av_image_fill_arrays(pFrameYUV->data, pFrameYUV->linesize, out_buffer,
                         AV_PIX_FMT_YUV420P, avctx->width, avctx->height, 1);

    SwsContext *img_convert_ctx = sws_getContext(avctx->width, avctx->height, avctx->pix_fmt,
                                                 avctx->width, avctx->height, AV_PIX_FMT_YUV420P,
                                                 SWS_BILINEAR, NULL, NULL, NULL);
    sws_scale(img_convert_ctx, frame->data, frame->linesize, 0,
              avctx->height, pFrameYUV->data, pFrameYUV->linesize);


    int height_half = height / 2, width_half = width / 2;
    int y_wrap = pFrameYUV->linesize[0];
    int u_wrap = pFrameYUV->linesize[1];
    int v_wrap = pFrameYUV->linesize[2];

    uint8_t *y_buf = pFrameYUV->data[0];
    uint8_t *u_buf = pFrameYUV->data[1];
    uint8_t *v_buf = pFrameYUV->data[2];

    size_t yuvLength = width * height + width_half * height_half * 2;

    uint8_t *buf = static_cast<uint8_t *>(malloc(yuvLength));
    memcpy(buf, y_buf, width * height);
    memcpy(buf + height * width, u_buf, width_half * height_half);
    memcpy(buf + height * width + width_half * height_half, v_buf, width_half * height_half);

    sws_freeContext(img_convert_ctx);
    free(out_buffer);
    av_frame_free(&frame);
    av_frame_free(&pFrameYUV);
    av_packet_free(&pkt);
    return buf;
}

bool ImageToYUV::decode(const char *filename, const int width, const int height,
                            const char *outfilename)
{
    AVCodec *codec;
    AVCodecContext *avctx = NULL;
    FILE *f;
    AVFrame *frame;
    AVPacket *pkt;

    pkt = av_packet_alloc();
    if (!pkt)
        return false;

    LOGD("infile : %s", filename);

    AVCodecID id = AV_CODEC_ID_MJPEG;
    codec = avcodec_find_decoder(id);
    if (!codec)
    {
        LOGE("Could not find encoder for '%s'", avcodec_get_name(id));
        return false;
    }

    avctx = avcodec_alloc_context3(codec);
    if (!avctx)
    {
        LOGE("Could not allocate video codec context\n");
        return false;
    }

    if (avctx->width == 0)
    {
        avctx->width = width;
    }
    if (avctx->height == 0)
    {
        avctx->height = height;
    }

    if (avcodec_open2(avctx, codec, NULL) < 0)
    {
        LOGE("Could not open codec\n");
        return false;
    }

    f = fopen(filename, "rb");
    if (!f)
    {
        LOGE("Could not open %s\n", filename);
        return false;
    }
    fseek(f, 0, SEEK_END);
    long fsize = ftell(f);
    fseek(f, 0, SEEK_SET);
    uint8_t inbuf[fsize];

    frame = av_frame_alloc();
    if (!frame)
    {
        LOGE("Could not allocate video frame\n");
        return false;
    }

    pkt->size = static_cast<int>(fread(inbuf, sizeof(uint8_t), static_cast<size_t>(fsize), f));
    fclose(f);
    if (pkt->size == 0)
        return false;
    pkt->data = inbuf;

    int ret = avcodec_send_packet(avctx, pkt);
    if (ret < 0)
    {
        LOGE("Error avcodec_send_packet : %s\n", av_err2str(ret));
        return false;
    }

    ret = avcodec_receive_frame(avctx, frame);
    if (ret < 0)
    {
        LOGE("Error avcodec_receive_frame : %s\n", av_err2str(ret));
        return false;
    }

    AVFrame *pFrameYUV = av_frame_alloc();
    pFrameYUV->format = AV_PIX_FMT_YUV420P;
    pFrameYUV->width = avctx->width;
    pFrameYUV->height = avctx->height;

    uint8_t *out_buffer = (uint8_t *) av_malloc(
            static_cast<size_t>(av_image_get_buffer_size(AV_PIX_FMT_YUV420P, avctx->width,
                                                         avctx->height, 1)));
    av_image_fill_arrays(pFrameYUV->data, pFrameYUV->linesize, out_buffer,
                         AV_PIX_FMT_YUV420P, avctx->width, avctx->height, 1);

    SwsContext *img_convert_ctx = sws_getContext(avctx->width, avctx->height, avctx->pix_fmt,
                                                 avctx->width, avctx->height, AV_PIX_FMT_YUV420P,
                                                 SWS_BILINEAR, NULL, NULL, NULL);
    sws_scale(img_convert_ctx, frame->data, frame->linesize, 0,
              avctx->height, pFrameYUV->data, pFrameYUV->linesize);

    yuv420p_save(pFrameYUV, outfilename);

    free(out_buffer);
    sws_freeContext(img_convert_ctx);
    avcodec_free_context(&avctx);
    av_frame_free(&frame);
    av_frame_free(&pFrameYUV);
    av_packet_free(&pkt);
    return true;
}

void
ImageToYUV::yuv420p_save(AVFrame *pFrame, const char *outfilename)
{
    FILE *pfout = fopen(outfilename, "wb+");
    if (!pfout)
    {
        LOGE("Could not open %s\n", outfilename);
        return;
    }


    int i = 0;

    int width = pFrame->width, height = pFrame->height;
    int height_half = height / 2, width_half = width / 2;
    int y_wrap = pFrame->linesize[0];
    int u_wrap = pFrame->linesize[1];
    int v_wrap = pFrame->linesize[2];

    uint8_t *y_buf = pFrame->data[0];
    uint8_t *u_buf = pFrame->data[1];
    uint8_t *v_buf = pFrame->data[2];

    size_t yuvLength = width * height + width_half * height_half * 2;

    uint8_t *buf = static_cast<uint8_t *>(malloc(yuvLength));
    memcpy(buf, y_buf, width * height);
    memcpy(buf + height * width, u_buf, width_half * height_half);
    memcpy(buf + height * width + width_half * height_half, v_buf, width_half * height_half);


//    char *name = static_cast<char *>(malloc(FILENAME_MAX));
//    strcpy(name, outfilename);
//    strcat(name, ".2");
//    FILE *n = fopen(name, "wb+");
//    fwrite(buf, 1, yuvLength, n);
//    fclose(n);

    fwrite(buf, 1, yuvLength, pfout);
    fclose(pfout);
}