package com.github.liaoheng.codec.demo

import android.os.Bundle
import android.os.SystemClock
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.github.liaoheng.codec.model.IDataServerSend
import com.github.liaoheng.codec.model.VideoFrame
import com.github.liaoheng.codec.view.CameraEncodeProxyVideoView
import com.github.liaoheng.common.util.Callback
import com.github.liaoheng.common.util.FileUtils
import com.github.liaoheng.common.util.L
import com.github.liaoheng.common.util.UIUtils
import java.io.File

/**
 * @author liaoheng
 * @date 2021-05-17 10:31:57
 */
class MainActivity : AppCompatActivity() {

    lateinit var videoView: CameraEncodeProxyVideoView;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        videoView = CameraEncodeProxyVideoView(this)
        videoView.defVideoCodec(object : IDataServerSend {
            override fun sendVideoData(videoFrame: VideoFrame?) {
                L.alog().d("MainActivity", "videoFrame : %s", videoFrame?.data?.size)
            }

            override fun sendAudioData(audioBytes: ByteArray?) {
            }
        })

        findViewById<ViewGroup>(R.id.video_layout).addView(
            videoView, ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.screenshot) {
            videoView.videoCodec.screenshot(
                File(
                    FileUtils.createProjectSpaceDir(this, "media"),
                    "test_" + System.currentTimeMillis() + ".jpg"
                ), object : Callback.EmptyCallback<String>() {
                    override fun onSuccess(t: String?) {
                        UIUtils.showToast(applicationContext, "" + t)
                    }

                    override fun onError(e: Throwable?) {
                        UIUtils.showToast(applicationContext, "" + e?.message)
                    }
                }
            )
        }
        return super.onOptionsItemSelected(item)
    }
}