package com.github.liaoheng.codec.mediacodec;

import android.content.Context;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.os.Build;
import android.text.TextUtils;

import com.github.liaoheng.codec.model.VideoCodecConfig;
import com.github.liaoheng.codec.util.CLog;
import com.github.liaoheng.common.util.PreferencesUtils;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * @author liaoheng
 * @version 2018-11-28 13:42
 */
public abstract class BaseCodecManager {
    private final String TAG = this.getClass().getSimpleName();
    private final MediaCodecList mMediaCodecList = new MediaCodecList(MediaCodecList.REGULAR_CODECS);
    private final String mimeType;

    protected BaseCodecManager(String mimeType) {
        this.mimeType = mimeType;
    }

    public List<String> getSupportedTypeEncoderList() {
        return getSupportedTypeCoderList(true);
    }

    public List<String> getSupportedTypeDecoderList() {
        return getSupportedTypeCoderList(false);
    }

    public List<String> getSupportedTypeCoderList(boolean encoder) {
        LinkedList<String> names = new LinkedList<>();
        MediaCodecInfo[] codecInfos = mMediaCodecList.getCodecInfos();
        if (codecInfos == null || codecInfos.length <= 0) {
            return names;
        }
        for (MediaCodecInfo info : codecInfos) {
            if (!info.getName().startsWith("OMX.")) {
                continue;
            }
            if (checkMediaCodecInfo(info, encoder)) {
                continue;
            }
            if (checkSupportCoder(info)) {
                names.add(info.getName());
            }
        }
        return names;
    }

    private boolean checkMediaCodecInfo(MediaCodecInfo info, boolean encoder) {
        return info.isEncoder() != encoder;
    }

    private boolean checkSupportCoder(MediaCodecInfo info) {
        return isSupportMimeType(info, getDefaultVideoMediaFormat());
    }

    public boolean isSupportMimeType(MediaCodecInfo info, MediaFormat format) {
        String mimeType = format.getString(MediaFormat.KEY_MIME);
        String[] supportedTypes = info.getSupportedTypes();
        for (String supportedType : supportedTypes) {
            if (supportedType.equalsIgnoreCase(mimeType)) {
                MediaCodecInfo.CodecCapabilities caps = info.getCapabilitiesForType(mimeType);
                if (caps != null && caps.isFormatSupported(format)) {
                    for (int i = 0; i < caps.colorFormats.length; i++) {
                        int colorFormat = caps.colorFormats[i];
                        if (isRecognizedFormat(colorFormat)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * Returns true if this is a color format that this test code understands (i.e. we know how
     * to read and generate frames in this format).
     */
    private boolean isRecognizedFormat(int colorFormat) {
        switch (colorFormat) {
            // these are the formats we know how to handle for this test
            case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar:
            case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420PackedPlanar:
            case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar:
            case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420PackedSemiPlanar:
            case MediaCodecInfo.CodecCapabilities.COLOR_TI_FormatYUV420PackedSemiPlanar:
                return true;
            default:
                return false;
        }
    }

    private MediaFormat getDefaultVideoMediaFormat() {
        MediaFormat format = getDecoderMediaFormat(new VideoCodecConfig());
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP) {
            //On Build.VERSION_CODES.LOLLIPOP, the format to MediaCodecList.findDecoder/EncoderForFormat must not contain a frame rate.
            //Use format.setString(MediaFormat.KEY_FRAME_RATE, null) to clear any existing frame rate setting in the format.
            format.setString(MediaFormat.KEY_FRAME_RATE, null);
        }
        return format;
    }

    /**
     * 得到编码器格式信息
     *
     * @param config 配置参数
     */
    public MediaFormat getEncoderMediaFormat(VideoCodecConfig config) {
        MediaFormat format = MediaFormat.createVideoFormat(mimeType, config.getWidth(),
                config.getHeight());
        format.setInteger(MediaFormat.KEY_FRAME_RATE, config.getFrameRate()); // 设置帧率
        format.setInteger(MediaFormat.KEY_BIT_RATE, config.getBitRate()); // 设置码率
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, config.getFrameInterval()); // 设置I帧间隔
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT, mEncoderColorFormat); // 设置颜色空间
        return format;
    }

    /**
     * 得到解码器格式信息
     */
    public MediaFormat getDecoderMediaFormat(VideoCodecConfig config) {
        MediaFormat format = MediaFormat
                .createVideoFormat(mimeType, config.getWidth(), config.getHeight());
        format.setInteger(MediaFormat.KEY_FRAME_RATE, config.getFrameRate());// 设置帧率
        format.setInteger(MediaFormat.KEY_BIT_RATE, config.getBitRate()); // 设置码率
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, config.getFrameInterval());// 设置I帧间隔
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT,
                MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible);// 设置颜色空间
        return format;
    }

    private String VIDEO_CODER_CONFIG = "video_coder_config_1";

    public String getEncoderNameConfigKey() {
        return mimeType + "_encoder_name";
    }

    public String getEncoderColorFormatConfigKey() {
        return mimeType + "_encoder_color_format";
    }

    public String getDecoderNameConfigKey() {
        return mimeType + "_decoder_name";
    }

    private String mEncoderName;
    private int mEncoderColorFormat;
    private String mDecoderName;

    public String getEncoderName() throws IOException {
        if (TextUtils.isEmpty(mEncoderName)) {
            throw new IOException("Unable to find an appropriate codec for " + mEncoderName);
        }
        return mEncoderName;
    }

    public String getDecoderName() throws IOException {
        if (TextUtils.isEmpty(mDecoderName)) {
            throw new IOException("Unable to find an appropriate codec for " + mDecoderName);
        }
        return mDecoderName;
    }

    public int getEncoderColorFormat() {
        return mEncoderColorFormat;
    }

    public void init(Context context) {
        final PreferencesUtils preferencesUtils = PreferencesUtils.from(context, VIDEO_CODER_CONFIG);

        mEncoderName = preferencesUtils.getString(getEncoderNameConfigKey(), null);
        mEncoderColorFormat = preferencesUtils.getInt(getEncoderColorFormatConfigKey(), -1);
        mDecoderName = preferencesUtils.getString(getDecoderNameConfigKey(), null);
        CLog.alog()
                .d(TAG, "init > EncoderName : %s , EncoderColorFormat : %s , DecoderName : %s", mEncoderName,
                        mEncoderColorFormat, mDecoderName);

        if (!TextUtils.isEmpty(mEncoderName) && !TextUtils.isEmpty(mDecoderName) && mEncoderColorFormat != -1) {
            return;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                testCodec(preferencesUtils);
            }
        }).start();
    }

    private void testDecoder(PreferencesUtils preferencesUtils) {
        MediaCodecInfo[] codecInfos = mMediaCodecList.getCodecInfos();
        for (MediaCodecInfo info : codecInfos) {
            if (!info.getName().startsWith("OMX.")) {
                continue;
            }
            MediaFormat format = getDefaultVideoMediaFormat();
            String mimeType = format.getString(MediaFormat.KEY_MIME);
            String[] supportedTypes = info.getSupportedTypes();
            for (String supportedType : supportedTypes) {
                if (supportedType.equalsIgnoreCase(mimeType)) {
                    if (!info.isEncoder()) {
                        MediaCodec mDecoder = null;

                        try {
                            mDecoder = MediaCodec.createByCodecName(info.getName());
                            mDecoder.configure(format, null, null, 0);
                            mDecoder.start();

                            mDecoderName = info.getName();
                            preferencesUtils.putString(getDecoderNameConfigKey(), mDecoderName).apply();

                            CLog.alog().d(TAG, " Decoder : %s  ", mDecoderName);
                            return;
                        } catch (Exception ignored) {
                        } finally {
                            if (mDecoder != null) {
                                mDecoder.stop();
                                mDecoder.release();
                            }
                        }
                    }
                }
            }
        }
    }

    private void testEncoder(PreferencesUtils preferencesUtils) {
        MediaCodecInfo[] codecInfos = mMediaCodecList.getCodecInfos();
        for (MediaCodecInfo info : codecInfos) {
            if (!info.getName().startsWith("OMX.")) {
                continue;
            }
            MediaFormat format = getDefaultVideoMediaFormat();
            String mimeType = format.getString(MediaFormat.KEY_MIME);
            String[] supportedTypes = info.getSupportedTypes();
            for (String supportedType : supportedTypes) {
                if (supportedType.equalsIgnoreCase(mimeType)) {

                    if (!info.isEncoder()) {
                        continue;
                    }

                    MediaCodecInfo.CodecCapabilities caps = info.getCapabilitiesForType(mimeType);
                    if (caps != null && caps.isFormatSupported(format)) {

                        for (int i = 0; i < caps.colorFormats.length; i++) {
                            int colorFormat = caps.colorFormats[i];
                            if (isRecognizedFormat(colorFormat)) {

                                if (info.isEncoder()) {
                                    MediaCodec mEncoder = null;
                                    try {
                                        format.setInteger(MediaFormat.KEY_COLOR_FORMAT, colorFormat); // 设置颜色空间

                                        mEncoder = MediaCodec.createByCodecName(info.getName());
                                        mEncoder.configure(format, null, null,
                                                MediaCodec.CONFIGURE_FLAG_ENCODE);
                                        mEncoder.start();

                                        mEncoderName = info.getName();
                                        mEncoderColorFormat = colorFormat;

                                        preferencesUtils.putString(getEncoderNameConfigKey(), mEncoderName)
                                                .putInt(getEncoderColorFormatConfigKey(), mEncoderColorFormat)
                                                .apply();

                                        CLog.alog().d(TAG, " Encoder : %s  %s ", mEncoderName, mEncoderColorFormat);
                                        return;
                                    } catch (Exception ignored) {
                                    } finally {
                                        if (mEncoder != null) {
                                            mEncoder.stop();
                                            mEncoder.release();
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void testCodec(PreferencesUtils preferencesUtils) {
        testDecoder(preferencesUtils);
        testEncoder(preferencesUtils);
    }
}
