package com.github.liaoheng.codec.view;

import android.view.View;

import com.github.liaoheng.codec.model.CameraCodecConfig;
import com.github.liaoheng.common.util.SystemException;

/**
 * @author liaoheng
 * @version 2018-12-27 10:04
 */
public interface ICameraView {
    void setCameraCallBack(CameraCallback callBack);

    void toggle();

    View getView();

    /**
     * 摄像头状态回调
     */
    interface CameraCallback {

        /**
         * 创建摄像头
         */
        void create(CameraCodecConfig config);

        /**
         * 切换摄像头方向
         */
        void toggle(CameraCodecConfig config);

        /**
         * 摄像头数据开始展示
         */
        void start(CameraCodecConfig config);

        /**
         * 摄像头数据结束展示
         */
        void stop();

        /**
         * 摄像头错误
         */
        void error(SystemException e);
    }
}
