package com.github.liaoheng.codec.video;

import com.github.liaoheng.codec.core.IMediaMuxer;
import com.github.liaoheng.codec.model.VideoCodecConfig;
import com.github.liaoheng.codec.mediacodec.IMediaCodec;
import com.github.liaoheng.common.util.Callback;

import java.io.File;

/**
 * 视频工具类接口
 *
 * @author liaoheng
 * @version 2018-05-17 16:05
 */
public interface IVideoCodec {

    /**
     * 初始化
     */
    void init(Object obj);

    /**
     * 开始
     *
     * @param config 配置 {@link VideoCodecConfig}
     */
    boolean start(VideoCodecConfig config);

    /**
     * 重新开始
     */
    boolean restart();

    /**
     * 恢复
     */
    void resume();

    /**
     * 暂停
     */
    void pause();

    /**
     * 停止
     */
    void stop();

    /**
     * 释放
     */
    void release();

    /**
     * 是否运行中
     */
    boolean isRunning();

    /**
     * 状态回调
     *
     * @param callBack {@link VideoCodecCallBack}
     */
    void setCallBack(VideoCodecCallBack callBack);

    VideoCodecCallBack getCallBack();

    VideoCodecConfig getVideoHeader();

    IMediaCodec getMediaCodec();

    void setVideoHeader(VideoCodecConfig config);

    /**
     * 截图
     *
     * @param path 截图文件路径
     */
    void screenshot(File path, Callback<String> callback);


    void initMediaMuxer(IMediaMuxer mediaMuxer);
}
