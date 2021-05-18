package com.github.liaoheng.codec.mediacodec.hevc;

import android.media.MediaFormat;

import com.github.liaoheng.codec.mediacodec.BaseCodecManager;

/**
 * @author liaoheng
 * @version 2018-11-28 13:02
 */
public class HEVCCodecManager extends BaseCodecManager {

    private static HEVCCodecManager mCodecManager;

    public static HEVCCodecManager get() {
        if (mCodecManager == null) {
            mCodecManager = new HEVCCodecManager(MediaFormat.MIMETYPE_VIDEO_HEVC);
        }
        return mCodecManager;
    }

    private HEVCCodecManager(String mimeType) {
        super(mimeType);
    }

}
