package com.github.liaoheng.codec.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.github.liaoheng.codec.util.ACUtils;

/**
 * 摄像头视频参数
 *
 * @author liaoheng
 * @version 2018-12-12 15:03
 */
public class CameraCodecConfig extends VideoCodecConfig implements Parcelable {
    private BitRateLevel bitRateLevel = BitRateLevel.LOW;
    private VideoResolution resolution = VideoResolution.VGA;

    public CameraCodecConfig() {
        super();
    }

    public CameraCodecConfig(int width, int height, int frameRate, int bitRate, int frameInterval,
            VideoCodecID videoCodecID, BitRateLevel bitRateLevel,
            VideoResolution resolution) {
        super(width, height, frameRate, bitRate, frameInterval, videoCodecID);
        this.bitRateLevel = bitRateLevel;
        this.resolution = resolution;
    }

    public CameraCodecConfig updateResolution(VideoResolution resolution) {
        this.resolution = resolution;
        setWidth(resolution.getWidth());
        setHeight(resolution.getHeight());
        return this;
    }

    public CameraCodecConfig updateBitRateLevel(BitRateLevel bitRateLevel) {
        this.bitRateLevel = bitRateLevel;
        return this;
    }

    @Override
    public int getBitRate() {
        if (bitRateLevel == BitRateLevel.LOW) {
            return ACUtils.getLowBitRate(getWidth(), getHeight());
        } else if (bitRateLevel == BitRateLevel.MID) {
            return ACUtils.getMidBitRate(getWidth(), getHeight());
        } else if (bitRateLevel == BitRateLevel.HEIGHT) {
            return ACUtils.getHeightBitRate(getWidth(), getHeight());
        }
        return super.getBitRate();
    }

    public BitRateLevel getBitRateLevel() {
        return bitRateLevel;
    }

    public void setBitRateLevel(BitRateLevel bitRateLevel) {
        this.bitRateLevel = bitRateLevel;
    }

    public VideoResolution getResolution() {
        return resolution;
    }

    public void setResolution(VideoResolution resolution) {
        this.resolution = resolution;
    }

    @Override
    public CameraCodecConfig copy() {
        return new CameraCodecConfig(getWidth(), getHeight(), getFrameRate(), getBitRate(), getFrameInterval(),
                getVideoCodecID(), bitRateLevel, resolution);
    }

    @Override
    public String toString() {
        return "CameraCodecConfig{" +
                "bitRateLevel=" + bitRateLevel +
                ", resolution=" + resolution +
                "} " + super.toString();
    }

    protected CameraCodecConfig(Parcel in) {
        super(in);
        bitRateLevel = in.readParcelable(BitRateLevel.class.getClassLoader());
        resolution = in.readParcelable(VideoResolution.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeParcelable(bitRateLevel, flags);
        dest.writeParcelable(resolution, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<CameraCodecConfig> CREATOR = new Creator<CameraCodecConfig>() {
        @Override
        public CameraCodecConfig createFromParcel(Parcel in) {
            return new CameraCodecConfig(in);
        }

        @Override
        public CameraCodecConfig[] newArray(int size) {
            return new CameraCodecConfig[size];
        }
    };
}
