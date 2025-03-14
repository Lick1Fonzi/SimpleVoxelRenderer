package DB.SimpleVoxelRenderer;

import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glViewport;

import android.content.Context;
import android.graphics.Point;
import android.opengl.GLSurfaceView;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class BasicRenderer implements GLSurfaceView.Renderer {

    protected Context context;
    protected GLSurfaceView surface;
    protected Point screenSize;
    protected float[] colorBuffer;

    public BasicRenderer(){
        this(0.9f,0.9f,0.9f);
    }
    public BasicRenderer(float r, float g, float b){
        this(r,g,b,1); // alpha value is transparency, 1 is opaque
    }
    public BasicRenderer(float r, float g, float b, float a){
        colorBuffer = new float[]{r,g,b,a};
        screenSize = new Point(0,0);
    }

    public void setContextandSurface(Context context, GLSurfaceView surface){
        this.context = context;
        this.surface = surface;
    }

    public Context getContext(){return this.context;}

    public GLSurfaceView getSurface() {
        return surface;
    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        glClearColor(colorBuffer[0],colorBuffer[1],colorBuffer[2],colorBuffer[3]);

    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int w, int h) {
        glViewport(0,0,w,h);
        this.screenSize.x = w;
        this.screenSize.y = h;
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        glClear(GL_COLOR_BUFFER_BIT);
    }
}
