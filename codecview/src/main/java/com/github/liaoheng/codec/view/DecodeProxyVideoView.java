package com.github.liaoheng.codec.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.AttributeSet;

/**
 * @author liaoheng
 * @version 2021-05-17 16:12:34
 */
public class DecodeProxyVideoView extends BaseProxyVideoView {
    public DecodeProxyVideoView(@NonNull Context context) {
        super(context);
    }

    public DecodeProxyVideoView(@NonNull Context context, @NonNull AttributeSet attrs) {
        super(context, attrs);
    }

    public DecodeProxyVideoView(@NonNull Context context, @NonNull AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public DecodeProxyVideoView(@NonNull Context context, @NonNull AttributeSet attrs, int defStyleAttr,
            int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected BaseVideoView initRealView(Context context) {
        return new DecodeVideoView(context);
    }
}
