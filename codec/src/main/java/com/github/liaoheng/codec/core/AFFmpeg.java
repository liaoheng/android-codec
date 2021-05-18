package com.github.liaoheng.codec.core;

/**
 * @author liaoheng
 * @version 2018-06-29 10:00
 */
public class AFFmpeg {

    static {
        System.loadLibrary("affmpeg");
    }

    private static boolean isInit;

    public AFFmpeg() {
        if (!isInit) {
            isInit = true;
            init();
        }
    }

    public native boolean init();

    public native boolean initRecord(String fileName, int width, int height, int bitRate, int gop, int frameRate,
            int videoCodecId);

    public native void releaseRecord();

    public synchronized native void writeAudioData(byte[] data, int length);

    public synchronized native void writeVideoData(byte[] data, int length);

    public native boolean screenshot(String fileName, int width, int height, byte[] data, int length, int videoCodecId);

    public native byte[] imageToYUV(byte[] data, int width, int height);

    public native boolean imageToYUVInit();

    public native void imageToYUVRelease();
}


