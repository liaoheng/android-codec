package com.github.liaoheng.codec.audio;

import com.github.liaoheng.codec.core.IMediaMuxer;
import com.github.liaoheng.codec.model.IDataServerSend;

/**
 * 音频工具类接口
 *
 * @author liaoheng
 * @version 2018-05-21 10:37
 */
@Deprecated
public interface IAudioCodec {

    /**
     * 初始化
     */
    void init();

    /**
     * 开始
     */
    void start();

    /**
     * 重新开始
     */
    void restart();

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
     * 设置麦克风打开/关闭状态
     *
     * @param onOff true：打开；false：关闭
     */
    void setMicrophoneSwitch(boolean onOff);

    /**
     * 设置扬声器打开/关闭状态
     *
     * @param onOff true：打开；false：关闭
     */
    void setLoudspeakerSwitch(boolean onOff);

    /**
     * 麦克风打开/关闭状态
     *
     * @return true：打开；false：关闭
     */
    boolean isMicrophoneSwitch();

    /**
     * 扬声器打开/关闭状态
     *
     * @return true：打开；false：关闭
     */
    boolean isLoudspeakerSwitch();

    /**
     * 获得混编(本地与远端)之后的音频数据
     */
    byte[] getAudioData();

    /**
     * 远端音频数据
     *
     * @param bytes     音频数据
     */
    void receivedAudioData(byte[] bytes);

    void setDataServerSend(IDataServerSend send);

    void initMediaMuxer(IMediaMuxer mediaMuxer);
}
