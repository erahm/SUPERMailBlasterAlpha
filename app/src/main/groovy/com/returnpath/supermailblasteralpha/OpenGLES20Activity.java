package com.returnpath.supermailblasteralpha;

import android.app.*;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.opengl.GLSurfaceView;
import android.os.Bundle;

public class OpenGLES20Activity extends Activity {

    private GLSurfaceView surfaceView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        surfaceView = new GLSurfaceView(this);

        ActivityManager actman = (ActivityManager)getSystemService(
                Context.ACTIVITY_SERVICE
        );
        ConfigurationInfo conf = actman.getDeviceConfigurationInfo();

        if (conf.reqGlEsVersion >= 0x20000) {
            surfaceView.setEGLContextClientVersion(2);
            surfaceView.setRenderer(new GLES20Renderer());
        } else {
            // We don't support OpenGL ES 2.0. This is "very bad" and
            // completely "unrecoverable".
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

}
