package com.github.liaoheng.codec.mediacodec.hevc;

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
 * @author liaoheng
 * @version 2018-09-10 10:33
 */
public class HEVCMediaCodec implements IMediaCodec {
    private final String TAG = HEVCMediaCodec.class.getSimpleName();
    private String mPrefix;
    private MediaCodec mMediaCodec;
    private AtomicBoolean mRunning = new AtomicBoolean(false);//是否在运行中
    private AtomicBoolean isRelease = new AtomicBoolean(true);
    private int mVideoCodecID;

    public HEVCMediaCodec(String prefix) {
        mPrefix = prefix;
    }

    @Override
    public boolean isNalHeaderFrame(byte[] outData) {//判断vps 0x40
        return (outData[0] == 0 && outData[1] == 0 && outData[2] == 0
                && outData[3] == 1 && outData[4] == 64)
                || (outData[0] == 0 && outData[1] == 0 && outData[2] == 1
                && outData[3] == 64);
    }

    @Override
    public boolean isIFrame(byte[] outData) {// i帧 0x26
        return (outData[0] == 0 && outData[1] == 0 && outData[2] == 0
                && outData[3] == 1 && outData[4] == 38)
                || (outData[0] == 0 && outData[1] == 0 && outData[2] == 1
                && outData[3] == 38);
    }

    @Override
    public void initEncoder() throws IOException {
        String encoderName = HEVCCodecManager.get().getEncoderName();
        mMediaCodec = MediaCodec.createByCodecName(encoderName);
        isRelease.set(false);
        ACUtils.ealog(TAG, mPrefix, "initEncoder : %s", encoderName);
        //CLog.alog().d(TAG, "initEncoder : %s", encoderName);
    }

    @Override
    public void startEncoder(VideoCodecConfig config) {
        if (mMediaCodec != null) {
            if (isRunning()) {
                stop();
            }
            mVideoCodecID = config.getVideoCodecIDInt();
            MediaFormat mEncoderMediaFormat = HEVCCodecManager.get().getEncoderMediaFormat(config);
            mMediaCodec.configure(mEncoderMediaFormat, null, null,
                    MediaCodec.CONFIGURE_FLAG_ENCODE);
            mMediaCodec.start();
            mRunning.set(true);
            ACUtils.ealog(TAG, mPrefix, "startEncoder  config: %s", config);
            //L.alog().d(TAG, "startEncoder  config: %s", config);
        }
    }

    @Override
    public void initDecoder() throws IOException {
        String decoderName = HEVCCodecManager.get().getDecoderName();
        mMediaCodec = MediaCodec.createByCodecName(decoderName);
        isRelease.set(false);
        ACUtils.ealog(TAG, mPrefix, "initDecoder : %s", decoderName);
        //L.alog().d(TAG, "initDecoder : %s", decoderName);
    }

    @Override
    public void startDecoder(Surface surface, VideoCodecConfig config) {
        if (mMediaCodec != null) {
            if (isRunning()) {
                stop();
            }
            mVideoCodecID = config.getVideoCodecIDInt();
            MediaFormat mDecoderMediaFormat = HEVCCodecManager.get().getDecoderMediaFormat(config);
            mMediaCodec.configure(mDecoderMediaFormat, surface, null, 0);     //配置窗口和款高比
            mMediaCodec.start();
            mRunning.set(true);
            ACUtils.ealog(TAG, mPrefix, "startDecoder  config: %s", config);
            //L.alog().d(TAG, "startDecoder  config: %s", config);
        }
    }

    @Override
    public void stop() {
        if (mMediaCodec != null && isRunning()) {
            mRunning.set(false);
            mMediaCodec.stop();
            ACUtils.ealog(TAG, mPrefix, "stop");
            //L.alog().d(TAG, "stop");
        }
    }

    @Override
    public void release() {
        if (mMediaCodec != null) {
            mRunning.set(false);
            isRelease.set(true);
            mMediaCodec.release();
            ACUtils.ealog(TAG, mPrefix, "release");
            //L.alog().d(TAG, "release");
        }
        mMediaCodec = null;
    }

    @Override
    public int getEncoderColorFormat() {
        return HEVCCodecManager.get().getEncoderColorFormat();
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
    public final int dequeueOutputBuffer(@NonNull MediaCodec.BufferInfo info, long timeoutUs) {
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
    public final void queueInputBuffer(int index, int offset, int size, long presentationTimeUs, int flags)
            throws MediaCodec.CryptoException {
        if (!isRunning()) {
            return;
        }
        getMediaCodec().queueInputBuffer(index, offset, size, presentationTimeUs, flags);
    }

    @Nullable
    @Override
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
