package DB.SimpleVoxelRenderer;

import java.util.Random;

public class Voxel {
    private float sidelen = 1f;
    private float baricentro[];
    private float vertices[];
    private int indices[];
    private float vert_col[];
    private float color[];

    // due triangoli compongono una faccia del cubo
    private static final int VOXEL_FACES[] = new int[]{
            0,1,2,1,3,2,  // faccia DX
            4,0,2,4,2,6,  // faccia avanti
            5,4,6,5,6,7,  //faccia SX
            2,3,7,7,6,2,  //faccia sotto
            3,1,5,3,5,7,  // faccia dietro
            5,1,0,0,4,5   // faccia sopra
    };

    public Voxel(){
        this(0f,0f,0f,255,0,0,0);
    }
    public Voxel(float x, float y, float z,float r, float g, float b,int offset){
        this.baricentro = new float[]{x,y,z};
        this.vertices = Calc_all_vertices();
        this.indices = calcIndices(offset);
        this.color = new float[]{r,g,b};
        this.vert_col = storeVertCol();
    }

    /*
     * compute all 8 vertices, in all possible directions starting from centre
     */
    public float[] Calc_all_vertices(){
        float v[] = new float[24];
        calc_vert(v,0,1,1,1);
        calc_vert(v,3,1,1,-1);
        calc_vert(v,6,1,-1,1);
        calc_vert(v,9,1,-1,-1);
        calc_vert(v,12,-1,1,1);
        calc_vert(v,15,-1,1,-1);
        calc_vert(v,18,-1,-1,1);
        calc_vert(v,21,-1,-1,-1);
        return v;
    }

    /*
     * calcolo x,y,z di un singolo vertex, a partire dal centro andando in direzione xsing,ysign,zsign
     */
    public void calc_vert(float arr[],int i, int xsign, int ysign, int zsign){
        float x = this.baricentro[0] + (this.sidelen/2) * xsign;
        float y = this.baricentro[1] + (this.sidelen/2) * ysign;
        float z = this.baricentro[2] + (this.sidelen/2) * zsign;
        arr[i] = x;
        arr[i+1] = y;
        arr[i+2] = z;
    }

    /*
     * compute indices keeping in mind i want a single data structure that holds all cubes data
     * first cube has 0-7 indices, second 8-15 eccetera
     */
    public int[] calcIndices(int offset){
        int ind[] = new int[36];
        for(int i = 0;i< ind.length;i++){
            ind[i] = VOXEL_FACES[i] + offset*8;
        }
        return ind;
    }

    /*
     * to each vertex its coordinates and color, store everything in array to pass to gpu
     */
    public float[] storeVertCol(){
        float v[] = new float[48];
        int j = 0;
        for(int i = 0; i< vertices.length;i = i+3){
            v[j++] = vertices[i];
            v[j++] = vertices[i+1];
            v[j++] = vertices[i+2];

            v[j++] = color[0];
            v[j++] = color[1];
            v[j++] = color[2];
        }
        return v;
    }

    public float[] getVerticesAndColors(){return this.vert_col;}

    public int[] getIndices(){return this.indices;}

}
