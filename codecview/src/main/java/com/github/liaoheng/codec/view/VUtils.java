package com.github.liaoheng.codec.view;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PorterDuff;
import android.graphics.SurfaceTexture;
import android.view.Surface;
import android.view.TextureView;

import java.math.BigDecimal;

/**
 * @author liaoheng
 * @version 2018-05-21 10:10
 */
public class VUtils {
    public static void videoViewStop(IVideoLifeView... views) {
        for (IVideoLifeView view : views) {
            view.stop();
        }
    }

    public static void videoViewStart(IVideoLifeView... views) {
        for (IVideoLifeView view : views) {
            view.start();
        }
    }

    public static void videoViewRestart(IVideoLifeView... views) {
        for (IVideoLifeView view : views) {
            view.restart();
        }
    }

    public static void videoViewResume(IVideoLifeView... views) {
        for (IVideoLifeView view : views) {
            view.resume();
        }
    }

    public static void videoViewPause(IVideoLifeView... views) {
        for (IVideoLifeView view : views) {
            view.pause();
        }
    }

    public static int getLowBitRate(int width, int height) {
        return width * height * 3 / 2;
    }

    public static int getMidBitRate(int width, int height) {
        return width * height * 3;
    }

    public static int getHeightBitRate(int width, int height) {
        return width * height * 3 * 2;
    }

    public static double toMbps(double bitRate) {
        return new BigDecimal(bitRate / 1024 / 1024).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    public static double toKbps(double bitRate) {
        return new BigDecimal(bitRate / 1024).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    public static void clearSurface(SurfaceTexture texture) {
        Surface surface = new Surface(texture);
        if (surface.isValid()) {
            Canvas canvas = surface.lockCanvas(null);
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            surface.unlockCanvasAndPost(canvas);
        }
        surface.release();
    }

    /**
     * 重新计算video的显示位置，让其全部显示并据中
     *
     * @param videoWidth  视频长
     * @param videoHeight 视频宽
     * @see <a href="https://blog.csdn.net/qq_24295537/article/details/53997098">csdn</a>
     */
    public static void updateTextureViewSizeCenter(TextureView view, int videoWidth, int videoHeight) {
        int width = view.getWidth();
        int height = view.getHeight();

        int temp;
        if (width < height) {
            temp = height;
            height = width;
            width = temp;
        }

        float sx = (float) width / (float) videoWidth;
        float sy = (float) height / (float) videoHeight;

        Matrix matrix = new Matrix();

        //第1步:把视频区移动到View区,使两者中心点重合.
        matrix.preTranslate((width - videoWidth) / 2, (height - videoHeight) / 2);

        //第2步:因为默认视频是fitXY的形式显示的,所以首先要缩放还原回来.
        matrix.preScale(videoWidth / (float) width, videoHeight / (float) height);

        //第3步,等比例放大或缩小,直到视频区的一边和View一边相等.如果另一边和view的一边不相等，则留下空隙
        if (sx >= sy) {
            matrix.postScale(sy, sy, width / 2, height / 2);
        } else {
            matrix.postScale(sx, sx, width / 2, height / 2);
        }

        view.setTransform(matrix);
        view.postInvalidate();
    }

    /**
     * 重新计算video的显示位置，裁剪后全屏显示
     *
     * @param videoWidth  视频长
     * @param videoHeight 视频宽
     * @see <a href="https://blog.csdn.net/qq_24295537/article/details/53997098">csdn</a>
     */
    public static void updateTextureViewSizeCenterCrop(TextureView view, int videoWidth, int videoHeight) {
        int width = view.getWidth();
        int height = view.getHeight();

        int temp;
        if (width < height) {
            temp = height;
            height = width;
            width = temp;
        }

        float sx = (float) width / (float) videoWidth;
        float sy = (float) height / (float) videoHeight;

        Matrix matrix = new Matrix();
        float maxScale = Math.max(sx, sy);

        //第1步:把视频区移动到View区,使两者中心点重合.
        matrix.preTranslate((width - videoWidth) / 2, (height - videoHeight) / 2);

        //第2步:因为默认视频是fitXY的形式显示的,所以首先要缩放还原回来.
        matrix.preScale(videoWidth / (float) width, videoHeight / (float) height);

        //第3步,等比例放大或缩小,直到视频区的一边超过View一边, 另一边与View的另一边相等. 因为超过的部分超出了View的范围,所以是不会显示的,相当于裁剪了.
        matrix.postScale(maxScale, maxScale, width / 2, height / 2);//后两个参数坐标是以整个View的坐标系以参考的

        view.setTransform(matrix);
        view.postInvalidate();
    }

}
