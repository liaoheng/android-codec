package com.github.liaoheng.codec;

import android.content.Context;

import com.github.liaoheng.codec.mediacodec.avc.AVCCodecManager;
import com.github.liaoheng.codec.mediacodec.hevc.HEVCCodecManager;

/**
 * @author liaoheng
 * @version 2018-08-23 15:10
 */
public class Codec {

    public static void init(Context context) {
        HEVCCodecManager.get().init(context);
        AVCCodecManager.get().init(context);
    }
}
