package com.github.liaoheng.codec.util;

import java.math.BigDecimal;

/**
 * @author liaoheng
 * @version 2018-05-21 10:10
 */
public class ACUtils {

    public static int getLowBitRate(int width, int height) {
        return width * height * 3 / 2;
    }

    public static int getMidBitRate(int width, int height) {
        return width * height * 3;
    }

    public static int getHeightBitRate(int width, int height) {
        return width * height * 3 * 2;
    }

    public static double toMbps(double bitRate) {
        return new BigDecimal(bitRate / 1024 / 1024).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    public static double toKbps(double bitRate) {
        return new BigDecimal(bitRate / 1024).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    public static void ealog(String tag, String prefix, String t, Object... obj) {
        CLog.alog().d(tag, "|%s| > %s", prefix, String.format(t, obj));
    }

    public static void elog(String tag, String prefix, String t, Object... obj) {
        CLog.log().d(tag, "|%s| > %s", prefix, String.format(t, obj));
    }
}
