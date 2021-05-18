package com.github.liaoheng.codec.mediacodec.avc;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.view.Surface;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.github.liaoheng.codec.model.VideoCodecConfig;
import com.github.liaoheng.codec.util.ACUtils;
import com.github.liaoheng.codec.mediacodec.IMediaCodec;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 对硬编码({@link MediaCodec})封装，主要是维护对应状态
 *
 * @author liaoheng
 * @version 2018-05-17 12:50
 */
public class AVCMediaCodec implements IMediaCodec {
    private final String TAG = AVCMediaCodec.class.getSimpleName();
    private String mPrefix;
    private MediaCodec mMediaCodec;
    private AtomicBoolean mRunning = new AtomicBoolean(false);//是否在运行中
    private AtomicBoolean isRelease = new AtomicBoolean(true);
    private int mVideoCodecID;

    public AVCMediaCodec(String prefix) {
        mPrefix = prefix;
    }

    @Override
    public boolean isNalHeaderFrame(byte[] outData) {//判断sps 0x67
        return (outData[0] == 0 && outData[1] == 0 && outData[2] == 0
                && outData[3] == 1 && outData[4] == 103)
                || (outData[0] == 0 && outData[1] == 0 && outData[2] == 1
                && outData[3] == 103);
    }

    @Override
    public boolean isIFrame(byte[] outData) { // i帧 0x65
        return (outData[0] == 0 && outData[1] == 0 && outData[2] == 0
                && outData[3] == 1 && outData[4] == 101)
                || (outData[0] == 0 && outData[1] == 0 && outData[2] == 1
                && outData[3] == 101);
    }

    @Override
    public void initEncoder() throws IOException {
        String encoderName = AVCCodecManager.get().getEncoderName();
        mMediaCodec = MediaCodec.createByCodecName(encoderName);
        isRelease.set(false);
        ACUtils.ealog(TAG,mPrefix,"initEncoder : %s", encoderName);
        //CLog.alog().d(TAG, "initEncoder : %s", encoderName);
    }

    @Override
    public void startEncoder(VideoCodecConfig config) {
        if (mMediaCodec != null) {
            if (isRunning()) {
                stop();
            }
            mVideoCodecID = config.getVideoCodecIDInt();
            MediaFormat format = AVCCodecManager.get().getEncoderMediaFormat(config);
            mMediaCodec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            mMediaCodec.start();
            mRunning.set(true);
            ACUtils.ealog(TAG,mPrefix,"startEncoder  config: %s", config);
            //CLog.alog().d(TAG, "startEncoder  config: %s", config);
        }
    }

    @Override
    public void initDecoder() throws IOException {
        String decoderName = AVCCodecManager.get().getDecoderName();
        mMediaCodec = MediaCodec.createByCodecName(decoderName);
        isRelease.set(false);
        ACUtils.ealog(TAG,mPrefix,"initDecoder : %s", decoderName);
        //CLog.alog().d(TAG, "initDecoder : %s", decoderName);
    }

    @Override
    public void startDecoder(Surface surface, VideoCodecConfig config) {
        if (mMediaCodec != null) {
            if (isRunning()) {
                stop();
            }
            mVideoCodecID = config.getVideoCodecIDInt();
            MediaFormat format = AVCCodecManager.get().getDecoderMediaFormat(config);
            mMediaCodec.configure(format, surface, null, 0);
            mMediaCodec.start();
            mRunning.set(true);
            ACUtils.ealog(TAG,mPrefix,"startDecoder config: %s", config);
            //CLog.alog().d(TAG, "startDecoder  config: %s", config);
        }
    }

    @Override
    public void stop() {
        if (mMediaCodec != null && isRunning()) {
            mRunning.set(false);
            mMediaCodec.stop();
            ACUtils.ealog(TAG,mPrefix,"stop");
            //CLog.alog().d(TAG, "stop");
        }
    }

    @Override
    public void release() {
        if (mMediaCodec != null) {
            mRunning.set(false);
            isRelease.set(true);
            mMediaCodec.release();
            ACUtils.ealog(TAG, mPrefix, "release");
            //CLog.alog().d(TAG, "release");
        }
        mMediaCodec = null;
    }

    @Override
    public int getEncoderColorFormat() {
        return AVCCodecManager.get().getEncoderColorFormat();
    }

    @Override
    public final int dequeueInputBuffer(long timeoutUs) {
        if (!isRunning()) {
            return -1;
        }
        int r = -1;
        try {
            r = getMediaCodec().dequeueInputBuffer(timeoutUs);
        } catch (IllegalStateException ignored) {
        }
        return r;
    }

    @Override
    public final int dequeueOutputBuffer(
            @NonNull MediaCodec.BufferInfo info, long timeoutUs) {
        if (!isRunning()) {
            return -1;
        }
        return getMediaCodec().dequeueOutputBuffer(info, timeoutUs);
    }

    @Override
    public final void releaseOutputBuffer(int index, boolean render) {
        if (!isRunning()) {
            return;
        }
        getMediaCodec().releaseOutputBuffer(index, render);
    }

    @Override
    public final void queueInputBuffer(
            int index,
            int offset, int size, long presentationTimeUs, int flags)
            throws MediaCodec.CryptoException {
        if (!isRunning()) {
            return;
        }
        getMediaCodec().queueInputBuffer(index, offset, size, presentationTimeUs, flags);
    }

    @Override
    @Nullable
    public ByteBuffer getInputBuffer(int index) {
        if (!isRunning()) {
            return null;
        }
        return getMediaCodec().getInputBuffer(index);
    }

    @Nullable
    @Override
    public ByteBuffer getOutputBuffer(int index) {
        if (!isRunning()) {
            return null;
        }
        return getMediaCodec().getOutputBuffer(index);
    }

    @Override
    public MediaCodec getMediaCodec() {
        return mMediaCodec;
    }

    @Override
    public synchronized boolean isRunning() {
        return mRunning.get();
    }

    @Override
    public boolean isRelease() {
        return isRelease.get();
    }

    @Override
    public int getVideoCodecId() {
        return mVideoCodecID;
    }
}
