package com.github.liaoheng.codec.demo

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.github.liaoheng.common.core.CountDownHelper
import com.github.liaoheng.common.core.CountDownHelper.CountDownListener
import com.github.liaoheng.common.util.AppUtils
import com.github.liaoheng.common.util.UIUtils

/**
 * @author liaoheng
 * @version 2020-07-21 16:03
 */
class LaunchActivity : AppCompatActivity() {
    private lateinit var mCountDownHelper: CountDownHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launch)
        val decorView = window.decorView
        val uiOptions = (View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or View.SYSTEM_UI_FLAG_FULLSCREEN)
        decorView.systemUiVisibility = uiOptions
        val version: TextView = findViewById(R.id.launch_version)
        version.text = AppUtils.getVersionInfo(this)?.versionName
        mCountDownHelper = CountDownHelper(3, 1,
            object : CountDownListener {
                override fun start() {}
                override fun pause(time: Long) {}
                override fun onTick(time: Long) {}
                override fun stop() {}
                override fun finish() { //倒计时完成
                    enter()
                }
            })

        mCountDownHelper.start()
    }

    override fun onDestroy() {
        mCountDownHelper.stop()
        super.onDestroy()
    }

    fun enter() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.CAMERA),
                11
            )
        }else{
            UIUtils.startActivity(this, MainActivity::class.java)
            finish()
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 11) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                UIUtils.startActivity(this, MainActivity::class.java)
                finish()
            } else {
                UIUtils.showToast(this, "需要权限")
            }
        }
    }

}