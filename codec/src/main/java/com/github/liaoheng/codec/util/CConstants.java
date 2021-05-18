package com.github.liaoheng.codec.util;

import com.github.liaoheng.codec.model.VideoResolution;

/**
 * @author liaoheng
 * @version 2018-08-23 15:07
 */
public final class CConstants {
    public final static VideoResolution DEF_CAMERA_PREVIEW_RESOLUTION = VideoResolution.VGA;
    public final static int DEF_CAMERA_PREVIEW_WIDTH = DEF_CAMERA_PREVIEW_RESOLUTION.getWidth();
    public final static int DEF_CAMERA_PREVIEW_HEIGHT = DEF_CAMERA_PREVIEW_RESOLUTION.getHeight();
    public static final int DEF_FRAME_RATE = 15;
    public static final int DEF_BIT_RATE = ACUtils.getLowBitRate(DEF_CAMERA_PREVIEW_WIDTH, DEF_CAMERA_PREVIEW_HEIGHT);
    public static final int DEF_I_FRAME_INTERVAL = 30;
}
