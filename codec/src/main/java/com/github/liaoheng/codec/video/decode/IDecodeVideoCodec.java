package com.github.liaoheng.codec.video.decode;

import android.view.Surface;

import com.github.liaoheng.codec.model.VideoCodecConfig;
import com.github.liaoheng.codec.video.IVideoCodec;

/**
 * 远端视频工具类接口
 *
 * @author liaoheng
 * @version 2018-05-17 16:05
 */
public interface IDecodeVideoCodec extends IVideoCodec {

    /**
     * 设置需要渲染的Surface
     * <p>SurfaceTexture & SurfaceHolder</p>
     *
     * @param surface {@link Surface}
     */
    void setSurface(Surface surface);

    /**
     * 收到视频头并配置解码器信息，并调用{@link IVideoCodec#start(VideoCodecConfig)}
     *
     * @param header 视频头数据
     */
    void receivedVideoHeader(VideoCodecConfig header);

    /**
     * 收到视频数据并解码渲染
     *
     * @param videoBytes 视频数据
     */
    void receivedVideoData(byte[] videoBytes);

    /**
     * @return 是否已经接收到视频头
     */
    boolean receivedHeader();

}
