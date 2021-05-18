package com.github.liaoheng.codec.demo

import android.app.Application
import com.github.liaoheng.codec.Codec
import com.github.liaoheng.codec.view.CodecView
import com.github.liaoheng.common.Common


/**
 * @author liaoheng
 * @version 2020-04-20 21:19
 */
class MApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Common.baseInit(this, "codec", BuildConfig.DEBUG)
        Codec.init(this)
        CodecView.init(this, BuildConfig.DEBUG)
    }
}