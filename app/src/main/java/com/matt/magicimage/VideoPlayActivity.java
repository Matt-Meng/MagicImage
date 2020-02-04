/**
 * Copyright (C), 2020-2020, Matt Meng
 * All rights reserved.
 */
package com.matt.magicimage;

import androidx.appcompat.app.AppCompatActivity;

import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;

import javax.microedition.khronos.opengles.GL;

/**
 * VideoTexture
 *
 * @author matt.meng
 * @date 2020/02/02
 */
public class VideoPlayActivity extends AppCompatActivity {
    private static final String TAG = "VideoPlayActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate");
        setContentView(new CustomGLSurfaceView(this));
    }
}
