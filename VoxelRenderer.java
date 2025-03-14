package DB.SimpleVoxelRenderer;

import static android.opengl.GLES20.GL_ARRAY_BUFFER;
import static android.opengl.GLES20.GL_BACK;
import static android.opengl.GLES20.GL_CCW;
import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_CULL_FACE;
import static android.opengl.GLES20.GL_DEPTH_BUFFER_BIT;
import static android.opengl.GLES20.GL_DEPTH_TEST;
import static android.opengl.GLES20.GL_ELEMENT_ARRAY_BUFFER;
import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_LEQUAL;
import static android.opengl.GLES20.GL_STATIC_DRAW;
import static android.opengl.GLES20.GL_TRIANGLES;
import static android.opengl.GLES20.GL_UNSIGNED_INT;
import static android.opengl.GLES20.glBindBuffer;
import static android.opengl.GLES20.glBufferData;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glCullFace;
import static android.opengl.GLES20.glDepthFunc;
import static android.opengl.GLES20.glDrawElements;
import static android.opengl.GLES20.glEnable;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glFrontFace;
import static android.opengl.GLES20.glGenBuffers;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glUniformMatrix4fv;
import static android.opengl.GLES20.glUseProgram;
import static android.opengl.GLES20.glVertexAttribPointer;
import static android.opengl.GLES30.glBindVertexArray;
import static android.opengl.GLES30.glGenVertexArrays;

import android.app.Activity;
import android.content.Context;
import android.opengl.Matrix;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class VoxelRenderer extends BasicRenderer {

    private String vlyfile;

    private ImageView ImgView;

    private int VAO[];
    private int shaderHandle;
    private float vertices[];
    private int index[];


    private int MVPloc;
    private float viewM[];
    private float projM[];
    private float modelM[];
    private float MVP[];
    private float temp[];
    private float anglex, angley;
    private float xdist,ydist,zdist, zcamera, scalex, maxscale;



    public VoxelRenderer(String vlyfile){
        super();
        this.vlyfile = vlyfile;
        //voxel = new Voxel();
        viewM = new float[16];
        modelM = new float[16];
        projM = new float[16];
        MVP = new float[16];
        temp = new float[16];
        Matrix.setIdentityM(viewM, 0);
        Matrix.setIdentityM(modelM, 0);
        Matrix.setIdentityM(projM, 0);
        Matrix.setIdentityM(MVP, 0);
    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        super.onSurfaceCreated(gl10, eglConfig);
        // imposto una immagine di attesa nel mentre che trasferisco tutti i dati verso la gpu
        pleaseWaitView();

        String vertexSrc = "#version 300 es\n" +
                "\n" +
                "layout(location = 1) in vec3 vPos;\n" +
                "layout(location = 2) in vec3 color;\n" +
                "uniform mat4 MVP;\n" +
                "out vec4 varyingColor;\n" +
                "\n" +
                "void main(){\n" +
                "varyingColor = vec4(color,1);\n" +
                "gl_Position = MVP * vec4(vPos,1);\n" +
                "}";

        String fragmentSrc = "#version 300 es\n" +
                "\n" +
                "precision mediump float;\n" +
                "\n" +
                "in vec4 varyingColor;\n" +
                "out vec4 fragColor;\n" +
                "\n" +
                "void main() {\n" +
                "fragColor = vec4(varyingColor);\n" +
                "}";

        shaderHandle = ShaderCompiler.createProgram(vertexSrc, fragmentSrc);

        InputStream is = null;
        try {
            is = context.getAssets().open(vlyfile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Vly_Parser parser = new Vly_Parser(is);
        try {
            parser.parse();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        index = parser.getIndBuf();
        vertices = parser.getVertBuf();

        float[] distance = parser.calc_displacement_to_center();
        this.xdist = distance[0];
        this.ydist = distance[1];
        this.zdist = distance[2];
        this.zcamera = parser.get_Distance_camera();


        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(metrics);
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;
        this.maxscale = parser.getScaleF();
        // proporzione 1 : scalex = x : width  (che è il max tra width depth height dell'oggetto)
        this.maxscale = width / 100 / (maxscale);
        this.scalex = maxscale;
        this.zcamera = 30f;


        VAO = new int[1];

        FloatBuffer vertexB = ByteBuffer.allocateDirect(vertices.length * Float.BYTES)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        vertexB.put(vertices);
        vertexB.position(0);

        IntBuffer indexB = ByteBuffer.allocateDirect(index.length * Integer.BYTES)
                .order(ByteOrder.nativeOrder()).asIntBuffer();
        indexB.put(index);
        indexB.position(0);

        int VBO[] = new int[2];
        glGenBuffers(2,VBO,0);
        glGenVertexArrays(1,VAO,0);

        glBindVertexArray(VAO[0]);
        glBindBuffer(GL_ARRAY_BUFFER,VBO[0]);
        glBufferData(GL_ARRAY_BUFFER,vertexB.capacity() * Float.BYTES,vertexB,GL_STATIC_DRAW);
        glVertexAttribPointer(1,3, GL_FLOAT,false,6*Float.BYTES,0);
        glVertexAttribPointer(2,3, GL_FLOAT,false,6*Float.BYTES,3*Float.BYTES);
        glEnableVertexAttribArray(1);
        glEnableVertexAttribArray(2);

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER,VBO[1]);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER,indexB.capacity()*Integer.BYTES,indexB,GL_STATIC_DRAW);

        glBindVertexArray(0);

        MVPloc = glGetUniformLocation(shaderHandle, "MVP");

        glEnable(GL_DEPTH_TEST);
        glDepthFunc(GL_LEQUAL);

        glEnable(GL_CULL_FACE);
        glCullFace(GL_BACK);
        glFrontFace(GL_CCW);

    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        super.onDrawFrame(gl10);

        // quando inizia a disegnare togli l'immagine di attesa
        // brutto perchè ad ogni draw frame catcha una exception
        try{
            if(ImgView.getVisibility() != View.GONE){
                hideWait();
            }
        }catch (Exception e){
            ;
        }

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        Matrix.multiplyMM(temp,0,projM,0,viewM,0);
        Matrix.setIdentityM(modelM,0);
        Matrix.scaleM(modelM,0,scalex,scalex,scalex);
        Matrix.rotateM(modelM,0,anglex,0,1,0);
        //Matrix.rotateM(modelM,0,angley,1,0,0);
        Matrix.translateM(modelM,0,-xdist,-ydist,-zdist);

        Matrix.multiplyMM(MVP,0,temp,0,modelM,0);

        glUseProgram(shaderHandle);
        glBindVertexArray(VAO[0]);
        glUniformMatrix4fv(MVPloc,1,false,MVP,0);
        glDrawElements(GL_TRIANGLES,index.length,GL_UNSIGNED_INT,0);

        glBindVertexArray(0);
        glUseProgram(0);
    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int w, int h) {
        super.onSurfaceChanged(gl10, w, h);
        float aspect = ((float)w / (float)(h==0 ? 1 : h));
        Matrix.setLookAtM(viewM,0,0,0,zcamera,0,0,0,0,1,0);
        Matrix.perspectiveM(projM,0,45f,aspect,0.1f,1000f);
    }

    public void addAngle(float x, float y){
        anglex -= x;
        angley += 0; // brutto da vedere se ruota in entrambi, anche se hai piu liberta di movimento
    }

    public void pinchScale(float x){
        zcamera += 0.5 * x;
        Matrix.setLookAtM(viewM,0,0,0,zcamera,0,0,0,0,1,0);

        if(zcamera<30f)
            zcamera = 30f;
        if(zcamera > 60f)
            zcamera = 60f;
    }

    /*
     * workaround per fare comparire l'immagine di attesa
     */
    protected void pleaseWaitView(){
        ((Activity)this.getContext()).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // crea ImageView
                Activity mContext = (Activity) getContext();
                ImgView = new ImageView(mContext);
                ImgView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                ImgView.setImageResource(R.drawable.cras3);
                // aggiungi ImageView
                ((ViewGroup) ((Activity) mContext).findViewById(android.R.id.content)).addView(ImgView);
            }
        });
    }

    /*
     * nasconde la view di caricamento
     */
    private void hideWait(){
        ((Activity) getContext()).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ImgView.setVisibility(View.GONE);
            }
        });
    }

}
