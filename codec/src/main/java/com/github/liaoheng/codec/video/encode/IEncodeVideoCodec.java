package com.github.liaoheng.codec.video.encode;

import com.github.liaoheng.codec.model.IDataServerSend;
import com.github.liaoheng.codec.video.IVideoCodec;

/**
 * 本地视频工具类接口
 *
 * @author liaoheng
 * @version 2018-05-17 16:05
 */
public interface IEncodeVideoCodec extends IVideoCodec {
    /**
     * 传入标准I420格式的一帧数据
     */
    void onPreviewFrame(byte[] data);

    /**
     * 发送数据回调
     */
    void setDataServerSend(IDataServerSend send);
}
