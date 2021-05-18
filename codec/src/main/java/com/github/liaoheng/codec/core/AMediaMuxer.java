package com.github.liaoheng.codec.core;

import android.os.SystemClock;
import android.text.TextUtils;

import com.github.liaoheng.codec.model.VideoCodecConfig;
import com.github.liaoheng.codec.util.CLog;
import com.github.liaoheng.common.thread.WorkProcessQueueHelper;
import com.github.liaoheng.common.thread.WorkProcessThread;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 混编音频与视频
 *
 * @author liaoheng
 * @version 2018-06-22 15:11
 */
@Deprecated
public class AMediaMuxer implements IMediaMuxer {
    private final String TAG = AMediaMuxer.class.getSimpleName();
    private AFFmpeg mFFmpeg;
    private String namePath;
    private AtomicBoolean isInit = new AtomicBoolean(false);
    private WorkProcessThread mEncodeThread = new WorkProcessThread(
            new WorkProcessThread.BaseHandler(CLog.alog(), TAG) {
                @Override
                public String name() {
                    return "MediaMuxerWriteThread";
                }

                @Override
                public void onHandler() {
                    handler();
                }

                @Override
                public void onStop(String name) {
                    super.onStop(name);
                    if (mFFmpeg != null) {
                        mFFmpeg.releaseRecord();
                    }
                }
            });

    private WorkProcessQueueHelper<Data> mProcessQueueHelper = new WorkProcessQueueHelper<>(mEncodeThread);

    class Data {
        Data(byte[] data, int type, int length) {
            this.data = data;
            this.type = type;
            this.length = length;
        }

        byte[] data;
        int type;
        int length;
    }

    public void init() {
        isInit.set(true);
        mFFmpeg = new AFFmpeg();
    }

    public String getFilePath() {
        return namePath;
    }

    public synchronized boolean isRunning() {
        return mEncodeThread.isRunning();
    }

    private void handler() {
        Data data = mProcessQueueHelper.takeQueue();
        if (data == null) {
            SystemClock.sleep(2);
            return;
        }
        if (data.type == 1) {
            mFFmpeg.writeVideoData(data.data, data.length);
        } else {
            mFFmpeg.writeAudioData(data.data, data.length);
        }
    }

    public boolean start(VideoCodecConfig config, String outFile) {
        if (TextUtils.isEmpty(namePath)) {
            return false;
        }
        if (!isInit.get()) {
            init();
        }
        namePath = outFile;
        boolean init = mFFmpeg.initRecord(namePath, config.getWidth(), config.getHeight(), config.getBitRate(),
                config.getFrameInterval(), config.getFrameRate(), config.getVideoCodecIDInt());
        if (init) {
            mEncodeThread.start();
            CLog.alog().d(TAG, "start");
        }
        return init;
    }

    public void writeVideoData(byte[] data) {
        mProcessQueueHelper.putQueue(new Data(data, 1, data.length));
    }

    public void writeAudioData(byte[] data) {
        mProcessQueueHelper.putQueue(new Data(data, 0, data.length));
    }

    public void release() {
        if (!isRunning()) {
            return;
        }
        mProcessQueueHelper.clearQueue();
        mEncodeThread.stop();
        CLog.alog().d(TAG, "release");
    }
}
