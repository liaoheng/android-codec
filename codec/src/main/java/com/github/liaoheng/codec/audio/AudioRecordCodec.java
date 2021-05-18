package com.github.liaoheng.codec.audio;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

/**
 * @author liaoheng
 * @version 2018-06-27 16:39
 */
@Deprecated
public class AudioRecordCodec {
    private AudioRecord audioRecord;
    private boolean isRecord = false;// 设置正在录制的状态

    // 缓冲区字节大小
    private int bufferSizeInBytes = 0;

    public void creatAudioRecord() {

        // 获得缓冲区字节大小
        bufferSizeInBytes = AudioRecord.getMinBufferSize(44100,
                AudioFormat.CHANNEL_IN_STEREO, AudioFormat.ENCODING_PCM_16BIT);

        // 创建AudioRecord对象
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, 44100,
                AudioFormat.CHANNEL_IN_STEREO, AudioFormat.ENCODING_PCM_16BIT, bufferSizeInBytes);
    }

    public void start() {
        audioRecord.startRecording();
        isRecord = true;
    }

    public void stop() {
        audioRecord.stop();
        isRecord = false;
    }

    public void release(){
        audioRecord.release();
        isRecord=false;
    }

    public byte[] getAudiodata() {
        byte[] audiodata = new byte[bufferSizeInBytes];
        int readsize = 0;
        readsize = audioRecord.read(audiodata, 0, bufferSizeInBytes);
        if (AudioRecord.ERROR_INVALID_OPERATION == readsize) {
            return null;
        }
        return audiodata;
    }

}
