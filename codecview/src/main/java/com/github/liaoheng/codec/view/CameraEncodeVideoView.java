package com.github.liaoheng.codec.view;

import android.content.Context;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.view.Surface;
import android.view.View;

import com.github.liaoheng.codec.model.CameraCodecConfig;
import com.github.liaoheng.codec.model.VideoCodecConfig;
import com.github.liaoheng.codec.video.encode.IEncodeVideoCodec;
import com.github.liaoheng.codec.video.IVideoCodec;
import com.github.liaoheng.codec.video.NV21Convertor;
import com.github.liaoheng.common.util.SystemException;

/**
 * 本地摄像头视频数据展示
 *
 * @author liaoheng
 * @version 2018-05-14 15:00
 */
public class CameraEncodeVideoView extends BaseVideoView implements ICameraView {

    public CameraEncodeVideoView(Context context) {
        super(context);
    }

    public CameraEncodeVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CameraEncodeVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public CameraEncodeVideoView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    private CameraHelper mCameraHelper;
    private ICameraView.CameraCallback mCameraCallBack;
    private NV21Convertor mConvertor;

    /**
     * 设置摄像头状态回调
     */
    @Override
    public void setCameraCallBack(ICameraView.CameraCallback callBack) {
        mCameraCallBack = callBack;
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
                mCameraHelper.destroy();
                createCamera();
                startCameraPreview();
                if (mCameraCallBack != null) {
                    mCameraCallBack.start(config);
                }
                getVideoCodec().start(config);
                mConvertor = NV21Convertor.create(getConfig(), getVideoCodec().getMediaCodec().getEncoderColorFormat());
            }
        } else {
            getVideoCodec().setVideoHeader(config);
        }
    }

    @Override
    public CameraCodecConfig getConfig() {
        return super.getConfig() == null ? null : (CameraCodecConfig) super.getConfig();
    }

    @Override
    public void initVideoCodec(IVideoCodec codec) {
        setVideoCodec(codec);
    }

    @Override
    protected void baseInit() {
        super.baseInit();
        mCameraHelper = new CameraHelper(getContext(), (data, camera) -> {
            //必须调用，这里共用一个buffer:https://blog.csdn.net/lb377463323/article/details/53338045
            camera.addCallbackBuffer(data);
            if (!isRunningVideoCodec() || data == null) {
                return;
            }
            if (mConvertor != null) {
                try {
                    ((IEncodeVideoCodec) getVideoCodec()).onPreviewFrame(mConvertor.convert(data));
                } catch (Exception ignored) {
                }
            }
        });
    }

    /**
     * 切换摄像头方向
     */
    @Override
    public void toggle() {
        mCameraHelper.destroy();
        mCameraHelper.reverse();
        createCamera();
        startCameraPreview();
        if (mCameraCallBack != null) {
            mCameraCallBack.toggle(getConfig());
        }
    }

    @Override
    public View getView() {
        return this;
    }

    @Override
    public void create(Surface surface) {
        createCamera();
        if (mCameraCallBack != null) {
            mCameraCallBack.create(getConfig());
        }
        super.create(null);
        start();
    }

    private void createCamera() {
        try {
            mCameraHelper.createCamera(getConfig().getWidth(), getConfig().getHeight());
            Camera.Size supportMaxSize = mCameraHelper.getSupportSize();
            if (getConfig() == null) {
                return;
            }
            getConfig().setWidth(supportMaxSize.width);
            getConfig().setHeight(supportMaxSize.height);
        } catch (SystemException e) {
            if (mCameraCallBack != null) {
                mCameraCallBack.error(e);
            }
        }
    }

    @Override
    public void destroy() {
        super.destroy();
        mConvertor = null;
        mCameraHelper.destroy();
    }

    private void startCameraPreview() {
        try {
            mCameraHelper.startPreview(getSurfaceTexture());
            updateTextureViewSizeCenterCrop(getConfig().getWidth(), getConfig().getHeight());
        } catch (SystemException e) {
            if (mCameraCallBack != null) {
                mCameraCallBack.error(e);
            }
        }
    }

    @Override
    public void start() {
        if (isVideoCodec()) {
            getVideoCodec().start(getConfig());
            mConvertor = NV21Convertor.create(getConfig(), getVideoCodec().getMediaCodec().getEncoderColorFormat());
        }
        startCameraPreview();
        if (mCameraCallBack != null) {
            mCameraCallBack.start(getConfig());
        }
    }

    @Override
    public void stop() {
        super.stop();
        mCameraHelper.stopPreview();
        if (mCameraCallBack != null) {
            mCameraCallBack.stop();
        }
    }

}
