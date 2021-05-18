package com.github.liaoheng.codec.model;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;

/**
 * @author liaoheng
 * @version 2018-12-12 16:56
 */
public enum BitRateLevel implements Parcelable {
    LOW(0, "低"), MID(1, "中"), HEIGHT(2, "高");
    private int level;
    private String text;

    BitRateLevel(int level, String text) {
        this.level = level;
        this.text = text;
    }

    public int getLevel() {
        return level;
    }

    public String getText() {
        return text;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(level);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<BitRateLevel> CREATOR = new Creator<BitRateLevel>() {
        @Override
        public BitRateLevel createFromParcel(Parcel in) {
            return BitRateLevel.create(in.readInt());
        }

        @Override
        public BitRateLevel[] newArray(int size) {
            return new BitRateLevel[size];
        }
    };

    @NonNull
    public static BitRateLevel create(int level) {
        for (BitRateLevel videoCodecID : BitRateLevel.values()) {
            if (videoCodecID.getLevel() == level) {
                return videoCodecID;
            }
        }
        return LOW;
    }

}
