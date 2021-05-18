package com.github.liaoheng.codec.audio;

import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.os.SystemClock;

import com.github.liaoheng.codec.core.IMediaMuxer;
import com.github.liaoheng.codec.model.IDataServerSend;
import com.github.liaoheng.codec.util.CLog;
import com.github.liaoheng.common.thread.WorkProcessQueueHelper;
import com.github.liaoheng.common.thread.WorkProcessThread;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 音频工具类
 *
 * @author liaoheng
 * @version 2018-05-21 10:16
 */
@Deprecated
public class AudioCodec implements IAudioCodec {
    private final String TAG = AudioCodec.class.getSimpleName();
    private AtomicBoolean isPause = new AtomicBoolean(false);//暂停状态
    private AudioRecordCodec mAudioRecordCodec;
    private AudioTrack mAudioTrack;
    private IMediaMuxer mMediaMuxer;
    private IDataServerSend mSend;
    private WorkProcessThread mAudioSendThread = new WorkProcessThread(
            new WorkProcessThread.BaseHandler(CLog.alog(), TAG) {
                @Override
                public String name() {
                    return "AudioSendThread";
                }

                @Override
                public void onHandler() {
                    sendAudioThread();
                }
            });
    private WorkProcessThread mAudioPlayThread = new WorkProcessThread(
            new WorkProcessThread.BaseHandler(CLog.alog(), TAG) {
                @Override
                public String name() {
                    return "AudioPlayThread";
                }

                @Override
                public void onHandler() {
                    audioPlayHandler();
                }
            });
    private WorkProcessQueueHelper<byte[]> mPlayWorkProcessQueueHelper = new WorkProcessQueueHelper<>(
            mAudioPlayThread);

    private void audioPlayHandler() {
        try {
            byte[] data = nextAudioData();
            if (data != null) {
                rendering(data);
            }
        } catch (Exception e) {
            CLog.alog().e(TAG, e);
            SystemClock.sleep(50);
        }
    }

    private byte[] nextAudioData() {
        return mPlayWorkProcessQueueHelper.takeQueue();
    }

    public void putAudioData(byte[] packet) {
        mPlayWorkProcessQueueHelper.putQueue(packet);
    }

    @Override
    public void init() {
        mAudioRecordCodec = new AudioRecordCodec();
        mAudioRecordCodec.creatAudioRecord();
        int bufferSizeInBytes = AudioRecord.getMinBufferSize(44100,
                AudioFormat.CHANNEL_IN_STEREO, AudioFormat.ENCODING_PCM_16BIT);
        mAudioTrack = new AudioTrack(
                new AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_SPEECH).build(),
                new AudioFormat.Builder().setEncoding(AudioFormat.ENCODING_PCM_16BIT).setSampleRate(44100).build(),
                bufferSizeInBytes, AudioTrack.MODE_STREAM, 123);
    }

    private volatile boolean mMicrophoneOn = true; // 麦克风打开标帜

    private volatile boolean mLoudspeakerOn = true; // 扬声器打开标帜

    @Override
    public byte[] getAudioData() {
        return mAudioRecordCodec.getAudiodata();
    }

    @Override
    public void receivedAudioData(byte[] bytes) {
        if (null == bytes || bytes.length == 0) {//过滤空数据
            return;
        }
        putAudioData(bytes);
    }

    private void rendering(byte[] data) {
        if (!isPause.get() && isLoudspeakerSwitch()) {
            mAudioTrack.write(data, 0, data.length);
        }
        //不跟随音频工具类的生命周期
        if (mMediaMuxer != null && mMediaMuxer.isRunning()) {
            mMediaMuxer.writeAudioData(data);
        }
    }

    @Override
    public void setDataServerSend(IDataServerSend send) {
        mSend = send;
    }

    @Override
    public void start() {
        CLog.alog().d(TAG, "start");
        mAudioRecordCodec.start();
        mAudioTrack.play();
        setMicrophoneSwitch(true);//TODO 保存麦克风与扬声器状态
        setLoudspeakerSwitch(true);
        startThread();
    }

    @Override
    public void restart() {
        isPause.set(false);
        CLog.alog().d(TAG, "restart");
    }

    @Override
    public void resume() {
        isPause.set(false);
        CLog.alog().d(TAG, "resume");
    }

    @Override
    public void pause() {
        isPause.set(true);
        CLog.alog().d(TAG, "pause");
    }

    public void initMediaMuxer(IMediaMuxer mediaMuxer) {
        mMediaMuxer = mediaMuxer;
    }

    private void sendAudioThread() {
        if (isPause.get()) {
            SystemClock.sleep(50);
            return;
        }
        byte[] audioData = getAudioData();
        if (null != audioData && audioData.length > 0 && isMicrophoneSwitch()) {
            if (mSend != null) {
                mSend.sendAudioData(audioData);
            }
        } else {
            SystemClock.sleep(3);
        }
    }

    private void startThread() {
        CLog.alog().d(TAG, "startThread");
        mAudioPlayThread.start();
        mAudioSendThread.start();
    }

    private void stopThread() {
        mAudioPlayThread.stop();
        mAudioSendThread.stop();
        CLog.alog().d(TAG, "stopThread");
    }

    @Override
    public void setMicrophoneSwitch(boolean onOff) {
        CLog.alog().d(TAG, "setMicrophoneSwitch %s", onOff);
        mMicrophoneOn = onOff;
    }

    @Override
    public void setLoudspeakerSwitch(boolean onOff) {
        CLog.alog().d(TAG, "setLoudspeakerSwitch %s", onOff);
        mLoudspeakerOn = onOff;
    }

    @Override
    public boolean isMicrophoneSwitch() {
        return mMicrophoneOn;
    }

    @Override
    public boolean isLoudspeakerSwitch() {
        return mLoudspeakerOn;
    }

    @Override
    public void stop() {
        stopThread();
        stopAction();
    }

    private void stopAction() {
        mAudioRecordCodec.stop();
        mAudioTrack.stop();

        mPlayWorkProcessQueueHelper.clearQueue();
        CLog.alog().d(TAG, "stop");
    }

    @Override
    public void release() {
        stopThread();
        releaseAction();
    }

    private void releaseAction() {
        mAudioRecordCodec.release();
        mAudioTrack.release();

        mPlayWorkProcessQueueHelper.clearQueue();
        CLog.alog().d(TAG, "release");
    }

    @Override
    public boolean isRunning() {
        return mAudioPlayThread.isRunning();
    }
}
