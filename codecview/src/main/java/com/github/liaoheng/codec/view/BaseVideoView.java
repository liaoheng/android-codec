package com.github.liaoheng.codec.view;

import android.content.Context;
import android.graphics.PointF;
import android.graphics.SurfaceTexture;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.Surface;
import android.view.TextureView;

import com.github.liaoheng.codec.model.VideoCodecConfig;
import com.github.liaoheng.codec.video.IVideoCodec;
import com.github.liaoheng.codec.video.VideoCodecCallBack;
import com.github.liaoheng.common.util.HandlerUtils;

import java.util.Map;

/**
 * 视频数据展示
 *
 * @author liaoheng
 * @version 2018-05-15 15:13
 */
public abstract class BaseVideoView
        extends TextureView implements TextureView.SurfaceTextureListener, IVideoLifeView, IVideoCodecView {
    protected final String TAG = this.getClass().getSimpleName();

    public BaseVideoView(Context context) {
        super(context);
        baseInit();
    }

    public BaseVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        baseInit();
    }

    public BaseVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        baseInit();
    }

    public BaseVideoView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        baseInit();
    }

    private boolean isRestart;
    private boolean isInit;
    private IVideoCodec mVideoCodec;

    @Override
    public IVideoCodec getVideoCodec() {
        return mVideoCodec;
    }

    protected void setVideoCodec(IVideoCodec videoCodec) {
        mVideoCodec = videoCodec;
    }

    public boolean isVideoCodec() {
        return mVideoCodec != null;
    }

    public boolean isRunningVideoCodec() {
        return isVideoCodec() && mVideoCodec.isRunning();
    }

    @CallSuper
    protected void baseInit() {
        setOpaque(false);
        setSurfaceTextureListener(this);
    }

    @Override
    public String getSign() {
        return TAG;
    }

    @Override
    public void initVideoCodec(IVideoCodec codec) {
        setVideoCodec(codec);
        if (codec != null) {
            codec.setCallBack(new VideoCodecCallBack() {

                @Override
                public void start(final VideoCodecConfig config) {
                    updateTextureViewSizeCenter(config.getWidth(), config.getHeight());
                }

                @Override
                public void stop() {
                    clear();
                }

                @Override
                public void clear() {
                    VUtils.clearSurface(getSurfaceTexture());
                }

                @Override
                public void error(Exception e) {

                }
            });
        }
    }

    @Override
    public VideoCodecConfig getConfig() {
        return mVideoCodec == null ? null : mVideoCodec.getVideoHeader();
    }

    @Override
    public void create(Surface surface) {
        if (!isVideoCodec()) {
            return;
        }
        getVideoCodec().init(surface);
    }

    @Override
    public void start() {
        if (!isVideoCodec()) {
            return;
        }
        getVideoCodec().start(getConfig());
    }

    @Override
    public void resume() {
        if (!isVideoCodec()) {
            return;
        }
        getVideoCodec().resume();
    }

    @Override
    public void restart() {
        if (isInit) {
            isRestart = false;
            if (isVideoCodec()) {
                getVideoCodec().restart();
            }
        } else {
            isRestart = true;
        }
    }

    @Override
    public void pause() {
        if (!isVideoCodec()) {
            return;
        }
        getVideoCodec().pause();
    }

    @Override
    public void stop() {
        if (!isVideoCodec()) {
            return;
        }
        getVideoCodec().stop();
    }

    @Override
    public void destroy() {
        if (!isVideoCodec()) {
            return;
        }
        getVideoCodec().release();
    }

    @Override
    public void restore(Map<String, Object> configs) {
        getVideoCodec().setVideoHeader((VideoCodecConfig) configs.get(getSign()));
    }

    @Override
    public void saved(Map<String, Object> configs) {
        configs.put(getSign(), getConfig());
    }

    /**
     * 移动位置
     */
    public void setPosition(@NonNull PointF position) {
        setX(position.x);
        setY(position.y);
    }

    protected void updateTextureViewSizeCenter(int videoWidth, int videoHeight) {
        HandlerUtils.post(() -> VUtils.updateTextureViewSizeCenter(this, videoWidth, videoHeight));
    }

    protected void updateTextureViewSizeCenterCrop(int videoWidth, int videoHeight) {
        HandlerUtils.post(() -> VUtils.updateTextureViewSizeCenterCrop(this, videoWidth, videoHeight));
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        VLog.alog().d(TAG, "onSurfaceTextureAvailable");
        create(new Surface(surface));
        isInit = true;
        if (isRestart) {
            restart();
        }
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        VLog.alog().d(TAG, "onSurfaceTextureDestroyed");
        destroy();
        isInit = false;
        return true;
    }
}
