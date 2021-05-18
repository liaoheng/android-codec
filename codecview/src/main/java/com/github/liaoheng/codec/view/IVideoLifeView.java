package com.github.liaoheng.codec.view;

import android.view.Surface;

/**
 * 视频控件生命状态接口
 *
 * @author liaoheng
 * @version 2018-05-17 16:19
 */
public interface IVideoLifeView {
    /**
     * 创建
     */
    void create(Surface surface);

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
     * 销毁
     */
    void destroy();

}
