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
     * ????????????video?????????????????????????????????????????????
     *
     * @param videoWidth  ?????????
     * @param videoHeight ?????????
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

        //???1???:?????????????????????View???,????????????????????????.
        matrix.preTranslate((width - videoWidth) / 2, (height - videoHeight) / 2);

        //???2???:?????????????????????fitXY??????????????????,?????????????????????????????????.
        matrix.preScale(videoWidth / (float) width, videoHeight / (float) height);

        //???3???,????????????????????????,???????????????????????????View????????????.??????????????????view????????????????????????????????????
        if (sx >= sy) {
            matrix.postScale(sy, sy, width / 2, height / 2);
        } else {
            matrix.postScale(sx, sx, width / 2, height / 2);
        }

        view.setTransform(matrix);
        view.postInvalidate();
    }

    /**
     * ????????????video???????????????????????????????????????
     *
     * @param videoWidth  ?????????
     * @param videoHeight ?????????
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

        //???1???:?????????????????????View???,????????????????????????.
        matrix.preTranslate((width - videoWidth) / 2, (height - videoHeight) / 2);

        //???2???:?????????????????????fitXY??????????????????,?????????????????????????????????.
        matrix.preScale(videoWidth / (float) width, videoHeight / (float) height);

        //???3???,????????????????????????,??????????????????????????????View??????, ????????????View??????????????????. ??????????????????????????????View?????????,????????????????????????,??????????????????.
        matrix.postScale(maxScale, maxScale, width / 2, height / 2);//?????????????????????????????????View????????????????????????

        view.setTransform(matrix);
        view.postInvalidate();
    }

}
