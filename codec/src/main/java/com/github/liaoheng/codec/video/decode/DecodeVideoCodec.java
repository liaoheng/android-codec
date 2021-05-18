package com.github.liaoheng.codec.video.decode;

import android.media.Image;
import android.media.MediaCodec;
import android.os.SystemClock;
import android.view.Surface;

import com.github.liaoheng.codec.core.YUVToJPEG;
import com.github.liaoheng.codec.model.VideoCodecConfig;
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
 * 远端视频工具类，主要是解码，还带上传数据
 *
 * @author wangj
 * @author liaoheng
 * @version 2018-05-22
 */
public class DecodeVideoCodec extends BaseVideoCodec implements IDecodeVideoCodec {
    private Surface mSurface; // 渲染器

    private long mFrameRate = 0;     // 帧率

    private long mTimestamp = 0;     // 时间戳

    private AtomicBoolean isCapture = new AtomicBoolean(false);//截屏状态

    private YUVToJPEG mYuvToJPEG= new YUVToJPEG();
    private byte[] mLstIFrame;//最新的关键帧

    private WorkProcessThread mVideoPlayThread = new WorkProcessThread(new WorkProcessThread.Handler() {
        @Override
        public String name() {
            return getUser() + " : RemoteVideoPlayThread";
        }

        @Override
        public void onStart(String name) {
            ACUtils.ealog(TAG,name,"start");
        }

        @Override
        public void onHandler() {
            videoPlayHandler();
        }

        @Override
        public void onStop(String name) {
            ACUtils.ealog(TAG,name,"stop");
        }
    });

    @Override
    protected IWorkProcessThread getWorkThread() {
        return mVideoPlayThread;
    }

    private void videoPlayHandler() {
        try {
            SystemClock.sleep(2);

            byte[] data = takeQueue();
            if (data == null) {
                SystemClock.sleep(5);
                return;
            }

            rendering(data);
        } catch (Exception e) {
            CLog.alog().e(TAG, e);
            SystemClock.sleep(50);
        }
    }

    public DecodeVideoCodec(String user) {
        super(user);
    }

    @Override
    public void setSurface(Surface surface) {
        mSurface = surface;
    }

    @Override
    public void init(Object obj) {
        super.init(obj);
        setSurface((Surface) obj);
    }

    @Override
    protected void initCodec(VideoCodecConfig header) throws IOException {
        switchMediaCodecByConfig(header);
        initDecoderMediaCodec();
    }

    @Override
    protected boolean startAction(VideoCodecConfig header, boolean isRestart) throws IOException {
        if (header.getWidth() == 0 || header.getHeight() == 0 || header.getFrameRate() == 0) {
            ACUtils.ealog(TAG,getUser(),"收到无效的视频头数据 : %s",header);
            if (mVideoCodecCallBack != null) {
                mVideoCodecCallBack.clear();
            }
            return false;
        }

        if (isVideoHeader() && !isRestart) {
            boolean flag = false;
            if (getVideoHeader().getVideoCodecIDInt() != header.getVideoCodecIDInt()) {
                flag = true;
            }
            if (!flag && getVideoHeader().getWidth() != header.getWidth()) {
                flag = true;
            }
            if (!flag && getVideoHeader().getHeight() != header.getHeight()) {
                flag = true;
            }
            if (!flag && getVideoHeader().getFrameRate() != header.getFrameRate()) {
                flag = true;
            }
            if (!flag && getVideoHeader().getBitRate() != header.getBitRate()) {
                flag = true;
            }
            if (!flag) {
                ACUtils.ealog(TAG,getUser(),"过滤收到的视频头数据 : %s",header);
                return false;
            }
        }

        stopMediaCodec();
        clearQueue();
        initCodec(header);

        setVideoHeader(header);
        mFrameRate = header.getFrameRate();
        ishead = false;
        isCapture.set(false);

        ACUtils.elog(TAG,getUser(),"start , config : %s ",header);
        startDecoderMediaCodec(mSurface, header);
        getWorkThread().start();

        startCallBack(header);
        return true;
    }

    @Override
    public void resume() {
        super.resume();
        ACUtils.ealog(TAG,getUser(),"resume");
    }

    @Override
    public boolean restart() {
        boolean restart = super.restart();
        ACUtils.ealog(TAG,getUser(),"restart : %s",restart);
        return restart;
    }

    @Override
    public void pause() {
        super.pause();
        ACUtils.ealog(TAG,getUser(),"pause");
    }

    @Override
    public void stop() {
        ACUtils.ealog(TAG,getUser(),"stop");
        super.stop();
        setVideoHeader(null);
        mLstIFrame = null;
        mTimestamp = 0;
        stopCallBack();
    }

    @Override
    public void release() {
        ACUtils.ealog(TAG,getUser(),"release");
        super.release();
        mTimestamp = 0;
        isCapture.set(false);
        if (mSurface != null) {
            mSurface.release();
        }
    }

    @Override
    public synchronized boolean isRunning() {
        return isRunningMediaCodec() && mVideoPlayThread.isRunning();
    }

    @Override
    public boolean receivedHeader() {
        return isVideoHeader();
    }

    @Override
    public void screenshot(File outFile, Callback<String> callback) {
        mYuvToJPEG.setCallback(callback);
        mYuvToJPEG.setParameter(outFile, getVideoHeader().getVideoCodecID());
        isCapture.set(true);
    }

    @Override
    public synchronized void receivedVideoHeader(VideoCodecConfig config) {
        if (mSurface == null || !mSurface.isValid()) {
            return;
        }
        start(config);
    }

    private boolean ishead;

    @Override
    public void receivedVideoData(byte[] videoBytes) {
        if (null == videoBytes || videoBytes.length == 0) {//过滤空数据
            return;
        }
        putQueue(videoBytes);
    }

    private void rendering(byte[] videoBytes) {

        if (getMediaCodec().isIFrame(videoBytes)) {
            mLstIFrame = videoBytes;
        }


        if (mMediaMuxer != null && mMediaMuxer.isRunning()) {
            if (getMediaCodec().isIFrame(videoBytes)) {
                mMediaMuxer.writeVideoData(videoBytes);
                ishead = true;
            } else {
                if (ishead) {
                    mMediaMuxer.writeVideoData(videoBytes);
                }
            }
        }

        if (isPause()) {//暂停的情况之下，不渲染
            SystemClock.sleep(50);
            return;
        }

        if (!isRunning() || !isVideoHeader()) {
            return;
        }

        int inputBufferId = getMediaCodec().dequeueInputBuffer(300);
        if (inputBufferId < 0) {
            return;
        }

        ByteBuffer inputBuffer = getMediaCodec().getInputBuffer(inputBufferId);
        if (inputBuffer == null) {
            return;
        }
        inputBuffer.clear();
        inputBuffer.put(videoBytes, 0, videoBytes.length);   // offset先始终设置为0
        long linearTime = mTimestamp * mFrameRate;    // 时间戳只要是一个线性递增的就行
        getMediaCodec().queueInputBuffer(inputBufferId, 0, videoBytes.length, linearTime, 0);
        mTimestamp++;

        //处理输出缓冲，解码渲染，渲染完后就释放
        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();   //用来装buf信息
        int outputBufferId = getMediaCodec().dequeueOutputBuffer(bufferInfo, 1000);   //获取一个输出缓冲id
        if (outputBufferId >= 0) {
            if (isCapture.get()) {
                Image outputImage = getMediaCodec().getMediaCodec().getOutputImage(outputBufferId);
                if (outputImage != null) {
                    isCapture.set(false);
                    mYuvToJPEG.compressToJpeg(outputImage);
                } else {
                    isCapture.set(false);
                    mYuvToJPEG.ffmpegToJpeg(getVideoHeader().getWidth(), getVideoHeader().getHeight(), mLstIFrame);
                }
            }
            getMediaCodec().releaseOutputBuffer(outputBufferId, true);   //渲染
        }
    }

}
