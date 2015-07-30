package com.returnpath.supermailblasteralpha;

import android.opengl.GLES20;
import android.opengl.Matrix;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class SimpleTriangle {
    // Since position comes first, the offset is 0.
    final private int mPositionOffset = 0;

    // The number of pieces of position data, measured in elements (not bytes).
    // We have x, y, and z --> 3 data.
    final private int mPositionDataSize = 3;

    // Our data looks like this: x, y, z, r, g, b, a
    // So we see color starts at offset 3. (Remember, 0-indexed)
    final private int mColorOffset = 3;

    // r, g, b, and a.
    final private int mColorDataSize = 4;

    // The 'stride' is the distance between two consecutive values of
    // consecutive vertices. Since we store 7 floats for each vertex,
    // (x, y, z, r, g, b, and a) there are 7 * sizeof(float) bytes between
    // the nth vertex's, say, x coord and the (n+1)th's x coord.
    final private int mStrideBytes = 7 * 4;

    private FloatBuffer mBuf;

    private float[] mModel = new float[16];

    // This constructor expects an array of 'packed' data. It should be
    // an array where the first 7 indices correspond to x, y, z, r, g, b, and a for the first vertex. The next 7 for the second, etc.
    public SimpleTriangle(float[] packed) {
        Matrix.setIdentityM(mModel, 0);
        mBuf = ByteBuffer.allocateDirect(packed.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        mBuf.put(packed);
        mBuf.position(0);
    }

    public void scale(float factor) {
        Matrix.scaleM(mModel, 0, factor, factor, factor);
    }

    public void shift(float dx, float dy) {
        Matrix.translateM(mModel, 0, dx, dy, 0.0f);
    }

    // Rotate the object. Positive is counter-clockwise. (I think)
    public void rotate(float dtheta) {
        Matrix.rotateM(mModel, 0, dtheta, 0.0f, 0.0f, 1.0f);
    }

    public void draw(float[] viewMatrix, float[] projMatrix, float[] mvpMatrix, int mvpHandle, int posHandle, int colorHandle) {
        mBuf.position(mPositionOffset);

        // Setup a_Position data.
        GLES20.glVertexAttribPointer(posHandle, mPositionDataSize,
                GLES20.GL_FLOAT, false, mStrideBytes, mBuf);
        GLES20.glEnableVertexAttribArray(posHandle);

        // Setup a_Color data.
        mBuf.position(mColorOffset);
        GLES20.glVertexAttribPointer(colorHandle, mColorDataSize,
                GLES20.GL_FLOAT, false, mStrideBytes, mBuf);
        GLES20.glEnableVertexAttribArray(colorHandle);

        Matrix.multiplyMM(mvpMatrix, 0, viewMatrix, 0, mModel, 0);
        Matrix.multiplyMM(mvpMatrix, 0, projMatrix, 0, mvpMatrix, 0);

        // Tell OpenGL about the matrix we've been saving up.
        GLES20.glUniformMatrix4fv(mvpHandle, 1, false, mvpMatrix, 0);

        // This is it. This is what draws our stuff!
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 3);
    }
}
