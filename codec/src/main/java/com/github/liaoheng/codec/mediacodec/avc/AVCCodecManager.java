package com.github.liaoheng.codec.mediacodec.avc;

import android.media.MediaFormat;

import com.github.liaoheng.codec.mediacodec.BaseCodecManager;

/**
 * @author liaoheng
 * @version 2018-11-28 13:02
 */
public class AVCCodecManager extends BaseCodecManager {

    private static AVCCodecManager mCodecManager;

    public static AVCCodecManager get() {
        if (mCodecManager == null) {
            mCodecManager = new AVCCodecManager(MediaFormat.MIMETYPE_VIDEO_AVC);
        }
        return mCodecManager;
    }

    private AVCCodecManager(String mimeType) {
        super(mimeType);
    }

}
