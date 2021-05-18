//
// Created by liaoheng on 2018/7/9.
//


#include "CaptureImage.h"


bool CaptureImage::captureH264(const char *filename, int width, int height, unsigned char *data,
                               int length) {
    return capture(filename, width, height, data, length, AV_CODEC_ID_H264);
}

bool CaptureImage::captureH265(const char *filename, int width, int height, unsigned char *data,
                               int length) {
    return capture(filename, width, height, data, length, AV_CODEC_ID_HEVC);
}

bool CaptureImage::capture(const char *filename, int width, int height, unsigned char *data,
                           int length, enum AVCodecID id) {
    AVCodec *pCodec;
    AVCodecContext *pCodecCtx;
    AVFrame *pFrame;
//    av_register_all();
//    avformat_network_init();
    AVPacket pkt = {0};


    pCodec = avcodec_find_decoder(id);
    if (pCodec == NULL) {
        LOGE("avcode find decoder failed!\n");
        return false;
    }

    pCodecCtx = avcodec_alloc_context3(pCodec);

    int ret = avcodec_open2(pCodecCtx, pCodec, NULL);
    if (ret < 0) {
        LOGE("Could not open video codec: %s\n", av_err2str(ret));
        return false;
    }

    pFrame = av_frame_alloc();

    av_init_packet(&pkt);

    pkt.size = length;
    pkt.data = data;
    pkt.flags |= AV_PKT_FLAG_KEY;

    int frameFinished;
    int r = -1;
    ret = avcodec_decode_video2(pCodecCtx, pFrame, &frameFinished, &pkt);
    if (ret < 0) {
        LOGE("Could not avcodec_decode_video2 : %s\n", av_err2str(ret));
        return false;
    }
    if (frameFinished) {
        r = writeJPEG(filename, pFrame, width, height);
    }

    av_free_packet(&pkt);
    av_free(pFrame);
    avcodec_close(pCodecCtx);
    return r == 0;
}


/**
 * 将AVFrame(YUV420格式)保存为JPEG格式的图片
 *
 * @param width YUV420的宽
 * @param height YUV42的高
 *
 */
int CaptureImage::writeJPEG(const char *out_file, AVFrame *pFrame, int width, int height) {

    // 分配AVFormatContext对象
    AVFormatContext *pFormatCtx = avformat_alloc_context();

    // 设置输出文件格式
    pFormatCtx->oformat = av_guess_format("mjpeg", NULL, NULL);
    // 创建并初始化一个和该url相关的AVIOContext
    if (avio_open(&pFormatCtx->pb, out_file, AVIO_FLAG_READ_WRITE) < 0) {
        LOGE("Couldn't open output file.");
        return -1;
    }

    // 构建一个新stream
    AVStream *pAVStream = avformat_new_stream(pFormatCtx, 0);
    if (pAVStream == NULL) {
        LOGE("Couldn't AVStream");
        return -1;
    }

    // 设置该stream的信息
    AVCodecContext *pCodecCtx = pAVStream->codec;

    pCodecCtx->codec_id = pFormatCtx->oformat->video_codec;
    pCodecCtx->codec_type = AVMEDIA_TYPE_VIDEO;
    pCodecCtx->pix_fmt = AV_PIX_FMT_YUVJ420P;
    pCodecCtx->width = width;
    pCodecCtx->height = height;
    pCodecCtx->time_base.num = 1;
    pCodecCtx->time_base.den = 25;

    // Begin Output some information
    av_dump_format(pFormatCtx, 0, out_file, 1);
    // End Output some information

    // 查找解码器
    AVCodec *pCodec = avcodec_find_encoder(pCodecCtx->codec_id);
    if (!pCodec) {
        LOGE("Codec not found.");
        return -1;
    }
    // 设置pCodecCtx的解码器为pCodec
    if (avcodec_open2(pCodecCtx, pCodec, NULL) < 0) {
        LOGE("Could not open codec.");
        return -1;
    }

    //Write Header
    avformat_write_header(pFormatCtx, NULL);

    int y_size = pCodecCtx->width * pCodecCtx->height;

    //Encode
    // 给AVPacket分配足够大的空间
    AVPacket pkt;
    av_new_packet(&pkt, y_size * 3);

    //
    int got_picture = 0;
    int ret = avcodec_encode_video2(pCodecCtx, &pkt, pFrame, &got_picture);
    if (ret < 0) {
        LOGE("Encode Error.\n");
        return -1;
    }
    if (got_picture == 1) {
        //pkt.stream_index = pAVStream->index;
        ret = av_write_frame(pFormatCtx, &pkt);
    }

    av_free_packet(&pkt);

    //Write Trailer
    av_write_trailer(pFormatCtx);

    LOGD("Encode Successful.\n");

    if (pAVStream) {
        avcodec_close(pAVStream->codec);
    }
    avio_close(pFormatCtx->pb);
    avformat_free_context(pFormatCtx);

    return 0;
}