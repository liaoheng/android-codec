package com.github.liaoheng.codec.video;

import com.github.liaoheng.codec.model.VideoCodecConfig;

/**
 * 视频工具类状态回调
 *
 * @author liaoheng
 * @version 2018-05-18 09:52
 */
public interface VideoCodecCallBack {
    /**
     * 开始
     */
    void start(VideoCodecConfig config);

    /**
     * 停止
     */
    void stop();

    /**
     * 清屏
     */
    void clear();


    void error(Exception e);
}
