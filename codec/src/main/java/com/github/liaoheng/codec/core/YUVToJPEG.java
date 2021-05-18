package com.github.liaoheng.codec.core;

import android.annotation.SuppressLint;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.media.Image;
import android.media.MediaCodec;

import com.github.liaoheng.codec.model.VideoCodecID;
import com.github.liaoheng.codec.util.CLog;
import com.github.liaoheng.common.util.Callback;
import com.github.liaoheng.common.util.Utils;
import io.reactivex.Observable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

/**
 * 使用{@link MediaCodec#getOutputImage(int)} 得到YUV格式的视频帧数据，在用YuvImage保存到文件。
 *
 * @author liaoheng
 * @version 2018-06-21 15:15
 * @see <a href="https://www.polarxiong.com/archives/Android-MediaCodec%E8%A7%86%E9%A2%91%E6%96%87%E4%BB%B6%E7%A1%AC%E4%BB%B6%E8%A7%A3%E7%A0%81-%E9%AB%98%E6%95%88%E7%8E%87%E5%BE%97%E5%88%B0YUV%E6%A0%BC%E5%BC%8F%E5%B8%A7-%E5%BF%AB%E9%80%9F%E4%BF%9D%E5%AD%98JPEG%E5%9B%BE%E7%89%87-%E4%B8%8D%E4%BD%BF%E7%94%A8OpenGL.html">YUV to JPEG</a>
 */
public class YUVToJPEG {

    private final String TAG = YUVToJPEG.class.getSimpleName();
    private AFFmpeg mFFmpeg = new AFFmpeg();

    public class RemoteImage {
        private Rect mCropRect;
        private int mFormat;
        private RemotePlane[] mPlanes;

        public RemoteImage(Rect mCropRect, int mFormat, Image.Plane[] planes) {
            this.mCropRect = mCropRect;
            this.mFormat = mFormat;
            mPlanes = new RemotePlane[planes.length];
            for (int i = 0; i < planes.length; i++) {
                Image.Plane plane = planes[i];
                mPlanes[i] = new RemotePlane(plane.getRowStride(), plane.getPixelStride(), plane.getBuffer());
            }
        }

        public Rect getCropRect() {
            return mCropRect;
        }

        public int getFormat() {
            return mFormat;
        }

        public RemotePlane[] getPlanes() {
            return mPlanes;
        }
    }

    public class RemotePlane {
        int mRowStride;
        int mPixelStride;
        ByteBuffer mBuffer;

        public RemotePlane(int mRowStride, int mPixelStride, ByteBuffer mBuffer) {
            this.mRowStride = mRowStride;
            this.mPixelStride = mPixelStride;
            this.mBuffer = Utils.cloneByteBuffer(mBuffer);
        }

        public int getRowStride() {
            return mRowStride;
        }

        public int getPixelStride() {
            return mPixelStride;
        }

        public ByteBuffer getBuffer() {
            return mBuffer;
        }
    }

    private File outFile;
    private VideoCodecID mVideoCodecID;
    private Callback<String> mCallback;

    public void setCallback(Callback<String> callback) {
        mCallback = callback;
    }

    public void setParameter(File outFile, VideoCodecID type) {
        this.outFile = outFile;
        mVideoCodecID = type;
    }

    /**
     * 通过YuvImage保存{@link Image}到文件
     */
    @SuppressLint("CheckResult")
    public void compressToJpeg(Image image) {
        if (outFile == null) {
            mCallback.onError(new IOException("outFile is null"));
            return;
        }
        RemoteImage remoteImage = new RemoteImage(image.getCropRect(), image.getFormat(), image.getPlanes());
        Observable.just(remoteImage).subscribeOn(Schedulers.io()).map(new Function<RemoteImage, String>() {
            @Override
            public String apply(RemoteImage image) throws Exception {
                FileOutputStream outStream = new FileOutputStream(outFile);

                Rect rect = image.getCropRect();
                YuvImage yuvImage = new YuvImage(getDataFromImage(image), ImageFormat.NV21, rect.width(), rect.height(),
                        null);
                yuvImage.compressToJpeg(rect, 100, outStream);
                return outFile.getAbsolutePath();
            }
        }).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<String>() {
            @Override
            public void accept(String fileName) {
                mCallback.onSuccess(fileName);
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) {
                CLog.alog().e(TAG, throwable);
                mCallback.onError(throwable);
            }
        });
    }

    /**
     * 使用ffmpeg将h264 or hevc当前关键帧转为jpg文件
     *
     * @param width 视频宽
     * @param height 视频高
     * @param data 必须要关键帧
     */
    @SuppressLint("CheckResult")
    public void ffmpegToJpeg(final int width, final int height, byte[] data) {
        if (outFile == null || mVideoCodecID == null) {
            mCallback.onError(new IOException("outFile or videoCodecID is null"));
            return;
        }
        Observable.just(data).subscribeOn(Schedulers.io()).map(new Function<byte[], String>() {
            @Override
            public String apply(byte[] image) throws Exception {
                if (!mFFmpeg.screenshot(outFile.getAbsolutePath(), width, height, image, image.length,
                        mVideoCodecID.getType())) {
                    throw new IllegalStateException("ffmpeg capture frame failure");
                }
                return outFile.getAbsolutePath();
            }
        }).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<String>() {
            @Override
            public void accept(String fileName) {
                mCallback.onSuccess(fileName);
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) {
                CLog.alog().e(TAG, throwable);
                mCallback.onError(throwable);
            }
        });
    }

    //YUV420Flexible(YUV_420_888) to NV21
    //https://www.polarxiong.com/archives/Android-YUV_420_888%E7%BC%96%E7%A0%81Image%E8%BD%AC%E6%8D%A2%E4%B8%BAI420%E5%92%8CNV21%E6%A0%BC%E5%BC%8Fbyte%E6%95%B0%E7%BB%84.html
    private byte[] getDataFromImage(RemoteImage image) {
        Rect crop = image.getCropRect();
        int format = image.getFormat();
        int width = crop.width();
        int height = crop.height();
        RemotePlane[] planes = image.getPlanes();
        byte[] data = new byte[width * height * ImageFormat.getBitsPerPixel(format) / 8];
        byte[] rowData = new byte[planes[0].getRowStride()];
        //L.alog().d(TAG, "get data from " + planes.length + " planes");
        int channelOffset = 0;
        int outputStride = 1;
        for (int i = 0; i < planes.length; i++) {
            switch (i) {
                case 0:
                    channelOffset = 0;
                    outputStride = 1;
                    break;
                case 1:
                    channelOffset = width * height + 1;
                    outputStride = 2;
                    break;
                case 2:
                    channelOffset = width * height;
                    outputStride = 2;
                    break;
            }
            ByteBuffer buffer = planes[i].getBuffer();
            int rowStride = planes[i].getRowStride();
            int pixelStride = planes[i].getPixelStride();
            //L.alog().d(TAG, "pixelStride " + pixelStride);
            //L.alog().d(TAG, "rowStride " + rowStride);
            //L.alog().d(TAG, "width " + width);
            //L.alog().d(TAG, "height " + height);
            //L.alog().d(TAG, "buffer size " + buffer.remaining());
            int shift = (i == 0) ? 0 : 1;
            int w = width >> shift;
            int h = height >> shift;
            buffer.position(rowStride * (crop.top >> shift) + pixelStride * (crop.left >> shift));
            for (int row = 0; row < h; row++) {
                int length;
                if (pixelStride == 1 && outputStride == 1) {
                    length = w;
                    buffer.get(data, channelOffset, length);
                    channelOffset += length;
                } else {
                    length = (w - 1) * pixelStride + 1;
                    buffer.get(rowData, 0, length);
                    for (int col = 0; col < w; col++) {
                        data[channelOffset] = rowData[col * pixelStride];
                        channelOffset += outputStride;
                    }
                }
                if (row < h - 1) {
                    buffer.position(buffer.position() + rowStride - length);
                }
            }
            //L.alog().d(TAG, "Finished reading data from plane " + i);
        }
        return data;
    }

}
