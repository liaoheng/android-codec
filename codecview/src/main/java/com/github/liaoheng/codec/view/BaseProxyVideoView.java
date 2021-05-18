package com.github.liaoheng.codec.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PointF;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.Surface;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.github.liaoheng.codec.model.BitRateLevel;
import com.github.liaoheng.codec.model.CameraCodecConfig;
import com.github.liaoheng.codec.model.VideoCodecConfig;
import com.github.liaoheng.codec.video.IVideoCodec;
import com.github.liaoheng.codec.video.VideoCodecCallBack;
import com.github.liaoheng.common.util.DisplayUtils;
import com.github.liaoheng.common.util.HandlerUtils;
import com.github.liaoheng.common.util.UIUtils;

import java.util.Map;

/**
 * @author liaoheng
 * @version 2018-12-26 17:05
 */
public abstract class BaseProxyVideoView extends FrameLayout implements IVideoLifeView, IVideoCodecView {
    public BaseProxyVideoView(@NonNull Context context) {
        super(context);
        init(context);
    }

    public BaseProxyVideoView(@NonNull Context context,
            @NonNull AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public BaseProxyVideoView(@NonNull Context context,
            @NonNull AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public BaseProxyVideoView(@NonNull Context context,
            @NonNull AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    protected BaseVideoView mVideoView;
    private TextView mResolution;
    private TextView mBitRate;
    private TextView mCodec;

    private void init(Context context) {
        mVideoView = initRealView(context);
        LayoutParams video = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        addView(mVideoView, video);
        if (CodecView.sDebug && mVideoView != null) {
            mResolution = new TextView(context);
            addTextViewStyle(context, mResolution, 0);

            mBitRate = new TextView(context);
            addTextViewStyle(context, mBitRate, 8);

            mCodec = new TextView(context);
            addTextViewStyle(context, mCodec, 16);

            UIUtils.viewGone(mResolution, mBitRate, mCodec);
        }
    }

    private void addTextViewStyle(Context context, TextView text, int bottomMargin) {
        LayoutParams params = new LayoutParams(DisplayUtils.dp2px(context, 40),
                DisplayUtils.dp2px(context, 8));
        params.gravity = Gravity.BOTTOM | Gravity.START;
        params.bottomMargin = DisplayUtils.dp2px(context, bottomMargin);
        text.setBackgroundColor(ContextCompat.getColor(context, android.R.color.black));
        text.setTextColor(ContextCompat.getColor(context, android.R.color.white));
        text.setGravity(Gravity.CENTER);
        text.setTextSize(DisplayUtils.px2sp(context, 14));
        addView(text, params);
    }

    protected abstract BaseVideoView initRealView(Context context);

    @SuppressLint("SetTextI18n")
    private void setParameter(VideoCodecConfig config) {
        if (config == null) {
            return;
        }
        if (mResolution != null) {
            mResolution.setText(config.getWidth() + "x" + config.getHeight());
            UIUtils.viewVisible(mResolution);
        }
        if (mBitRate != null) {
            String template = "%s";
            double bitRate = config.getBitRate();
            if (config instanceof CameraCodecConfig) {
                BitRateLevel bitRateLevel = ((CameraCodecConfig) config).getBitRateLevel();
                template = bitRateLevel.getText() + "(%s)";
            }
            String s = VUtils.toMbps(bitRate) + "mbps";
            mBitRate.setText(String.format(template, s));
            UIUtils.viewVisible(mBitRate);
        }
        if (mCodec != null) {
            mCodec.setText(config.getVideoCodecID().getText());
            UIUtils.viewVisible(mCodec);
        }
    }

    @Override
    public void initVideoCodec(IVideoCodec codec) {
        mVideoView.initVideoCodec(codec);
        final VideoCodecCallBack callBack = codec.getCallBack();
        codec.setCallBack(new VideoCodecCallBack() {
            @Override
            public void start(final VideoCodecConfig config) {
                if (callBack != null) {
                    callBack.start(config);
                }
                if (!CodecView.sDebug) {
                    return;
                }
                HandlerUtils.runOnUiThread(() -> setParameter(config));
            }

            @Override
            public void stop() {
                if (callBack != null) {
                    callBack.stop();
                }
            }

            @Override
            public void clear() {
                if (callBack != null) {
                    callBack.clear();
                }
            }

            @Override
            public void error(Exception e) {

            }
        });
    }

    @Override
    public IVideoCodec getVideoCodec() {
        return mVideoView.getVideoCodec();
    }

    @Override
    public VideoCodecConfig getConfig() {
        return mVideoView.getConfig();
    }

    @Override
    public String getSign() {
        return mVideoView.getSign();
    }

    @Override
    public void create(Surface surface) {
        mVideoView.create(surface);
    }

    @Override
    public void start() {
        mVideoView.start();
    }

    @Override
    public void restart() {
        mVideoView.restart();
    }

    @Override
    public void resume() {
        mVideoView.resume();
    }

    @Override
    public void pause() {
        mVideoView.pause();
    }

    @Override
    public void stop() {
        mVideoView.stop();
    }

    @Override
    public void destroy() {
        mVideoView.destroy();
    }

    /**
     * 移动位置
     */
    public void setPosition(PointF position) {
        if (position == null) {
            return;
        }
        setX(position.x);
        setY(position.y);
    }

    @Override
    public void restore(Map<String, Object> configs) {
        mVideoView.restore(configs);
    }

    @Override
    public void saved(Map<String, Object> configs) {
        mVideoView.saved(configs);
    }
}
