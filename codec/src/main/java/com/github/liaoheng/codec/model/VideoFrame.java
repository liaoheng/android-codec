package com.github.liaoheng.codec.model;

/**
 * @author liaoheng
 * @version 2019-04-12 10:37
 */
public class VideoFrame {

    private byte[] data;
    private int frameType = 0;  //帧类型：i,p帧=1 ;other帧=0
    private int mediaType;//编码格式: 0:H264 1:H265

    public VideoFrame() {
    }

    public VideoFrame(byte[] data, int frameType, int mediaType) {
        this.data = data;
        this.frameType = frameType;
        this.mediaType = mediaType;
    }

    public void setMediaType(int mediaType) {
        this.mediaType = mediaType;
    }

    public void setFrameType(int frameType) {
        this.frameType = frameType;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public byte[] getData() {
        return data;
    }

    public VideoFrame copy() {
        return new VideoFrame(data, frameType, mediaType);
    }
}
