package com.github.liaoheng.codec.util;

import com.github.liaoheng.common.util.Logcat;

/**
 * @author liaoheng
 * @version 2019-04-11 17:16
 */
public class CLog {

    private static Logcat mCodeLogger;

    public static Logcat code() {
        if (null == mCodeLogger) {
            mCodeLogger = Logcat.create();
            mCodeLogger.log().prefix("<Code>");
            mCodeLogger.logger().prefix("<Code>");
        }
        return mCodeLogger;
    }

    public static Logcat.ILogcat alog() {
        return code().log();
    }

    public static Logcat.ILogcat log() {
        return code().logger();
    }
}
