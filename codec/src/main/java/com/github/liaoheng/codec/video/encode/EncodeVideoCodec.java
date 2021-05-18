package com.github.liaoheng.codec.video.encode;

import android.media.Image;
import android.media.MediaCodec;
import android.os.SystemClock;

import com.github.liaoheng.codec.core.YUVToJPEG;
import com.github.liaoheng.codec.model.IDataServerSend;
import com.github.liaoheng.codec.model.VideoCodecConfig;
import com.github.liaoheng.codec.model.VideoFrame;
import com.github.liaoheng.codec.util.ACUtils;
import com.github.liaoheng.codec.util.CLog;
import com.github.liaoheng.codec.video.BaseVideoCodec;
import com.github.liaoheng.common.thread.IWorkProcessThread;
import com.github.liaoheng.common.thread.WorkProcessThread;
import com.github.liaoheng.common.util.Callback;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 本地视频工具类，主要是编码，还带上传数据
 *
 * @author wangj
 * @author liaoheng
 * @version 2018-05-22
 */
public class EncodeVideoCodec extends BaseVideoCodec implements IEncodeVideoCodec {

    // mSpsPps用来存储sps pps数据，后面遇到关键帧（I帧），必须将spspps数据加到I帧前面
    private byte[] mSpsPps;
    private VideoFrame mFrameData = new VideoFrame();
    private IDataServerSend mSend;

    private WorkProcessThread mVideoPlayThread = new WorkProcessThread(
            new IWorkProcessThread.BaseHandler(CLog.alog(), "LocalVideoPlayThread") {

                @Override
                public void onHandler() {
                    videoPlayHandler();
                }
            });

    @Override
    protected IWorkProcessThread getWorkThread() {
        return mVideoPlayThread;
    }

    public EncodeVideoCodec(String user) {
        super(user);
    }

    @Override
    public void init(Object obj) {
        super.init(obj);
    }

    @Override
    protected void initCodec(VideoCodecConfig config) throws IOException {
        switchMediaCodecByConfig(config);
        initEncoderMediaCodec();
    }

    @Override
    protected boolean startAction(VideoCodecConfig config, boolean isRestart) throws IOException {
        stopMediaCodec();
        clearQueue();
        initCodec(config);
        setVideoHeader(config);

        ACUtils.elog(TAG, getUser(), "start , config : %s ", config);
        startEncoderMediaCodec(config);
        mFrameData.setMediaType(config.getVideoCodecIDInt());
        getWorkThread().start();

        startCallBack(config);
        return true;
    }

    @Override
    public void resume() {
        super.resume();
        ACUtils.ealog(TAG, getUser(), "resume");
    }

    @Override
    public boolean restart() {
        boolean restart = super.restart();
        ACUtils.ealog(TAG, getUser(), "restart : %s", restart);
        return restart;
    }

    @Override
    public void pause() {
        super.pause();
        ACUtils.ealog(TAG, getUser(), "pause");
    }

    @Override
    public void stop() {
        ACUtils.ealog(TAG, getUser(), "stop");
        super.stop();
        stopCallBack();
    }

    @Override
    public void release() {
        ACUtils.ealog(TAG, getUser(), "release");
        super.release();
    }

    @Override
    public synchronized boolean isRunning() {
        return mVideoPlayThread.isRunning() && isRunningMediaCodec();
    }

    private YUVToJPEG mYuvToJPEG = new YUVToJPEG();
    private AtomicBoolean isCapture = new AtomicBoolean(false);//截屏状态
    private byte[] mLstIFrame;//最新的关键帧

    @Override
    public void screenshot(File outFile, Callback<String> callback) {
        mYuvToJPEG.setCallback(callback);
        mYuvToJPEG.setParameter(outFile, getVideoHeader().getVideoCodecID());
        isCapture.set(true);
    }

    /**
     * 循环编码视频数据
     */
    private void videoPlayHandler() {
        try {

            SystemClock.sleep(2);

            byte[] data = takeQueue();
            if (data == null) {
                SystemClock.sleep(5);
                return;
            }

            if (isPause()) {//暂停的情况之下，不上传数据
                SystemClock.sleep(50);
                return;
            }

            ByteBuffer[] inputBuffers = getMediaCodec().getMediaCodec().getInputBuffers(); // 检索输入缓冲区的集合
            ByteBuffer[] outputBuffers = getMediaCodec().getMediaCodec().getOutputBuffers(); // 检索输出缓冲区集
            int bufferIndex = getMediaCodec().dequeueInputBuffer(300); // 如果没有当前可用的缓冲区，则返回输入缓冲区的索引，以填充有效数据或-1。
            if (bufferIndex >= 0) { // 当前缓冲区有数据
                inputBuffers[bufferIndex].clear();
                inputBuffers[bufferIndex].put(data);

                getMediaCodec()
                        .queueInputBuffer(bufferIndex, 0, inputBuffers[bufferIndex].position(),
                                System.nanoTime() / 1000, 0);
                MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
                int outputBufferIndex;
                for (; ; ) {
                    outputBufferIndex = getMediaCodec().dequeueOutputBuffer(info, 0); // 输出缓冲区
                    if (outputBufferIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
                        break;
                    } else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                        outputBuffers = getMediaCodec().getMediaCodec().getOutputBuffers();
                    } else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                        //ByteBuffer byteBuffer = mMediaCodec.getMediaCodec()
                        //        .getOutputFormat()
                        //        .getByteBuffer("csd-0");
                        //mSpsPps = new byte[info.size];
                        //byteBuffer.get(mSpsPps);
                    } else if (outputBufferIndex < 0) {
                        break;
                    } else {
                        if (info.size <= 0) {
                            break;
                        }
                        ByteBuffer outputBuffer = outputBuffers[outputBufferIndex]; // 检索出缓冲区数据
                        outputBuffer.position(info.offset);
                        outputBuffer.limit(info.offset + info.size);
                        byte[] outData = new byte[info.size];
                        outputBuffer.get(outData);

                        if (getMediaCodec().isNalHeaderFrame(outData)) {
                            mSpsPps = outData;
                            //L.alog().d(TAG, "csd-0  : %s ", Arrays.toString(mSpsPps));
                        } else { // 将pps,sps,vps添加到每帧数据前
                            byte[] iFrameData = new byte[mSpsPps.length + outData.length];
                            System.arraycopy(mSpsPps, 0, iFrameData, 0, mSpsPps.length); // copy pps和sps数据
                            System.arraycopy(outData, 0, iFrameData, mSpsPps.length,
                                    outData.length);

                            if (getMediaCodec().isIFrame(outData)) {
                                mLstIFrame = iFrameData;
                            }

                            if (isCapture.get()) {
                                Image outputImage = getMediaCodec().getMediaCodec().getOutputImage(outputBufferIndex);
                                if (outputImage != null) {
                                    isCapture.set(false);
                                    mYuvToJPEG.compressToJpeg(outputImage);
                                } else {
                                    isCapture.set(false);
                                    mYuvToJPEG.ffmpegToJpeg(getVideoHeader().getWidth(), getVideoHeader().getHeight(), mLstIFrame);
                                }
                            }

                            outData = iFrameData;
                            mFrameData.setFrameType(1);
                        }
                        mFrameData.setData(outData);

                        if (mSend != null) {
                            mSend.sendVideoData(mFrameData);
                        }
                        getMediaCodec().releaseOutputBuffer(outputBufferIndex, false);
                    }
                }
            }
        } catch (Exception e) {
            CLog.alog().e(TAG, e);
            SystemClock.sleep(50);
        }
    }

    @Override
    public void onPreviewFrame(byte[] data) {
        putQueue(data);
    }

    @Override
    public void setDataServerSend(IDataServerSend send) {
        mSend = send;
    }
}
