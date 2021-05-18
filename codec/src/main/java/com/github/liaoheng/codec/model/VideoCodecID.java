package com.github.liaoheng.codec.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author liaoheng
 * @version 2018-11-14 15:42
 */
public enum VideoCodecID implements Parcelable {
    AVC(0, new byte[] { 104, 50, 54, 52 },"H264"), HEVC(1, new byte[] { 104, 50, 54, 53 },"H265");
    private int type;
    private byte[] ascii;
    private String text;

    VideoCodecID(int type, byte[] ascii, String text) {
        this.type = type;
        this.ascii = ascii;
        this.text = text;
    }

    public int getType() {
        return type;
    }

    public byte[] getAscii() {
        return ascii;
    }

    public String getText() {
        return text;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(type);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<VideoCodecID> CREATOR = new Creator<VideoCodecID>() {
        @Override
        public VideoCodecID createFromParcel(Parcel in) {
            return VideoCodecID.create(in.readInt());
        }

        @Override
        public VideoCodecID[] newArray(int size) {
            return new VideoCodecID[size];
        }
    };

    public static VideoCodecID create(int type) {
        for (VideoCodecID videoCodecID : VideoCodecID.values()) {
            if (videoCodecID.getType() == type) {
                return videoCodecID;
            }
        }
        return null;
    }

    public static VideoCodecID get(byte[] bytes) {
        if (bytes == null || bytes.length < VideoCodecID.HEVC.ascii.length) {
            return null;
        }
        if (bytes[0] == VideoCodecID.HEVC.ascii[0] && bytes[1] == VideoCodecID.HEVC.ascii[1]
                && bytes[2] == VideoCodecID.HEVC.ascii[2] && bytes[3] == VideoCodecID.HEVC.ascii[3]) {
            return VideoCodecID.HEVC;
        } else {
            return VideoCodecID.AVC;
        }
    }
}
