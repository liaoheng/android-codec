package com.github.liaoheng.codec.view;

import android.content.Context;

/**
 * @author liaoheng
 * @date 2021-05-17 12:10
 */
public class CodecView {

    public static boolean sDebug;

    public static void init(Context context, boolean debug) {
        sDebug = debug;
    }
}
