package com.returnpath.supermailblasteralpha;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.SystemClock;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class GLES20Renderer implements GLSurfaceView.Renderer {

    // The locations of the corners (vertices) of our triangles.
    private final FloatBuffer mTriVerts1;
    private final FloatBuffer mTriVerts2;
    private final FloatBuffer mTriVerts3;
    // Floats are 32 bits --> 4 bytes. This value should never need to change.
    private final int mBytesPerFloat = 4;
    // The 'stride' is the distance between two consecutive values of
    // consecutive vertices. Since we store 7 floats for each vertex,
    // (x, y, z, r, g, b, and a) there are 7 * sizeof(float) bytes between
    // the nth vertex's, say, x coord and the (n+1)th's x coord.
    private final int mStrideBytes = 7 * mBytesPerFloat;
    // This matrix is used to move models from object space to world space.
    private float[] mModelMatrix = new float[16];
    // This matrix represents the location of our camera and where it looks.
    private float[] mViewMatrix = new float[16];
    // This matrix is used to 'flatten' our world into camera space.
    // When we're doing things on a 2D grid, this isn't going to be
    // very fancy.
    private float[] mProjMatrix = new float[16];
    // This is the final combined matrix that shaders actually want.
    private float[] mMVPMatrix = new float[16];
    private int mMVPMatrixHandle;
    // These handles represent buffers within OpenGL for their respective
    // values of the vertices.
    private int mPositionHandle;
    // Since position comes first, the offset is 0.
    private int mPositionOffset = 0;
    // The number of pieces of position data, measured in elements (not bytes).
    // We have x, y, and z --> 3 data.
    private int mPositionDataSize = 3;
    // Same as above, but for color.
    private int mColorHandle;
    // Our data looks like this: x, y, z, r, g, b, a
    // So we see color starts at offset 3. (Remember, 0-indexed)
    private int mColorOffset = 3;
    // r, g, b, and a.
    private int mColorDataSize = 4;
    private float windowWidth = 0;
    private float windowHeight = 0;

    public GLES20Renderer() {

        // This triangle is red, green, and blue.
        final float[] triangle1VerticesData = {
                // X, Y, Z,
                // R, G, B, A
                -0.5f, -0.25f, 0.0f,
                1.0f, 0.0f, 0.0f, 1.0f,

                0.5f, -0.25f, 0.0f,
                0.0f, 0.0f, 1.0f, 1.0f,

                0.0f, 0.559016994f, 0.0f,
                0.0f, 1.0f, 0.0f, 1.0f
        };

        // This triangle is yellow, cyan, and magenta.
        final float[] triangle2VerticesData = {
                // X, Y, Z,
                // R, G, B, A
                -0.5f, -0.25f, 0.0f,
                1.0f, 1.0f, 0.0f, 1.0f,

                0.5f, -0.25f, 0.0f,
                0.0f, 1.0f, 1.0f, 1.0f,

                0.0f, 0.559016994f, 0.0f,
                1.0f, 0.0f, 1.0f, 1.0f
        };

        // This triangle is white, gray, and black.
        final float[] triangle3VerticesData = {
                // X, Y, Z,
                // R, G, B, A
                -0.5f, -0.25f, 0.0f,
                1.0f, 1.0f, 1.0f, 1.0f,

                0.5f, -0.25f, 0.0f,
                0.5f, 0.5f, 0.5f, 1.0f,

                0.0f, 0.559016994f, 0.0f,
                0.0f, 0.0f, 0.0f, 1.0f
        };

        // Setup the ByteBuffers to be as long as we need them.
        mTriVerts1 = ByteBuffer.allocateDirect(triangle1VerticesData.length * mBytesPerFloat)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer();
        // Then copy over the bytes and reset the ByteBuffer's position to the beginning.
        mTriVerts1.put(triangle1VerticesData).position(0);

        // ... and repeat!
        mTriVerts2 = ByteBuffer.allocateDirect(triangle1VerticesData.length * mBytesPerFloat)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer();
        mTriVerts2.put(triangle2VerticesData).position(0);

        // ... and again!
        mTriVerts3 = ByteBuffer.allocateDirect(triangle1VerticesData.length * mBytesPerFloat)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer();
        mTriVerts3.put(triangle3VerticesData).position(0);
    }

    public static int makeShader(String src, int type) {
        int handle = GLES20.glCreateShader(type);
        if (handle == 0) {
            // Something bad has happened. Shit shit fire our shit!
            throw new RuntimeException("We couldn't even create a shader. GLES20.glCreateShader(*) messed up. This is bad.");
        }

        GLES20.glShaderSource(handle, src);
        GLES20.glCompileShader(handle);

        int[] status = new int[1];
        GLES20.glGetShaderiv(handle, GLES20.GL_COMPILE_STATUS, status, 0);
        if (status[0] == 0) {
            // Something bad but not-as-bad-as-before has happened.
            // Probably a syntax error.
            // Either way, delete the shader object and report the error.
            GLES20.glDeleteShader(handle);
            // TODO: Print the error here.
            throw new RuntimeException("There was a problem compiling a shader. Likely a syntax error.");
        }

        return handle;
    }

    @Override
    public void onSurfaceCreated(GL10 glUnused, EGLConfig config) {
        GLES20.glClearColor(0.9f, 0.9f, 0.9f, 1.0f);

        Matrix.setLookAtM(mViewMatrix, 0, /* Offset */
            // Eye x, y, z
                0.0f,
                0.0f,
                1.0f,
            // Look at x, y, z
                0.0f,
                0.0f,
                0.0f,
                // Define what "up" means. We want to emulate a 2D view, so we choose the y-axis.
                0.0f,
                1.0f,
                0.0f
        );

        // Shaders! Everyone's favorite programmable pipeline feature.
        String vertSrc =
                  "uniform mat4 u_MVPMatrix;      \n" // A constant representing the combined model/view/projection matrix.

                + "attribute vec4 a_Position;     \n" // Per-vertex position information we will pass in.
                + "attribute vec4 a_Color;        \n" // Per-vertex color information we will pass in.

                + "varying vec4 v_Color;          \n" // This will be passed into the fragment shader.

                + "void main()                    \n" // The entry point for our vertex shader.
                + "{                              \n"
                + "   v_Color = a_Color;          \n" // Pass the color through to the fragment shader.
                                                      // It will be interpolated across the triangle.
                + "   gl_Position = u_MVPMatrix   \n" // gl_Position is a special variable used to store the final position.
                + "               * a_Position;   \n" // Multiply the vertex by the matrix to get the final point in
                + "}                              \n";// normalized screen coordinates.

        String fragSrc =
                  "precision mediump float;       \n" // Set the default precision to medium. We don't need as high of a
                                                      // precision in the fragment shader.
                + "varying vec4 v_Color;          \n" // This is the color from the vertex shader interpolated across the
                // triangle per fragment.
                + "void main()                    \n" // The entry point for our fragment shader.
                + "{                              \n"
                + "   gl_FragColor = v_Color;     \n" // Pass the color directly through the pipeline.
                + "}                              \n";

        int vert = makeShader(vertSrc, GLES20.GL_VERTEX_SHADER);
        int frag = makeShader(fragSrc, GLES20.GL_FRAGMENT_SHADER);

        int program = GLES20.glCreateProgram();
        if (program == 0) {
            throw new RuntimeException("GLES20.glCreateProgram() failed. This is less than good.");
        }

        // Bind the shader to the program.
        GLES20.glAttachShader(program, vert);
        GLES20.glAttachShader(program, frag);

        // Bind the attributes too.
        // TODO: These positions (0 and 1) should probably not be hardcoded.
        GLES20.glBindAttribLocation(program, 0, "a_Position");
        GLES20.glBindAttribLocation(program, 1, "a_Color");

        GLES20.glLinkProgram(program);

        int[] status = new int[1];
        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, status, 0);
        if (status[0] == 0) {
            // Linking failed. This is less obvious and we should really print something here.
            // TODO: Print the error message.
            GLES20.glDeleteProgram(program);
            program = 0;
        }

        mMVPMatrixHandle = GLES20.glGetUniformLocation(program, "u_MVPMatrix");
        mPositionHandle = GLES20.glGetAttribLocation(program, "a_Position");
        mColorHandle = GLES20.glGetAttribLocation(program, "a_Color");

        // Everything worked! Tell OpenGL to use this 'program' to render.
        // TODO: We might want save this program, in case we ever want to switch them.
        GLES20.glUseProgram(program);
    }

    @Override
    public void onSurfaceChanged(GL10 glUnused, int width, int height) {
        windowWidth = width;
        windowHeight = height;

        GLES20.glViewport(0, 0, width, height);

        Matrix.orthoM(mProjMatrix, 0,
                -windowWidth / 2.0f, windowWidth / 2.0f,
                -windowHeight / 2.0f, windowHeight / 2.0f,
                -1.0f, 1.0f);
    }

    @Override
    public void onDrawFrame(GL10 glUnused) {
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);

        // Animations run more smoothly when they're based off of time, not frames.
        long time = SystemClock.uptimeMillis();
        float angleDeg = (360.0f / 10000.0f) * (float)time;

        float scaling = 200.0f;
        float offsetRatio = 0.3f;

        // Reset the model matrix before drawing each triangle.
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.scaleM(mModelMatrix, 0, scaling, scaling, scaling);
        Matrix.rotateM(mModelMatrix, 0, angleDeg, 0.0f, 0.0f, 1.0f);
        drawTriangle(mTriVerts1);

        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, 1.0f, offsetRatio * windowHeight, 0.0f);
        Matrix.rotateM(mModelMatrix, 0, 1.1f * angleDeg, 0.0f, 0.0f, 1.0f);
        Matrix.scaleM(mModelMatrix, 0, scaling, scaling, scaling);
        drawTriangle(mTriVerts2);

        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, 0.0f, -offsetRatio * windowHeight, 0.0f);
        Matrix.rotateM(mModelMatrix, 0, 1.2f * angleDeg, 0.0f, 0.0f, 1.0f);
        Matrix.scaleM(mModelMatrix, 0, scaling, scaling, scaling);
        drawTriangle(mTriVerts3);
    }

    private void drawTriangle(final FloatBuffer triBuf) {
        triBuf.position(mPositionOffset);

        // Setup a_Position data.
        GLES20.glVertexAttribPointer(mPositionHandle, mPositionDataSize,
                GLES20.GL_FLOAT, false, mStrideBytes, triBuf);
        GLES20.glEnableVertexAttribArray(mPositionHandle);

        // Setup a_Color data.
        triBuf.position(mColorOffset);
        GLES20.glVertexAttribPointer(mColorHandle, mColorDataSize,
                GLES20.GL_FLOAT, false, mStrideBytes, triBuf);
        GLES20.glEnableVertexAttribArray(mColorHandle);

        Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, mProjMatrix, 0, mMVPMatrix, 0);

        // Tell OpenGL about the matrix we've been saving up.
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);

        // This is it. This is what draws our stuff!
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 3);
    }
}
