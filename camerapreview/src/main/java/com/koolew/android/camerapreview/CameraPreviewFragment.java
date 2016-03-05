package com.koolew.android.camerapreview;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.koolew.android.camerapreview.opengl.filter.FrameRenderer;
import com.koolew.android.camerapreview.opengl.filter.FrameRendererDrawOrigin;
import com.koolew.android.camerapreview.opengl.render.GLTextureView;
import com.koolew.android.camerapreview.opengl.utils.Common;

import org.bytedeco.javacpp.opencv_core;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by jinchangzhu on 10/22/15.
 */
public class CameraPreviewFragment extends Fragment {

    private static final String TAG = CameraPreviewFragment.class.getSimpleName();

    private static final int DEFAULT_VIDEO_WIDTH = 480;
    private static final int DEFAULT_VIDEO_HEIGHT = 360;

    private FrameLayout mPreviewContainer;
    private FilterGLTextureView mPreviewGLTexture;

    private int wantedWidth = DEFAULT_VIDEO_WIDTH;
    private int wantedHeight = DEFAULT_VIDEO_HEIGHT;

    private int previewWidth;
    private int previewHeight;

    private Object mFrameListenerLock = new Object();
    private FrameListener mFrameListener;


    public void setWantedSize(int width, int height) {
        wantedWidth = width;
        wantedHeight = height;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        cameraInstance().openCamera();
        View root = inflater.inflate(R.layout.camera_preview, null);
        mPreviewContainer = (FrameLayout) root.findViewById(R.id.preview_container);
        mPreviewGLTexture = new FilterGLTextureView(getActivity(), null);
        mPreviewContainer.addView(mPreviewGLTexture, ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
//        mPreviewGLTexture.setPivotY(cameraInstance().previewHeight() / 2);
//        float scale = 1.0f * Utils.getScreenWidthPixel(getActivity()) / cameraInstance().previewWidth();
//        mPreviewGLTexture.setScaleX(scale);
//        mPreviewGLTexture.setScaleY(scale);
        onCameraSizeChanged();
        return root;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cameraInstance().stopCamera();
    }

    private void onCameraSizeChanged() {
        ViewGroup.LayoutParams plp = mPreviewGLTexture.getLayoutParams();
        plp.width = wantedWidth;
        plp.height = (int) (wantedWidth * cameraInstance().getBestPreviewRatio());
        mPreviewGLTexture.setLayoutParams(plp);

        previewWidth = plp.width;
        previewHeight = plp.height;
    }

    private CameraInstance cameraInstance() {
        return CameraInstance.getInstance(wantedWidth, wantedHeight);
    }

    public void switchCamera() {
        cameraInstance().switchCamera();
        onCameraSizeChanged();
        mPreviewGLTexture.startPreview();
    }

    private void showCameraFailDialog() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new AlertDialog.Builder(getActivity())
                        .setMessage(R.string.fail_to_open_camera)
                        .setCancelable(false)
                        .setPositiveButton(android.R.string.ok,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                        getActivity().onBackPressed();
                                    }
                                })
                        .show();
            }
        });
    }

    public void setFrameListener(FrameListener frameListener) {
        mFrameListener = frameListener;
    }

    public void setFrameRenderer(RendererCreator rendererCreator) {
        mPreviewGLTexture.setFrameRenderer(rendererCreator);
    }

    public void clearFrameListener() {
        synchronized (mFrameListenerLock) {
            mFrameListener = null;
        }
    }

    public Rect getRoiRect(Rect surfaceRect) {
        return new Rect(0, (previewHeight - wantedHeight) / 2,
                wantedWidth, (previewHeight + wantedHeight) / 2);
    }

    public interface FrameListener {
        void onNewFrame(opencv_core.IplImage frameImage, long timestamp);
    }

    public interface RendererCreator {
        FrameRenderer createRenderer();
    }

    private class FilterGLTextureView extends GLTextureView implements GLSurfaceView.Renderer,
            SurfaceTexture.OnFrameAvailableListener {

        public static final String LOG_TAG = Common.LOG_TAG;

        public int viewWidth;
        public int viewHeight;

        private FrameRenderer mMyRenderer;

        private SurfaceTexture mSurfaceTexture;
        private int mTextureID;

        private Rect roiRect;
        opencv_core.IplImage roiImage;

        public FrameRenderer.Viewport drawViewport;

        public class ClearColor {
            public float r, g, b, a;
        }

        public ClearColor clearColor;

        public synchronized void setFrameRenderer(final RendererCreator rendererCreator) {
            queueEvent(new Runnable() {
                @Override
                public void run() {
                    FrameRenderer renderer = rendererCreator.createRenderer();

                    if (renderer == null) {
                        return;
                    }

                    mMyRenderer.release();
                    mMyRenderer = renderer;
                    mMyRenderer.setTextureSize(viewWidth, viewHeight);
                    mMyRenderer.setRotation((float) Math.PI / 2.0f);

                    GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);

                    Common.checkGLError("setFrameRenderer...");
                }
            });
        }

        public FilterGLTextureView(Context context, AttributeSet attrs) {
            super(context, attrs);

            setEGLContextClientVersion(2);
            setEGLConfigChooser(8, 8, 8, 8, 8, 0);
            setRenderer(this);
            setRenderMode(RENDERMODE_WHEN_DIRTY);

            clearColor = new ClearColor();
        }

        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            Log.i("stdzhu", "onSurfaceCreated...");

            GLES20.glDisable(GLES20.GL_DEPTH_TEST);
            GLES20.glDisable(GLES20.GL_STENCIL_TEST);

            mTextureID = genSurfaceTextureID();
            mSurfaceTexture = new SurfaceTexture(mTextureID);
            mSurfaceTexture.setOnFrameAvailableListener(this);

            //FrameRendererDrawOrigin rendererWave = FrameRendererDrawOrigin.create(false);
            FrameRenderer renderer = FrameRendererDrawOrigin.create(false);
            if (!renderer.init(true)) {
                renderer.release();
                return;
            }
            mMyRenderer = renderer;

            renderer.setRotation((float) Math.PI / 2.0f);

            requestRender();

            //cameraInstance().openCamera();
            //resize(cameraInstance().previewWidth(), cameraInstance().previewHeight());
            startPreview();
        }

        public void startPreview() {
            cameraInstance().startPreview(mSurfaceTexture);
            final int rotationX;
            if (cameraInstance().getCameraID() == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                rotationX = 180;
            }
            else {
                rotationX = 0;
            }
            post(new Runnable() {
                @Override
                public void run() {
                    setRotationX(rotationX);
                }
            });
        }

        private void calcViewport() {
            drawViewport = new FrameRenderer.Viewport();

            drawViewport.width = viewWidth;
            drawViewport.height = viewHeight;
            drawViewport.x = 0;
            drawViewport.y = 0;
        }

        @Override
        public void onSurfaceChanged(GL10 gl, final int width, final int height) {
            Log.i("stdzhu", String.format("onSurfaceChanged: %d x %d", width, height));

            GLES20.glClearColor(clearColor.r, clearColor.g, clearColor.b, clearColor.a);

            viewWidth = width;
            viewHeight = height;

            initRoi();

            calcViewport();

            post(new Runnable() {
                @Override
                public void run() {
                    mPreviewGLTexture.setPivotX(width / 2);
                    mPreviewGLTexture.setPivotY(height / 2);
                    float scale = 1.0f * mPreviewContainer.getWidth() / width;
                    mPreviewGLTexture.setScaleX(scale);
                    mPreviewGLTexture.setScaleY(scale);

                    setX((mPreviewContainer.getWidth() - width) / 2);
                    setY((mPreviewContainer.getHeight() - height) / 2);
                }
            });
        }

        @Override
        public void onDrawFrame(GL10 gl) {
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

            mMyRenderer.renderTexture(mTextureID, drawViewport);

            synchronized (mFrameListenerLock) {
                if (mFrameListener != null) {
                    GLES20.glReadPixels(roiRect.left, roiRect.top, roiRect.width(), roiRect.height(),
                            GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, roiImage.getByteBuffer());
                    if (getRotationX() > -1 && getRotationX() < 1) { // getRotationX() == 0
                        opencv_core.cvFlip(roiImage, null, 0);
                    }
                    Log.d("stdzhu", "new frame: " + System.currentTimeMillis());
                    mFrameListener.onNewFrame(roiImage, System.nanoTime() / 1000);
                }
            }

            if(mSurfaceTexture != null)
                mSurfaceTexture.updateTexImage();
        }

        @Override
        public void onFrameAvailable(SurfaceTexture surfaceTexture) {
            requestRender();
        }

        private int genSurfaceTextureID() {
            int[] texID = new int[1];
            GLES20.glGenTextures(1, texID, 0);
            GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, texID[0]);
            GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                    GL10.GL_TEXTURE_MIN_FILTER,GL10.GL_LINEAR);
            GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                    GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
            GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                    GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                    GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);
            return texID[0];
        }

        private void initRoi() {
            roiRect = getRoiRect(new Rect(0, 0, viewWidth, viewHeight));
            if (roiImage == null || roiImage.width() != roiRect.width()
                    || roiImage.height() != roiRect.height()) {
                roiImage = opencv_core.IplImage.create(
                        roiRect.width(), roiRect.height(), opencv_core.IPL_DEPTH_8U, 4);
            }
        }
    }
}
