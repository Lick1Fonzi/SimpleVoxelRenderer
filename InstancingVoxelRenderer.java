package DB.SimpleVoxelRenderer;

import static android.opengl.GLES20.GL_ARRAY_BUFFER;
import static android.opengl.GLES20.GL_BACK;
import static android.opengl.GLES20.GL_CCW;
import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_CULL_FACE;
import static android.opengl.GLES20.GL_CW;
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
import static android.opengl.GLES20.glUniform3fv;
import static android.opengl.GLES20.glUniformMatrix4fv;
import static android.opengl.GLES20.glUseProgram;
import static android.opengl.GLES20.glVertexAttribPointer;
import static android.opengl.GLES30.glBindVertexArray;
import static android.opengl.GLES30.glDrawElementsInstanced;
import static android.opengl.GLES30.glGenVertexArrays;
import static android.opengl.GLES30.glVertexAttribDivisor;

import android.app.Activity;
import android.content.Context;
import android.opengl.Matrix;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class InstancingVoxelRenderer extends VoxelRenderer{
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
    private int umodelM;
    private float MVP[];
    private float temp[];
    private float inverseModel[];
    private int uInverseModel;
    private float[] lightPos;
    private int uLightPos;
    private float[] eyePos;
    private int uEyePos;
    private float anglex, angley;
    private float xdist,ydist,zdist, zcamera, scalex, maxscale;
    private int voxelnum;

    public InstancingVoxelRenderer(String vlyfile){
        super(vlyfile);
        this.vlyfile = vlyfile;
        lightPos = new float[]{-10f,10f,10f};
        eyePos = new float[]{0f,0f,30f};
        viewM = new float[16];
        modelM = new float[16];
        projM = new float[16];
        MVP = new float[16];
        temp = new float[16];
        inverseModel = new float[16];
        Matrix.setIdentityM(inverseModel,0);
        Matrix.setIdentityM(viewM, 0);
        Matrix.setIdentityM(modelM, 0);
        Matrix.setIdentityM(projM, 0);
        Matrix.setIdentityM(MVP, 0);
    }


    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        super.onSurfaceCreated(gl10, eglConfig);

        // immagine di loading impostata nella classe padre VoxelRenderer

        //VERTEX SHADER
        String vertexSrc = "#version 300 es\n" +
                "\n" +
                "layout(location = 0) in vec3 vPos;\n" +
                "layout(location = 1) in vec3 normal;\n" +
                "layout(location = 3) in vec3 vcolor;\n" +
                "layout(location = 2) in vec3 offset;\n" +
                "uniform mat4 MVP;\n" +
                "uniform mat4 modelMatrix;\n" +
                "uniform mat4 inverseModel;\n" +
                "out vec3 fragModel;\n" +
                "out vec3 transfNormal;\n" +
                "out vec3 color;\n" +
                "\n" +
                "void main(){\n" +
                "vec3 Pos = vPos + offset ;\n" +
                "color = vcolor;\n" +
                "transfNormal = normalize(vec3(inverseModel * vec4(normal,1)  ));\n" +
                "fragModel = vec3(modelMatrix * vec4(vPos,1));\n" +
                "gl_Position = MVP * vec4(Pos,1);\n" +
                "}";
        // FRAGMENT SHADER
        String fragmentSrc = "#version 300 es\n" +
                "\n" +
                "precision mediump float;\n" +
                "\n" +
                "uniform vec3 lightPos;\n" +
                "uniform vec3 eyePos;\n" +
                "in vec3 fragModel;\n" +
                "in vec3 transfNormal;\n" +
                "in vec3 color;\n" +
                "out vec4 fragColor;\n" +
                "\n" +
                "void main() {\n" +
                "vec4 specComponent = vec4(1.0,1.0,1.0,1);\n"+
                "vec4 diffuseComponent = vec4( 0.8 * color,1);\n"+
                "vec4 ambientComponent = vec4(0.3 * color,1);\n" +
                "vec3 eyeDir = normalize(eyePos-fragModel);\n"+
                "vec3 lightDir = normalize(lightPos-fragModel);\n"+
                "float diff = max(dot(lightDir,transfNormal),0.0);\n"+
                "vec3 refl = reflect(-lightDir,transfNormal);\n" +
                "float spec =  pow( max(dot(eyeDir,refl),0.0), 50.0);\n"+
                "fragColor = ambientComponent + diff*diffuseComponent + spec*specComponent; \n"+
                "}";

        shaderHandle = ShaderCompiler.createProgram(vertexSrc, fragmentSrc);

        InputStream is = null;
        VlyParserInstancing vlypars = null;
        try {
            is = context.getAssets().open("pcube.ply");
            PlyObject ply = new PlyObject(is);
            ply.parse();
            index = ply.getIndices();
            vertices = ply.getVertices(); // vertices and normals of a single cube
            is = context.getAssets().open(vlyfile);
            vlypars = new VlyParserInstancing(is);
            vlypars.parse();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        float[] offsetColors = vlypars.getOffsetColor(); //offsets and colors of cubes
        this.voxelnum = vlypars.getVoxel_num(); // numbers di cubi


        // calcolo di quanto spostare la figura per centrarla. Nella drawcall applico questa traslazione
        // sarebbe più efficiente farlo una sola volta quando si calcolano i vertici invece che tutte le volte quando disegno
        float[] distance = vlypars.calc_displacement_to_center();
        this.xdist = distance[0];
        this.ydist = distance[1];
        this.zdist = distance[2];
        //this.zcamera = vlypars.get_Distance_camera();

        // calcolo di quanto scalare la figura
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(metrics);
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;
        this.maxscale = vlypars.getScaleF();
        //this.maxscale = vlypars.get_Distance_camera();
        // proporzione 1 : scalex = x : width  (che è il max tra width depth height dell'oggetto)
        this.maxscale = width / 100 / (maxscale);
        this.scalex = maxscale;
        this.zcamera = vlyfile.equals("christmas.vly") ? 35 : 30;
        this.eyePos[2] = zcamera;
        this.lightPos[2] = zcamera;


        FloatBuffer vertexB = ByteBuffer.allocateDirect(vertices.length * Float.BYTES)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        vertexB.put(vertices);
        vertexB.position(0);

        IntBuffer indexB = ByteBuffer.allocateDirect(index.length * Integer.BYTES)
                .order(ByteOrder.nativeOrder()).asIntBuffer();
        indexB.put(index);
        indexB.position(0);

        FloatBuffer offColB = ByteBuffer.allocateDirect(offsetColors.length * Float.BYTES)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        offColB.put(offsetColors);
        offColB.position(0);

        VAO = new int[1];
        int VBO[] = new int[2];
        int EBO[] = new int[1];
        glGenBuffers(2,VBO,0);
        glGenBuffers(1,EBO,0); // un VAO a parte per gli indici
        glGenVertexArrays(1,VAO,0);
        glBindVertexArray(VAO[0]);

        glBindBuffer(GL_ARRAY_BUFFER,VBO[0]);
        glBufferData(GL_ARRAY_BUFFER,vertexB.capacity() * Float.BYTES,vertexB,GL_STATIC_DRAW);
        glVertexAttribPointer(0,3, GL_FLOAT,false,6*Float.BYTES,0); //vertices
        glVertexAttribPointer(1,3, GL_FLOAT,false,6*Float.BYTES,3*Float.BYTES); // normals
        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);

        glBindBuffer(GL_ARRAY_BUFFER,VBO[1]);
        glBufferData(GL_ARRAY_BUFFER,offColB.capacity() * Float.BYTES,offColB,GL_STATIC_DRAW);
        glVertexAttribPointer(2,3,GL_FLOAT,false,6*Float.BYTES,0); // offset
        glVertexAttribPointer(3,3,GL_FLOAT,false,6*Float.BYTES,3*Float.BYTES); // colors
        glEnableVertexAttribArray(2);
        glEnableVertexAttribArray(3);
        /*
         * invece che usare keyword gl_instanceID dentro allo shader utilizzo attrib divisor
         * in questo modo automaticamente passa ad ogni istanza del cubo un solo set di offset e colori
         */
        glVertexAttribDivisor(2,1);
        glVertexAttribDivisor(3,1);

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER,EBO[0]);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER,indexB.capacity()*Integer.BYTES,indexB,GL_STATIC_DRAW);

        glBindVertexArray(0);

        MVPloc = glGetUniformLocation(shaderHandle, "MVP");
        umodelM = glGetUniformLocation(shaderHandle, "modelMatrix");
        uInverseModel = glGetUniformLocation(shaderHandle,"inverseModel");
        uLightPos = glGetUniformLocation(shaderHandle,"lightPos");
        uEyePos = glGetUniformLocation(shaderHandle,"eyePos");

        glUseProgram(shaderHandle);
        glUniform3fv(uLightPos,1,lightPos,0);
        glUniform3fv(uEyePos,1,eyePos,0);
        glUseProgram(0);

        glEnable(GL_DEPTH_TEST);
        glDepthFunc(GL_LEQUAL);

        glEnable(GL_CULL_FACE);
        glCullFace(GL_BACK);
        glFrontFace(GL_CCW);

    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        super.onDrawFrame(gl10);

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        // prima parte di MVP
        Matrix.multiplyMM(temp,0,projM,0,viewM,0);

        glUseProgram(shaderHandle);
        glBindVertexArray(VAO[0]);

        // faccio S,R,T in questo ordine perchè figura non è centrata, la centro ogni volta qui
        Matrix.setIdentityM(modelM,0);
        Matrix.scaleM(modelM,0,scalex,scalex,scalex);
        Matrix.rotateM(modelM,0,anglex,0,1,0);
        Matrix.translateM(modelM,0,-xdist,-ydist,-zdist);

        glUniformMatrix4fv(umodelM,1,false,modelM,0);
        //compute second part of MVP
        Matrix.multiplyMM(MVP, 0, temp, 0, modelM, 0);
        //send MVP
        glUniformMatrix4fv(MVPloc, 1, false, MVP, 0);
        //compute T(modelM^-1) and send
        Matrix.invertM(inverseModel, 0,modelM,0);
        glUniformMatrix4fv(uInverseModel,1,true,inverseModel,0);
        glUniformMatrix4fv(MVPloc,1,false,MVP,0);

        glDrawElementsInstanced(GL_TRIANGLES,index.length,GL_UNSIGNED_INT,0,this.voxelnum);

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
    }

    /*
     * mi allontano per dare impressione di rimpicciolire oggetto (e viceversa ingrandire mi avvicino)
     */
    public void pinchScale(float x){
        float max = vlyfile.equals("christmas.vly") ? 80f : 60f;
        float min = vlyfile.equals("christmas.vly") ? 40f : 30f;
        zcamera += 0.5 * x;

        if(zcamera<min)
            zcamera = min;
        if(zcamera > max)
            zcamera = max;

        Matrix.setLookAtM(viewM,0,0,0,zcamera,0,0,0,0,1,0);
        eyePos[2] = zcamera;
    }

}
