package com.github.liaoheng.codec.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.AttributeSet;

/**
 * @author liaoheng
 * @version 2021-05-17 16:26:13
 */
public class EncodeProxyVideoView extends BaseProxyVideoView {
    public EncodeProxyVideoView(@NonNull Context context) {
        super(context);
    }

    public EncodeProxyVideoView(@NonNull Context context, @NonNull AttributeSet attrs) {
        super(context, attrs);
    }

    public EncodeProxyVideoView(@NonNull Context context, @NonNull AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public EncodeProxyVideoView(@NonNull Context context, @NonNull AttributeSet attrs, int defStyleAttr,
            int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected BaseVideoView initRealView(Context context) {
        return new EncodeVideoView(context);
    }
}
