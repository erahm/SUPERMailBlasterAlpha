package com.returnpath.supermailblasteralpha;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.SystemClock;

import java.util.ArrayList;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class GLES20Renderer implements GLSurfaceView.Renderer {

    // This matrix represents the location of our camera and where it looks.
    private float[] mViewMatrix = new float[16];

    // This matrix is used to 'flatten' our world into camera space.
    // When we're doing things on a 2D grid, this isn't going to be
    // very fancy.
    private float[] mProjMatrix = new float[16];

    // This is the final combined matrix that shaders actually want.
    private float[] mVPMatrix = new float[16];
    private int mMVPMatrixHandle;

    // These handles represent buffers within OpenGL for their respective
    // values of the vertices.
    private int mPositionHandle;

    private float windowWidth = 0;
    private float windowHeight = 0;

    // Time since something last updated.
    private float mLastUpdate = 0.0f;

    private ArrayList<VertexGroup> shapes = new ArrayList<VertexGroup>();
    private int mColorHandle;
    private boolean mShapesNeedLoading = true;

    public GLES20Renderer() {
        // A  triangle, made with hard coded points.
        float scale = 200.0f;
        VertexGroup triangle = new VertexGroup(new float[]{
                // x, y, z
                -0.5f * scale, -0.25f * scale, 0.0f,
                +0.5f * scale, -0.25f * scale, 0.0f,
                +0.0f * scale, +0.56f * scale, 0.0f
        });
        triangle.shift(-200.0f, -100.0f)
                .color((float) Math.random(), (float) Math.random(), (float) Math.random());

        // A wide rectangle, made with easier-to-use method calls.
        VertexGroup square = VertexGroup
                // The 0.5f here means this is drawn above the other shapes, which default to 0.
                .makeRect(100.0f, 500.0f, 0.5f)
                .color(0.1f, 0.7f, 1.0f)
                .center(-150.0f, 150.0f);

        shapes.add(triangle);
        shapes.add(square);
    }

    private static int makeShader(String src, int type) {
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

    public void clearShapes() {
        shapes = new ArrayList<VertexGroup>();
    }

    @Override
    public void onSurfaceCreated(GL10 glUnused, EGLConfig config) {
        GLES20.glClearColor(0.1f, 0.1f, 0.1f, 1.0f);

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
                "uniform mat4 u_MVPMatrix;      \n"
                        + "uniform vec3 u_Color;          \n"

                        + "attribute vec4 a_Position;     \n"

                        + "varying vec4 v_Color;          \n"

                        + "void main()                    \n"
                + "{                              \n"
                        + "    v_Color = vec4(u_Color, 1.0); \n"
                        + "    gl_Position = u_MVPMatrix  \n"
                        + "               * a_Position;   \n"
                        + "}                              \n";

        String fragSrc =
                "precision mediump float;       \n"

                        + "varying vec4 v_Color;          \n"

                        + "void main()                    \n"
                + "{                              \n"
                        + "   gl_FragColor = v_Color;     \n"
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
        mColorHandle = GLES20.glGetUniformLocation(program, "u_Color");

        mPositionHandle = GLES20.glGetAttribLocation(program, "a_Position");

        // Everything worked! Tell OpenGL to use this 'program' to render.
        // TODO: We might want save this program, in case we ever want to switch them.
        GLES20.glUseProgram(program);
    }

    @Override
    public void onSurfaceChanged(GL10 glUnused, int width, int height) {
        windowWidth = width;
        windowHeight = height;

        // We postpone loading the shapes because we want them to scale to the size of the screen,
        // which we don't have until here.
        if (mShapesNeedLoading) {
            // A square which is sized at 30% of the smallest window dimension.
            float side = 0.3f * Math.min(windowHeight, windowWidth);
            shapes.add(VertexGroup
                            .makeRect(side, side)
                            .color((float) Math.random(), (float) Math.random(), (float) Math.random())
            );
            mShapesNeedLoading = false;
        }

        // Whenever the view changes, update the matrices to avoid distorting our screen.
        // If we don't update them, the screen stretches from the old resolution to the new one.
        GLES20.glViewport(0, 0, width, height);

        Matrix.orthoM(mProjMatrix, 0,
                -windowWidth / 2.0f, windowWidth / 2.0f,
                -windowHeight / 2.0f, windowHeight / 2.0f,
                -1.0f, 1.0f);

        Matrix.multiplyMM(mVPMatrix, 0, mProjMatrix, 0, mViewMatrix, 0);
    }

    @Override
    public void onDrawFrame(GL10 glUnused) {
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);

        // Animations run more smoothly when they're based off of time, not frames.
        float time = SystemClock.uptimeMillis() / 1000.0f;
        float dTime = time - mLastUpdate;

        for (VertexGroup tri : shapes) {
            tri.draw(mVPMatrix, mMVPMatrixHandle, mPositionHandle, mColorHandle);
        }

        mLastUpdate = time;
    }

}
