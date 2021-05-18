package com.github.liaoheng.codec.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.github.liaoheng.codec.util.CConstants;

/**
 * 视频参数
 *
 * @author liaoheng
 * @version 2018-05-22 14:41
 */
public class VideoCodecConfig implements Parcelable {
    private int width = CConstants.DEF_CAMERA_PREVIEW_WIDTH;// 画面宽
    private int height = CConstants.DEF_CAMERA_PREVIEW_HEIGHT;// 画面高
    private int frameRate = CConstants.DEF_FRAME_RATE; //帧率，FPS
    private int bitRate = CConstants.DEF_BIT_RATE;//码率，bits/sec
    private int frameInterval = CConstants.DEF_I_FRAME_INTERVAL;//I帧间隔，单位秒
    private VideoCodecID videoCodecID = VideoCodecID.AVC;

    public VideoCodecConfig() {
    }

    public VideoCodecConfig(int width, int height, int frameRate, int bitRate, int frameInterval,
            VideoCodecID videoCodecID) {
        this.width = width;
        this.height = height;
        this.frameRate = frameRate;
        this.bitRate = bitRate;
        this.frameInterval = frameInterval;
        this.videoCodecID = videoCodecID;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getFrameRate() {
        return frameRate;
    }

    public void setFrameRate(double frameRate) {
        this.frameRate = (int) frameRate;
    }

    public int getBitRate() {
        return bitRate;
    }

    public void setBitRate(int bitRate) {
        this.bitRate = bitRate;
    }

    public int getFrameInterval() {
        return frameInterval;
    }

    public void setFrameInterval(int frameInterval) {
        this.frameInterval = frameInterval;
    }

    public int getVideoCodecIDInt() {
        return videoCodecID == null ? 0 : videoCodecID.getType();
    }

    public VideoCodecID getVideoCodecID() {
        return videoCodecID;
    }

    public void setVideoCodecID(VideoCodecID videoCodecID) {
        setVideoCodecID(videoCodecID.getType());
    }

    public void setVideoCodecID(int videoCodecID) {
        if (videoCodecID == VideoCodecID.HEVC.getType()) {
            this.videoCodecID = VideoCodecID.HEVC;
        } else {
            this.videoCodecID = VideoCodecID.AVC;
        }
    }

    public VideoCodecConfig copy() {
        return new VideoCodecConfig(width, height, frameRate, bitRate, frameInterval, videoCodecID);
    }

    public boolean isUpdate(VideoCodecConfig config){
        if (width !=config.width){
            return true;
        }
        if (height!= config.height){
            return true;
        }
        if (frameRate!=config.frameRate){
            return true;
        }
        if (frameInterval!=config.frameInterval){
            return true;
        }
        if (videoCodecID.getType()!=config.videoCodecID.getType()){
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return "VideoCodecConfig{" +
                "width=" + width +
                ", height=" + height +
                ", frameRate=" + frameRate +
                ", bitRate=" + bitRate +
                ", frameInterval=" + frameInterval +
                ", videoCodecID=" + videoCodecID +
                '}';
    }

    protected VideoCodecConfig(Parcel in) {
        width = in.readInt();
        height = in.readInt();
        frameRate = in.readInt();
        bitRate = in.readInt();
        frameInterval = in.readInt();
        videoCodecID = in.readParcelable(VideoCodecID.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(width);
        dest.writeInt(height);
        dest.writeInt(frameRate);
        dest.writeInt(bitRate);
        dest.writeInt(frameInterval);
        dest.writeParcelable(videoCodecID, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<VideoCodecConfig> CREATOR = new Creator<VideoCodecConfig>() {
        @Override
        public VideoCodecConfig createFromParcel(Parcel in) {
            return new VideoCodecConfig(in);
        }

        @Override
        public VideoCodecConfig[] newArray(int size) {
            return new VideoCodecConfig[size];
        }
    };
}
