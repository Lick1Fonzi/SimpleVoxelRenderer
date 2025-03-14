package DB.SimpleVoxelRenderer;

import android.app.ActivityManager;
import android.content.pm.ConfigurationInfo;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class Voxel_Unimore_Activity extends AppCompatActivity {


    private GLSurfaceView.Renderer renderer;
    private float x,y,x1,y1;

    @Override
    protected void onCreate(Bundle SavedState) {
        super.onCreate(SavedState);
        x = 0;
        y = 0;
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        ConfigurationInfo info = manager.getDeviceConfigurationInfo();

        ConfigurationInfo configurationInfo = manager.getDeviceConfigurationInfo();
        int supported = 1;
        if (configurationInfo.reqGlEsVersion >= 0x30000)
            supported = 3;
        else if (configurationInfo.reqGlEsVersion >= 0x20000)
            supported = 2;

        Log.v("TAG", "Opengl ES supported >= " +
                supported + " (" + Integer.toHexString(configurationInfo.reqGlEsVersion) + " " +
                configurationInfo.getGlEsVersion() + ")");

        GLSurfaceView surface = new GLSurfaceView(this);
        surface.setEGLContextClientVersion(supported);
        surface.setPreserveEGLContextOnPause(true);

        String vlyfile = getIntent().getStringExtra("vlyfile");
        String RenderType = getIntent().getStringExtra("RenderType");

        renderer = new VoxelRenderer(vlyfile);

        if(RenderType.equals("Instanced")){
            renderer = new InstancingVoxelRenderer(vlyfile);
        }

        setContentView(surface);
        ((BasicRenderer) renderer).setContextandSurface(this, surface);
        surface.setRenderer(renderer);
    }

    /*
     * quando ci sono due dita giù faccio scaling, altrimenti ruoto la figura
     * guardando il verso in cui scorre il dito capisco se ingrandire o rimpicciolire
     */
    @Override
    public boolean onTouchEvent(MotionEvent event){
        super.onTouchEvent(event);
        float dir = 0;
        switch (event.getAction()){
            case  MotionEvent.ACTION_DOWN:
                x = event.getX();
                y = event.getY();
                if (event.getPointerCount() == 2){
                    x1 = event.getX(1);
                    y1 = event.getY(1);
                }
                break;
            case MotionEvent.ACTION_MOVE:

                if (event.getPointerCount() == 2){
                    // Pinching con due dita
                    final float tmpx = event.getX(1);
                    final float tmpy = event.getY(1);
                    if(Math.abs(tmpx) < Math.abs(x1)){
                        dir = -1;
                    }
                    if(Math.abs(tmpx) > Math.abs(x1)){
                        dir = 1;
                    }
                    x1 = tmpx;

                    ((VoxelRenderer) renderer).pinchScale(dir);
                }
                else {
                    ((VoxelRenderer) renderer).addAngle((x - event.getX()) / 2, y - event.getY() / 2);
                    x = event.getX();
                    y = event.getY();
                    break;
                }
        }
        return true;
    }
}
