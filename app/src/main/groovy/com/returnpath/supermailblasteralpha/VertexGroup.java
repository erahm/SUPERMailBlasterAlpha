package com.returnpath.supermailblasteralpha;

import android.opengl.GLES20;
import android.opengl.Matrix;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class VertexGroup {
    // Since position comes first, the offset is 0.
    final private int mPositionOffset = 0;

    // The number of pieces of position data, measured in elements (not bytes).
    // We have x, y, and z --> 3 data.
    final private int mPositionDataSize = 3;
    // The 'stride' is the distance between two consecutive values of consecutive vertices.
    // Since we store 3 floats for each vertex, our stride is 3 times the sie of each float.
    final private int mStrideBytes = 3 * 4;
    private int mColorHandle;
    private FloatBuffer mBuf;

    private float[] mModelMatrix = new float[16];

    // Gray is always easy to see, so we default to it. This is somewhat unusual, but should help
    // with debuggin.
    private float r = 0.5f;
    private float g = 0.5f;
    private float b = 0.5f;

    // This constructor expects an array of 'packed' data. It should be
    // an array where the first 7 indices correspond to x, y, z, r, g, b, and a for the first vertex. The next 7 for the second, etc.
    public VertexGroup(float[] packed) {
        Matrix.setIdentityM(mModelMatrix, 0);
        mBuf = ByteBuffer.allocateDirect(packed.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        mBuf.put(packed);
        mBuf.position(0);

        color(1.0f, 1.0f, 1.0f);
    }

    public static VertexGroup makeRect(float width, float height, float z) {
        final float loX = -width / 2.0f;
        final float hiX = width / 2.0f;
        final float loY = -height / 2.0f;
        final float hiY = height / 2.0f;

        return new VertexGroup(new float[]{
                loX, loY, z,
                loX, hiY, z,
                hiX, loY, z,
                hiX, hiY, z
        });
    }

    public static VertexGroup makeRect(float width, float height) {
        return VertexGroup.makeRect(width, height, 0.0f);
    }

    public VertexGroup color(float r, float g, float b) {
        this.r = r;
        this.g = g;
        this.b = b;
        return this;
    }

    public VertexGroup scale(float factor) {
        Matrix.scaleM(mModelMatrix, 0, factor, factor, factor);
        return this;
    }

    public VertexGroup shift(float dx, float dy) {
        Matrix.translateM(mModelMatrix, 0, dx, dy, 0.0f);
        return this;
    }

    // Rotate the object. Positive is counter-clockwise.
    public VertexGroup rotate(float dtheta) {
        Matrix.rotateM(mModelMatrix, 0, dtheta, 0.0f, 0.0f, 1.0f);
        return this;
    }

    public void draw(float[] mvpMatrix, int mvpHandle, int posHandle, int colorHandle) {
        mBuf.position(mPositionOffset);

        // Setup a_Position data.
        GLES20.glVertexAttribPointer(posHandle, mPositionDataSize,
                GLES20.GL_FLOAT, false, mStrideBytes, mBuf);
        GLES20.glEnableVertexAttribArray(posHandle);

        float[] local = new float[16];
        Matrix.multiplyMM(local, 0, mvpMatrix, 0, mModelMatrix, 0);

        // Tell OpenGL about the matrix we've been saving up.
        GLES20.glUniformMatrix4fv(mvpHandle, 1, false, local, 0);

        // Make sure the color is setup properly.
        GLES20.glUniform3f(colorHandle, r, g, b);

        // OpenGL needs to know how many vertices we're drawing.
        mBuf.position(0);
        int vertCount = mBuf.remaining() / (mStrideBytes / 4);


        // This is it. This is what draws our stuff!
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, vertCount);
    }
}
