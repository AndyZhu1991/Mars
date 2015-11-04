package com.koolew.mars.view;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.Log;

import com.koolew.mars.camerautils.CameraInstance;
import com.koolew.mars.opengl.filter.FrameRenderer;
import com.koolew.mars.opengl.filter.FrameRendererBlur;
import com.koolew.mars.opengl.filter.FrameRendererDrawOrigin;
import com.koolew.mars.opengl.filter.FrameRendererEdge;
import com.koolew.mars.opengl.filter.FrameRendererEmboss;
import com.koolew.mars.opengl.filter.FrameRendererLerpBlur;
import com.koolew.mars.opengl.filter.FrameRendererWave;
import com.koolew.mars.opengl.render.GLTextureView;
import com.koolew.mars.opengl.utils.Common;

import org.bytedeco.javacpp.opencv_core;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;


public class FilterGLTextureView extends GLTextureView implements GLSurfaceView.Renderer,
        SurfaceTexture.OnFrameAvailableListener {

    public static final String LOG_TAG = Common.LOG_TAG;

    public int viewWidth;
    public int viewHeight;

    private FrameRenderer mMyRenderer;

    private SurfaceTexture mSurfaceTexture;
    private int mTextureID;
    private SurfaceListener mSurfaceListener;

    private FrameListener mFrameListener;
    private Rect roiRect;
    opencv_core.IplImage roiImage = opencv_core.IplImage.create(
            480, 270, opencv_core.IPL_DEPTH_8U, 4);

    public FrameRenderer.Viewport drawViewport;

    public class ClearColor {
        public float r, g, b, a;
    }

    public ClearColor clearColor;

    public enum FilterButtons {
        Filter_Wave,
        Filter_Blur,
        Filter_Emboss,
        Filter_Edge,
        Filter_BlurLerp,
    }

    public synchronized void setFrameRenderer(final FilterButtons filterID) {
        Log.i(LOG_TAG, "setFrameRenderer to " + filterID);
        queueEvent(new Runnable() {
            @Override
            public void run() {
                FrameRenderer renderer = null;
                boolean isExternalOES = true;
                switch (filterID) {
                    case Filter_Wave:
                        renderer = FrameRendererWave.create(isExternalOES);
                        if (renderer != null)
                            ((FrameRendererWave) renderer).setAutoMotion(0.4f);
                        break;
                    case Filter_Blur:
                        renderer = FrameRendererBlur.create(isExternalOES);
                        if(renderer != null) {
                            ((FrameRendererBlur) renderer).setSamplerRadius(50.0f);
                        }
                        break;
                    case Filter_Edge:
                        renderer = FrameRendererEdge.create(isExternalOES);
                        break;
                    case Filter_Emboss:
                        renderer = FrameRendererEmboss.create(isExternalOES);
                        break;
                    case Filter_BlurLerp:
                        renderer = FrameRendererLerpBlur.create(isExternalOES);
                        if(renderer != null) {
                            ((FrameRendererLerpBlur) renderer).setIntensity(3);
                        }
                        break;
                    default:
                        break;
                }

                if (renderer != null) {
                    mMyRenderer.release();
                    mMyRenderer = renderer;
                    mMyRenderer.setTextureSize(viewWidth, viewHeight);
                    mMyRenderer.setRotation((float) Math.PI / 2.0f);
                }

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

    private CameraInstance cameraInstance() {
        return CameraInstance.getInstance(480, 270);
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
        FrameRendererDrawOrigin rendererWave = FrameRendererDrawOrigin.create(false);
        if(!rendererWave.init(true)) {
            Log.e(LOG_TAG, "init filter failed!\n");
        }
        mMyRenderer = rendererWave;

        rendererWave.setRotation((float) Math.PI / 2.0f);

        requestRender();

        if (mSurfaceListener != null) {
            mSurfaceListener.onSurfaceCreated(mSurfaceTexture);
        }

        //cameraInstance().openCamera();
        //resize(cameraInstance().previewWidth(), cameraInstance().previewHeight());
        startPreview();
    }

    public void startPreview() {
        cameraInstance().startPreview(mSurfaceTexture);
        if (cameraInstance().getCameraID() == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            setRotationX(180);
        }
        else {
            setRotationX(0);
        }
    }

    private void calcViewport() {
        drawViewport = new FrameRenderer.Viewport();

        drawViewport.width = viewWidth;
        drawViewport.height = viewHeight;
        drawViewport.x = 0;
        drawViewport.y = 0;
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        Log.i("stdzhu", String.format("onSurfaceChanged: %d x %d", width, height));

        GLES20.glClearColor(clearColor.r, clearColor.g, clearColor.b, clearColor.a);

        viewWidth = width;
        viewHeight = height;


        if (mSurfaceListener != null) {
            mSurfaceListener.onSurfaceChanged(mSurfaceTexture, width, height);
        }
        initRoi();

        calcViewport();
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        mMyRenderer.renderTexture(mTextureID, drawViewport);

        if (mFrameListener != null) {
            GLES20.glReadPixels(roiRect.left, roiRect.top, roiRect.width(), roiRect.height(),
                    GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, roiImage.getByteBuffer());
            if (getRotationX() > -1 && getRotationX() < 1) { // getRotationX() == 0
                opencv_core.cvFlip(roiImage, null, 0);
            }
            Log.d("stdzhu", "new frame: " + System.currentTimeMillis());
            mFrameListener.onNewFrame(roiImage, System.nanoTime() / 1000);
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

    public void setSurfaceListener(SurfaceListener surfaceListener) {
        mSurfaceListener = surfaceListener;
    }

    public void setFrameListener(FrameListener frameListener) {
        mFrameListener = frameListener;
        initRoi();
    }

    private void initRoi() {
        if (mFrameListener != null) {
            roiRect = mFrameListener.getRoiRect(new Rect(0, 0, viewWidth, viewHeight));
            if (roiImage == null || roiImage.width() != roiRect.width()
                    || roiImage.height() != roiRect.height()) {
                roiImage = opencv_core.IplImage.create(
                        roiRect.width(), roiRect.height(), opencv_core.IPL_DEPTH_8U, 4);
            }
        }
    }

    public void clearFrameListener() {
        mFrameListener = null;
    }

    public interface SurfaceListener {
        void onSurfaceCreated(SurfaceTexture surfaceTexture);
        void onSurfaceChanged(SurfaceTexture surfaceTexture, int width, int height);
    }

    public interface FrameListener {
        void onNewFrame(opencv_core.IplImage image, long timestamp);
        // 设置感兴趣区域
        Rect getRoiRect(Rect surfaceRect);
    }
}
