package com.github.liaoheng.codec.model;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;

/**
 * @author liaoheng
 * @version 2018-12-12 16:56
 * @see <a href="https://zh.wikipedia.org/wiki/%E6%98%BE%E7%A4%BA%E5%88%86%E8%BE%A8%E7%8E%87%E5%88%97%E8%A1%A8">显示分辨率列表</a>
 */
public enum VideoResolution implements Parcelable {
    QVGA(0, 320, 240, "低"),
    VGA(1, 640, 480, "中"),
    HD(2, 1280, 720, "高"),
    FHD(3, 1920, 1080, "超高");

    private int index;
    private int width;
    private int height;
    private String text;

    VideoResolution(int index, int width, int height, String text) {
        this.index = index;
        this.width = width;
        this.height = height;
        this.text = text;
    }

    public static VideoResolution get(int width, int height) {
        for (VideoResolution videoResolution : values()) {
            if (videoResolution.width == width && videoResolution.height == height) {
                return videoResolution;
            }
        }
        return null;
    }

    public int getIndex() {
        return index;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public String getText() {
        return text;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(index);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<VideoResolution> CREATOR = new Creator<VideoResolution>() {
        @Override
        public VideoResolution createFromParcel(Parcel in) {
            return VideoResolution.create(in.readInt());
        }

        @Override
        public VideoResolution[] newArray(int size) {
            return new VideoResolution[size];
        }
    };

    @NonNull
    public static VideoResolution create(int index) {
        for (VideoResolution videoResolution : VideoResolution.values()) {
            if (videoResolution.getIndex() == index) {
                return videoResolution;
            }
        }
        return VGA;
    }
}
