package com.github.liaoheng.codec.model;

/**
 * 发送音视频数据回调
 *
 * @author liaoheng
 * @version 2018-08-20 16:17
 */
public interface IDataServerSend {
    /**
     * 发送视频数据
     *
     * @param videoFrame 视频数据
     */
    void sendVideoData(VideoFrame videoFrame);

    /**
     * 发送音数据
     *
     * @param audioBytes 音频数据
     */
    void sendAudioData(byte[] audioBytes);
}
