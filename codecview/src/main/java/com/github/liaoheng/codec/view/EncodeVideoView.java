package com.github.liaoheng.codec.view;

import android.content.Context;
import android.util.AttributeSet;

import com.github.liaoheng.codec.model.CameraCodecConfig;
import com.github.liaoheng.codec.model.VideoCodecConfig;

/**
 * 本地摄像头视频数据展示
 *
 * @author liaoheng
 * @version 2018-05-14 15:00
 */
public class EncodeVideoView extends BaseVideoView {

    public EncodeVideoView(Context context) {
        super(context);
    }

    public EncodeVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public EncodeVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public EncodeVideoView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    /**
     * 更新视频参数
     */
    public void updateConfig(CameraCodecConfig config) {
        VLog.alog().d(TAG, "updateConfig : %s", config);
        VideoCodecConfig oldConfig = getConfig();
        if (isRunningVideoCodec()) {
            if (config.getWidth() != oldConfig.getWidth() && config.getHeight() != oldConfig.getHeight()) {
                getVideoCodec().stop();
                getVideoCodec().start(config);
            }
        } else {
            getVideoCodec().setVideoHeader(config);
        }
    }

}
