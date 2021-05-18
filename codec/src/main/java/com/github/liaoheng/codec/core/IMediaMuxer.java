package com.github.liaoheng.codec.core;

import com.github.liaoheng.codec.model.VideoCodecConfig;

/**
 * 混编音频与视频
 *
 * @author liaoheng
 * @version 2018-06-22 15:11
 */
@Deprecated
public interface IMediaMuxer {

    String getFilePath();

    boolean isRunning();

    boolean start(VideoCodecConfig config, String outFile);

    void writeVideoData(byte[] data);

    void writeAudioData(byte[] data);

    void release();
}
