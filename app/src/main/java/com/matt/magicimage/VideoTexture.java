/**
 * Copyright (C), 2020-2020, Matt Meng
 * All rights reserved.
 */
package com.matt.magicimage;

import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.os.Environment;
import android.print.PrinterId;
import android.util.Log;
import android.view.Surface;

import com.matt.magicimage.utils.ShaderUtil;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * VideoTexture
 *
 * @author matt.meng
 * @date 2020/02/02
 */
public class VideoTexture {
    private static final String TAG = "VideoTexture";

    private static final int COOR_PER_VERTEX = 2;
    private final String mVertexShaderCode = "attribute vec4 vPosition;"
            + "attribute vec2 vCoordinate;"
            + "varying vec2 aCoordinate;"
            + "void main() {"
            + "gl_Position = vPosition;"
            + "aCoordinate = vCoordinate;"
            + "}";
    private final String mFragmentShaderCode = "#extension GL_OES_EGL_image_external : require\n"
            + "precision mediump float;"
            + "uniform samplerExternalOES vTexture;"
            + "varying vec2 aCoordinate;"
            + "void main() {"
            + "gl_FragColor = texture2D(vTexture, aCoordinate);"
            + "}";
    private FloatBuffer mVertexBuffer;
    private ShortBuffer mDrawListBuffer;
    private FloatBuffer mFragmentBuffer;

    private static float[] sVertexPositions = {
            -1f, 1f,
            -1f, -1f,
            1f, 1f,
            1f, -1f
    };
    private static float[] sTextureCoords = {
            0f, 0f,
            0f, 1f,
            1f, 0f,
            1f, 1f
    };
    private short[] mDrawOrder = {0, 1, 2, 0, 2, 3};
    private int mProgram;
    private int[] mTextureIds = new int[1];
    private int mPositionHandle;
    private int mTextureHandle;
    private int mCoordinateHandle;
    private final int mVertexCount = sVertexPositions.length / COOR_PER_VERTEX;
    private final int mVertexStride = COOR_PER_VERTEX * 4;

    private MediaPlayer mMediaPlayer;
    private SurfaceTexture mSurfaceTexture;
    private boolean mFrameAvailable = false;
    private int mFrameAvaiableCount = 0;
    private int mUpdateTexImageCount = 0;

    public VideoTexture() {
        int vertexShader = ShaderUtil.createShader(GLES20.GL_VERTEX_SHADER, mVertexShaderCode);
        int fragmentShader = ShaderUtil.createShader(GLES20.GL_FRAGMENT_SHADER, mFragmentShaderCode);
        mProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(mProgram, vertexShader);
        GLES20.glAttachShader(mProgram, fragmentShader);
        GLES20.glLinkProgram(mProgram);

        int[] linkStatus = new int[1];
        GLES20.glGetProgramiv(mProgram, GLES20.GL_LINK_STATUS, linkStatus, 0);
        if (linkStatus[0] != GLES20.GL_TRUE) {
            String programInfo = GLES20.glGetProgramInfoLog(mProgram);
            GLES20.glDeleteProgram(mProgram);
            Log.e(TAG, "could not link program " + programInfo);
            return;
        }

        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(sVertexPositions.length * 4);
        byteBuffer.order(ByteOrder.nativeOrder());
        mVertexBuffer = byteBuffer.asFloatBuffer();
        mVertexBuffer.put(sVertexPositions);
        mVertexBuffer.position(0);

        ByteBuffer drawListByteBuffer = ByteBuffer.allocateDirect(mDrawOrder.length * 2);
        drawListByteBuffer.order(ByteOrder.nativeOrder());
        mDrawListBuffer = drawListByteBuffer.asShortBuffer();
        mDrawListBuffer.put(mDrawOrder);
        mDrawListBuffer.position(0);

        mFragmentBuffer = ByteBuffer.allocateDirect(sTextureCoords.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(sTextureCoords);
        mFragmentBuffer.position(0);

        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
        mCoordinateHandle = GLES20.glGetAttribLocation(mProgram, "vCoordinate");
        mTextureHandle = GLES20.glGetUniformLocation(mProgram, "vTexture");

        setupTexture();
        initPlayer();
        mMediaPlayer.setSurface(new Surface(mSurfaceTexture));
    }

    public void draw() {
        synchronized (this) {
            // 解码效率高于绘制效率时，updateTexImage次数低于FrameAvaiable，
            // 导致阻塞MediaPlayer解码缓冲区
            // 因此不使用bool而是使用计数机制
            while (mFrameAvaiableCount != mUpdateTexImageCount) {
                mSurfaceTexture.updateTexImage();
                Log.i(TAG, "updateTexImage");
                mUpdateTexImageCount++;
            }
            /*if (mFrameAvailable) {
                mSurfaceTexture.updateTexImage();
                Log.i(TAG, "updateTexImage");
                mFrameAvailable = false;
            }else {
                return;
            }*/
        }

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glClearColor(1f, 0, 0, 1f);

        GLES20.glUseProgram(mProgram);
        GLES20.glBindTexture(GLES20.GL_TEXTURE, mTextureIds[0]);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);

        GLES20.glEnableVertexAttribArray(mPositionHandle);
        GLES20.glVertexAttribPointer(mPositionHandle, COOR_PER_VERTEX, GLES20.GL_FLOAT, false, mVertexStride, mVertexBuffer);

        GLES20.glEnableVertexAttribArray(mCoordinateHandle);
        GLES20.glVertexAttribPointer(mCoordinateHandle, COOR_PER_VERTEX, GLES20.GL_FLOAT, false, mVertexStride, mFragmentBuffer);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, mVertexCount);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
    }

    private int loadTexture() {
        int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0]);
        GLES20.glActiveTexture(textures[0]);
        GLES20.glUniform1i(mTextureHandle, 0);

        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0);
        return textures[0];
    }

    private void setupTexture() {
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glGenTextures(1, mTextureIds, 0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0);

        mSurfaceTexture = new SurfaceTexture(mTextureIds[0]);
        mSurfaceTexture.setOnFrameAvailableListener(new SurfaceTexture.OnFrameAvailableListener() {
            @Override
            public void onFrameAvailable(SurfaceTexture surfaceTexture) {
                Log.i(TAG, "onFrameAvailable");
                mFrameAvaiableCount++;
                //mFrameAvailable = true;
            }
        });
    }

    private void initPlayer() {
        Log.i(TAG, "initPlayer");
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.start();
            }
        });
        mMediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                Log.e(TAG, "Play error " + what + " " + extra);
                return false;
            }
        });

        String dirPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES).getAbsolutePath();
        String path = dirPath + File.separator + "1.mp4";
        try {
            mMediaPlayer.setDataSource(path);
        } catch (IOException e) {
            Log.e(TAG, "set DataSource error " + e);
        }

        mMediaPlayer.prepareAsync();
    }

    public void release() {
        mMediaPlayer.stop();
        mMediaPlayer.release();
    }
}
