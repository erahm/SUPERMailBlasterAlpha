package com.returnpath.supermailblasteralpha;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;

public class GameActivity extends Activity {

    private GLSurfaceView surfaceView;
    private GLES20Renderer renderer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        surfaceView = new GLSurfaceView(this);

        ActivityManager actman = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
        ConfigurationInfo conf = actman.getDeviceConfigurationInfo();

        if (conf.reqGlEsVersion >= 0x20000) {
            surfaceView.setEGLContextClientVersion(2);
            surfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
            renderer = new GLES20Renderer();
            surfaceView.setRenderer(renderer);

            System.out.println("Using OpenGL ES 2.0 backend.");
        } else {
            // This is "very bad" and "completely unrecoverable".
            System.out.println("OpenGL ES 2.0 not supported.");
            return;
        }

        setContentView(surfaceView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        surfaceView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        surfaceView.onPause();
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        float x = e.getX();
        float y = e.getY();

        // TEST: draw triangle where clicked
        renderer.touchAction(e);
        // check if touch overlaps with player/control box
        // if it does, fire pixel in direction player is facing
        surfaceView.requestRender();
        return true;
    }

}
