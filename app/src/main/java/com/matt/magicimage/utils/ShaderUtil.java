/**
 * Copyright (C), 2020-2020, Matt Meng
 * All rights reserved.
 */
package com.matt.magicimage.utils;

import android.opengl.GLES20;
import android.util.Log;

/**
 * ShaderUtil
 *
 * @author matt.meng
 * @date 2020/02/02
 */
public class ShaderUtil {
    private static final String TAG = "ShaderUtil";


    /**
     * create shader
     *
     * @param type       shader type
     * @param shaderCode shader code
     * @return shader id
     */
    public static int createShader(int type, String shaderCode) {
        int shaderId = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shaderId, shaderCode);
        // add the source code to the shader and compile it
        GLES20.glCompileShader(shaderId);
        int[] compileStatus = new int[1];
        GLES20.glGetShaderiv(shaderId, GLES20.GL_COMPILE_STATUS, compileStatus, 0);
        if (compileStatus[0] == 0) {
            Log.e(TAG, "compile shader: " + type + ", error: " + GLES20.glGetShaderInfoLog(shaderId));
            GLES20.glDeleteShader(shaderId);
            shaderId = 0;
        }
        return shaderId;
    }
}
