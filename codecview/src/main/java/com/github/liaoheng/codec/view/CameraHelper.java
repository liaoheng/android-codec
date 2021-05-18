package com.github.liaoheng.codec.view;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;

import androidx.annotation.Nullable;

import com.github.liaoheng.codec.util.CLog;
import com.github.liaoheng.codecview.R;
import com.github.liaoheng.common.util.SystemException;

import java.io.IOException;
import java.util.List;

/**
 * 相机功能封装
 *
 * @author liaoheng
 * @version 2018-05-14 15:09
 */
public class CameraHelper {
    private final String TAG = CameraHelper.class.getSimpleName();
    private Camera mCamera; // 相机
    private boolean started; // 相机开启预览标帜
    private Camera.PreviewCallback mPreviewCallback;//相机视频数据回调
    private Context mContext;
    private boolean isBack;//后置摄像头
    private Camera.Size mSupportSize;

    public void reverse() {
        isBack = !isBack;
    }

    /**
     * 当前相机使用的分辨率
     */
    public Camera.Size getSupportSize() {
        return mSupportSize;
    }

    public CameraHelper(Context context, Camera.PreviewCallback mPreviewCallback) {
        mContext = context;
        this.mPreviewCallback = mPreviewCallback;
    }

    /**
     * 创建相机，不指定摄像头方向
     */
    public void createCamera(int width, int height) throws SystemException {
        if (mCamera != null) {
            destroy();
        }
        mCamera = getNewCamera(getFacing(), width, height);
    }

    /**
     * 当前相机使用的摄像头方向
     */
    public int getFacing() {
        if (isBack) {
            return Camera.CameraInfo.CAMERA_FACING_BACK;
        }
        return Camera.CameraInfo.CAMERA_FACING_FRONT;
    }

    /**
     * 切换相机前后摄像头
     */
    @Deprecated
    public void toggleCamera(SurfaceTexture surface) throws SystemException {
        if (mSupportSize == null) {
            return;
        }
        destroy();
        isBack = !isBack;
        createCamera(mSupportSize.width, mSupportSize.height);
        startPreview(surface);
    }

    /**
     * 开始预览
     */
    public void startPreview(SurfaceTexture surface) throws SystemException {
        if (mCamera != null && !started) {
            try {
                mCamera.setPreviewTexture(surface); // 设置预览容器
            } catch (IOException e) {
                destroy();
                throw new SystemException(mContext.getString(R.string.toast_connect_camera_failed), e);
            }
            // 根据预览尺寸计算缓存数据的值
            int previewFormat = mCamera.getParameters().getPreviewFormat();
            Camera.Size previewSize = mCamera.getParameters().getPreviewSize();
            int size = previewSize.width * previewSize.height * ImageFormat
                    .getBitsPerPixel(previewFormat) / 8;
            mCamera.addCallbackBuffer(new byte[size]);
            mCamera.setPreviewCallbackWithBuffer(mPreviewCallback); // 设置预览回调
            mCamera.startPreview(); // 开始预览
            started = true;
            CLog.alog().d(TAG, "startPreview size: w %s , h %s", previewSize.width, previewSize.height);
        }
    }

    /**
     * 停止预览
     */
    public void stopPreview() {
        if (mCamera != null && started) {
            CLog.alog().d(TAG, "stopPreview");
            mCamera.stopPreview();
            mCamera.setPreviewCallbackWithBuffer(null);
            started = false;
        }
    }

    /**
     * 释放相机
     */
    public void destroy() {
        stopPreview();
        CLog.alog().d(TAG, "destroy");
        if (null != mCamera) {
            mCamera.release();
            mCamera = null;
        }
    }

    /**
     * 创建相机
     *
     * @param facing 前后摄像头 {@link Camera.CameraInfo#facing}
     */
    @Nullable
    public Camera getNewCamera(int facing, int width, int height) throws SystemException {
        if (!mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            throw new SystemException(mContext.getString(R.string.toast_no_camera));
        }

        int cameraId = getCameraId(facing);
        if (cameraId == -1) {
            throw new SystemException(mContext.getString(R.string.toast_no_camera));
        }

        CLog.alog().d(TAG, "getNewCamera id: %s", cameraId);

        Camera mCamera = Camera.open(cameraId); // 打开相机
        if (mCamera == null) {
            throw new SystemException(mContext.getString(R.string.toast_connect_camera_failed));
        }

        Camera.Parameters parameters = mCamera.getParameters();
        parameters.setPreviewFormat(ImageFormat.NV21); // 设置预览格式NV21(属于YUV420SP)
        Camera.Size supportMaxSize = getCameraSupportedPreviewSize(parameters, width, height);
        if (null == supportMaxSize) {
            throw new SystemException(mContext.getString(R.string.toast_the_camera_size_is_not_supported));
        }
        parameters.setPreviewSize(supportMaxSize.width,
                supportMaxSize.height); // 还可以设置很多相机的参数，但是建议先遍历当前相机是否支持该配置，不然可能会导致出错；
        mCamera.setParameters(parameters);
        mSupportSize = supportMaxSize;
        return mCamera;
    }

    /**
     * 得到对应镜头ID
     *
     * @param facing 前后摄像头 {@link Camera.CameraInfo#facing}
     */
    public int getCameraId(int facing) {
        int frontIndex = -1;
        int backIndex = -1;
        int cameraCount = Camera.getNumberOfCameras();
        Camera.CameraInfo info = new Camera.CameraInfo();
        for (int cameraIndex = 0; cameraIndex < cameraCount; cameraIndex++) {
            Camera.getCameraInfo(cameraIndex, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                frontIndex = cameraIndex;
            } else if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                backIndex = cameraIndex;
            }
        }
        if (frontIndex != -1 && facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            return frontIndex;
        } else if (backIndex != -1 && facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
            return backIndex;
        } else {
            return -1;
        }
    }

    public Camera.Size getCameraSupportedPreviewSize(Camera.Parameters parameters, int width, int height) {
        List<Camera.Size> previewSizes = parameters.getSupportedPreviewSizes();
        Camera.Size supportSize = null;
        for (Camera.Size size : previewSizes) {
            int w = size.width;
            int h = size.height;
            if (h == height) {
                supportSize = size;
                if (w == width) {
                    break;
                }
            }
        }
        if (supportSize != null) {
            CLog.alog()
                    .d(TAG, "camera supported preview ow: %s oh: %s w: %s h: %s", width, height, supportSize.width,
                            supportSize.height);
        }
        return supportSize;
    }

}
