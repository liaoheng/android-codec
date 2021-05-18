package com.github.liaoheng.codec.view;


import com.github.liaoheng.codec.model.VideoCodecConfig;
import com.github.liaoheng.codec.video.IVideoCodec;

import java.util.Map;

/**
 * 使用视频工具类的View控件接口
 *
 * @author liaoheng
 * @version 2018-05-18 10:02
 */
public interface IVideoCodecView {
    /**
     * 初始化视频工具类
     */
    void initVideoCodec(IVideoCodec codec);

    IVideoCodec getVideoCodec();

    /**
     * 得到视频参数
     */
    VideoCodecConfig getConfig();

    String getSign();

    void restore(Map<String, Object> configs);

    void saved(Map<String, Object> configs);
}
