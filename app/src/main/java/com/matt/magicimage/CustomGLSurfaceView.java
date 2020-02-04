/**
 * Copyright (C), 2020-2020, Matt Meng
 * All rights reserved.
 */
package com.matt.magicimage;

import android.content.Context;
import android.opengl.GLSurfaceView;

/**
 * CustomGLSurfaceView
 *
 * @author matt.meng
 * @date 2020/02/04
 */
public class CustomGLSurfaceView extends GLSurfaceView {

    private final CustomGLRenderer renderer;

    public CustomGLSurfaceView(Context context) {
        super(context);

        // Create an OpenGL ES 2.0 context
        setEGLContextClientVersion(2);

        renderer = new CustomGLRenderer();

        // Set the Renderer for drawing on the GLSurfaceView
        setRenderer(renderer);
        //setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        renderer.release();
    }
}
