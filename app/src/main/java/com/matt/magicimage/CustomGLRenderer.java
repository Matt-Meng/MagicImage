package com.matt.magicimage;

import javax.microedition.khronos.egl.EGLConfig;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.Log;

import javax.microedition.khronos.opengles.GL10;

/**
 * CustomGLRenderer
 *
 * @author matt.meng
 * @date 2020/02/04
 */
public class CustomGLRenderer implements GLSurfaceView.Renderer {
    private static final String TAG = "CustomGLRenderer";
    private VideoTexture mVideoTexture;

    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        // Set the background frame color
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        mVideoTexture = new VideoTexture();
    }

    public void onDrawFrame(GL10 unused) {
        // Redraw background color
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        mVideoTexture.draw();
    }

    public void onSurfaceChanged(GL10 unused, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
    }

    public void release() {
        mVideoTexture.release();
        Log.i(TAG, "release");
    }
}
