package com.github.liaoheng.codec.video;

import android.view.Surface;

import androidx.annotation.CallSuper;

import com.github.liaoheng.codec.core.IMediaMuxer;
import com.github.liaoheng.codec.mediacodec.IMediaCodec;
import com.github.liaoheng.codec.mediacodec.avc.AVCMediaCodec;
import com.github.liaoheng.codec.mediacodec.hevc.HEVCMediaCodec;
import com.github.liaoheng.codec.model.InitCodecException;
import com.github.liaoheng.codec.model.VideoCodecConfig;
import com.github.liaoheng.codec.model.VideoCodecID;
import com.github.liaoheng.common.thread.IWorkProcessThread;
import com.github.liaoheng.common.thread.WorkProcessQueueHelper;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author liaoheng
 * @version 2018-11-14 14:47
 */
public abstract class BaseVideoCodec implements IVideoCodec {
    protected final String TAG = getClass().getSimpleName();
    private String mPrefix;
    private IMediaCodec mMediaCodec; // 编码器
    private VideoCodecConfig mVideoHeader;//解码视频配置
    private WorkProcessQueueHelper<byte[]> mWorkProcessQueueHelper;
    private AtomicBoolean isPause = new AtomicBoolean(false);//暂停状态
    protected VideoCodecCallBack mVideoCodecCallBack;
    protected IMediaMuxer mMediaMuxer;

    public void initMediaMuxer(IMediaMuxer mediaMuxer) {
        mMediaMuxer = mediaMuxer;
    }

    protected BaseVideoCodec(String user) {
        mPrefix = user;
    }

    @Override
    public void setCallBack(VideoCodecCallBack callBack) {
        mVideoCodecCallBack = callBack;
    }

    @Override
    public VideoCodecCallBack getCallBack() {
        return mVideoCodecCallBack;
    }

    public String getUser() {
        return mPrefix;
    }

    public void putQueue(byte[] data) {
        mWorkProcessQueueHelper.putQueue(data);
    }

    protected byte[] takeQueue() {
        return mWorkProcessQueueHelper.takeQueue();
    }

    public void clearQueue() {
        mWorkProcessQueueHelper.clearQueue();
    }

    @Override
    public IMediaCodec getMediaCodec() {
        return mMediaCodec;
    }

    protected boolean isRunningMediaCodec() {
        return isMediaCodec() && mMediaCodec.isRunning();
    }

    protected void initEncoderMediaCodec() throws IOException {
        if (isMediaCodec() && mMediaCodec.isRelease()) {
            mMediaCodec.initEncoder();
        }
    }

    protected void initDecoderMediaCodec() throws IOException {
        if (isMediaCodec() && mMediaCodec.isRelease()) {
            mMediaCodec.initDecoder();
        }
    }

    protected void startEncoderMediaCodec(VideoCodecConfig config) {
        if (isMediaCodec()) {
            mMediaCodec.startEncoder(config);
        }
    }

    protected void startDecoderMediaCodec(Surface surface, VideoCodecConfig config) {
        if (isMediaCodec()) {
            mMediaCodec.startDecoder(surface, config);
        }
    }

    @CallSuper
    @Override
    public void init(Object obj) {
        mWorkProcessQueueHelper = new WorkProcessQueueHelper<>(getWorkThread());
    }

    protected void switchMediaCodecByConfig(VideoCodecConfig config) {
        if (mMediaCodec != null && !mMediaCodec.isRelease()) {
            if (mMediaCodec.getVideoCodecId() != config.getVideoCodecIDInt()) {
                mMediaCodec.release();
                initMediaCodec(config);
            }
        } else {
            initMediaCodec(config);
        }
    }

    private void initMediaCodec(VideoCodecConfig config) {
        if (VideoCodecID.HEVC.equals(config.getVideoCodecID())) {
            mMediaCodec = new HEVCMediaCodec(getUser());
        } else {
            mMediaCodec = new AVCMediaCodec(getUser());
        }
    }

    protected void stopMediaCodec() {
        if (isMediaCodec() && mMediaCodec.isRunning()) {
            mMediaCodec.stop();
        }
    }

    @Override
    public VideoCodecConfig getVideoHeader() {
        return mVideoHeader;
    }

    @Override
    public void setVideoHeader(VideoCodecConfig config) {
        mVideoHeader = config;
    }

    boolean isMediaCodec() {
        return mMediaCodec != null;
    }

    protected boolean isVideoHeader() {
        return mVideoHeader != null;
    }

    public boolean isPause() {
        return isPause.get();
    }

    @Override
    public void stop() {
        if (isMediaCodec()) {
            mMediaCodec.stop();
        }
        getWorkThread().stop();
        clearQueue();
    }

    protected void stopCallBack() {
        if (mVideoCodecCallBack == null) {
            return;
        }
        mVideoCodecCallBack.stop();
    }

    @Override
    public void release() {
        if (isMediaCodec()) {
            mMediaCodec.release();
        }
        getWorkThread().stop();
        mVideoHeader = null;
        clearQueue();
    }

    @Override
    public boolean start(VideoCodecConfig config) {
        return play(config, false);
    }

    private boolean play(VideoCodecConfig config, boolean isRestart) {
        try {
            return startAction(config, isRestart);
        } catch (IOException e) {
            getWorkThread().stop();
            if (mVideoCodecCallBack != null) {
                mVideoCodecCallBack.error(new InitCodecException(getUser(), e));
            }
        }
        return false;
    }

    protected void startCallBack(VideoCodecConfig config) {
        if (mVideoCodecCallBack == null) {
            return;
        }
        mVideoCodecCallBack.start(config);
    }

    @Override
    public void resume() {
        isPause.set(false);
    }

    @Override
    public boolean restart() {
        isPause.set(false);
        clearQueue();
        if (isRunning() || !isVideoHeader()) {
            return false;
        }
        return play(getVideoHeader(), true);
    }

    @Override
    public void pause() {
        isPause.set(true);
    }

    /**
     * 初始化编码
     */
    protected abstract void initCodec(VideoCodecConfig config) throws IOException;

    protected abstract boolean startAction(VideoCodecConfig header, boolean isRestart) throws IOException;

    protected abstract IWorkProcessThread getWorkThread();
}
