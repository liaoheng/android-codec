package com.github.liaoheng.codec.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;

import com.github.liaoheng.codec.model.CameraCodecConfig;
import com.github.liaoheng.codec.model.IDataServerSend;
import com.github.liaoheng.codec.model.VideoFrame;
import com.github.liaoheng.codec.video.encode.EncodeVideoCodec;

/**
 * @author liaoheng
 * @version 2021-05-17 16:26:13
 */
public class CameraEncodeProxyVideoView extends BaseProxyVideoView implements ICameraView {
    public CameraEncodeProxyVideoView(@NonNull Context context) {
        super(context);
    }

    public CameraEncodeProxyVideoView(@NonNull Context context, @NonNull AttributeSet attrs) {
        super(context, attrs);
    }

    public CameraEncodeProxyVideoView(@NonNull Context context, @NonNull AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public CameraEncodeProxyVideoView(@NonNull Context context, @NonNull AttributeSet attrs, int defStyleAttr,
            int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected BaseVideoView initRealView(Context context) {
        return new CameraEncodeVideoView(context);
    }

    @Override
    public void setCameraCallBack(CameraCallback callBack) {
        ((ICameraView) mVideoView).setCameraCallBack(callBack);
    }

    @Override
    public void toggle() {
        ((ICameraView) mVideoView).toggle();
    }

    @Override
    public View getView() {
        return this;
    }

    @Override
    public CameraCodecConfig getConfig() {
        return super.getConfig() == null ? null : (CameraCodecConfig) super.getConfig();
    }

    public void defVideoCodec(IDataServerSend send) {
        EncodeVideoCodec encode = new EncodeVideoCodec("CameraEncode");
        encode.setVideoHeader(new CameraCodecConfig());
        encode.setDataServerSend(send);
        initVideoCodec(encode);
    }
}
