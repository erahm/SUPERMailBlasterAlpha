package com.returnpath.supermailblasteralpha;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.SystemClock;
import android.view.MotionEvent;

import java.util.ArrayList;
import java.util.Random;

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

    private ArrayList<VertexGroup> invaders = new ArrayList<VertexGroup>();
    private ArrayList<VertexGroup> bullets = new ArrayList<VertexGroup>();
    private int mColorHandle;
    private boolean mShapesNeedLoading = true;

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

    public void clearInvaders() {
        invaders = new ArrayList<VertexGroup>();
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

        // We postpone loading the invaders because we want them to scale to the size of the screen,
        // which we don't have until here.
        if (mShapesNeedLoading) {
            initializeInvaderGrid();
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
        final float time = SystemClock.uptimeMillis() / 1000.0f;
        final float dTime = time - mLastUpdate;

        // Skip the first frame, instead using it as our starting point for time-based animations.
        if (mLastUpdate == 0) {
            mLastUpdate = time;
            return;
        }

        // Inch the invaders down. It should take about 30 seconds for a row to cross the screen.
        final float dyInvader = dTime * -windowHeight / 30;
        for (int i = 0; i < invaders.size(); i += 1) {
            VertexGroup invader = invaders.get(i);
            if (invader == null) {
                continue;
            }
            invader.shift(0, dyInvader);

            // This isn't quite right. It will trigger when the center passes through the
            // bottom of the screen, not the trailing edge. If you watch it closely,
            // you'll see them removed early.
            if (invader.centerY() < -windowHeight / 2) {
                System.out.println("An alien broke through!");
                invaders.set(i, null);
                continue;
            }

            invader.draw(mVPMatrix, mMVPMatrixHandle, mPositionHandle, mColorHandle);
        }

        // Every frame, randomly kill off an invader. Since this happens every frame, it needs
        // to be unlikely enough that they live more than a half second.
        // This is mostly a demonstration that we can remove them.
        if (Math.random() < 0.01 && invaders.size() != 0) {
            Random r = new Random();
            int idx;
            do {
                idx = r.nextInt(invaders.size());
            } while (invaders.get(idx) == null);
            invaders.set(idx, null);
        }

        // Update bullet positions
        for (int i = 0; i < bullets.size(); i++) {
            VertexGroup vg = bullets.get(i);
            if (vg.centerY() > windowHeight / 2) {
                // if bullet is out of frame, swap with last bullet in array
                bullets.remove(i);
                i -= 1;
            }

            vg.shift(0f, -10 * dyInvader);
            vg.draw(mVPMatrix, mMVPMatrixHandle, mPositionHandle, mColorHandle);

        }

        mLastUpdate = time;
    }

    private void initializeInvaderGrid() {
        final int perRow = 15;
        final int rows = 10;
        float block = (1.0f / perRow) * Math.min(windowWidth, windowHeight);
        float size = 0.8f * block;
        float margin = 0.2f * block;

        for (int i = 0; i < perRow; i += 1) {
            for (int j = 0; j < rows; j += 1) {
                VertexGroup invader = VertexGroup
                        .makeRect(size, 0.3f * size)
                        .color(0.0f, 1.0f, 0.0f)
                        .center(i * (size + margin) - 0.25f * windowWidth,
                                j * (size + margin) + 0.5f * windowHeight);
                invaders.add(invader);
            }
        }

    }

    // defines what happens when user taps the screen
    public void touchAction(MotionEvent e) {

        this.bullets.add(VertexGroup
                .makeRect(15f, 15f, 0f)
                .color(1f, 0.5f, 1f)
                .shift(e.getX() - windowWidth / 2,
                        -e.getY() + windowHeight / 2));
    }

}
