package DB.SimpleVoxelRenderer;

import static java.lang.Math.abs;
import static java.lang.Math.min;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;

public class Vly_Parser {

    private int voxel_num;
    private InputStream is;
    private ArrayList<float[]> vert_indexcol;
    private ArrayList<float[]> colors;
    private float gridWidth,gridHeight,gridDepth;
    private float xmin,xmax,ymin,ymax,zmin,zmax;

    private float[] VCOLBUF;
    private int[] INDBUF;


    public Vly_Parser(InputStream inputstream){
        this.is = inputstream;

        this.vert_indexcol = new ArrayList<float[]>();
        this.colors = new ArrayList<float[]>();

    }

    public void parse() throws IOException{
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        Iterator<String> it = reader.lines().iterator();

        String line;
        int n_vox = 1;
        int n_cols = 0;

        while(it.hasNext()){
            line = it.next();
            System.out.println(line);

            if(line.contains("grid_size")){
                String[] gridsize = line.split(" ");
                gridWidth = Float.parseFloat(gridsize[1]);
                gridHeight = Float.parseFloat(gridsize[3]);
                gridDepth = Float.parseFloat(gridsize[2]);
            }

            else if(line.contains("voxel_num")){
                int index1 = line.lastIndexOf("voxel_num: ");
                index1+=("voxel_num: ").length();
                this.voxel_num = Integer.parseInt(line.substring(index1).trim());
                System.out.println("The file describes " +  this.voxel_num + " voxels");
            }

            //righe dei voxel
            else {
                if(n_vox <= this.voxel_num){
                    line = line.trim();
                    String[] coords = line.split(" ");
                    float x = Float.parseFloat(coords[0]);
                    // siccome z in file considerato up, invertiamo lettura assi tra y,z
                    float y = Float.parseFloat(coords[2]);
                    float z = Float.parseFloat(coords[1]);
                    float ind_col = Float.parseFloat(coords[3]);

                    float[] barycentre = new float[]{x,y,z,ind_col};
                    vert_indexcol.add(barycentre);

                    n_vox++;
                }

                //righe dei colori
                else {
                    line = line.trim();

                    String[] rgb = line.split(" ");
                    int index = Integer.parseInt(rgb[0]);
                    float r = Float.parseFloat(rgb[1]) / 256.0f;
                    float g = Float.parseFloat(rgb[2]) / 256.0f;
                    float b = Float.parseFloat(rgb[3]) / 256.0f;

                    float[] c = new float[]{r,g,b};

                    this.colors.add(c);
                }
            }

        }
        StoreVertexColorBufferAndIndicesBuffer();
    }


    /*
     * Parsando il file ho due array: baricentri + indice colore associato e colori
     * concateno tutto per avere x,y,z,r,g,b per poter istanziare un oggetto Voxel
     * creo array di Voxel, da essi calcolo vertexbuffer e indexbuffer
     */
    public void StoreVertexColorBufferAndIndicesBuffer(){
        Voxel voxels[] = new Voxel[this.voxel_num];

        for(int i = 0; i < this.voxel_num;i++){
            float[] v = vert_indexcol.get(i);
            float x = v[0];
            float y = v[1];
            float z = v[2];

            int colj = (int) v[3]; // indice del colore

            float[] c = colors.get(colj);
            float r = c[0];
            float g = c[1];
            float b = c[2];

            voxels[i] = new Voxel(x,y,z,r,g,b,i);
        }

        this.VCOLBUF = new float[this.voxel_num * 6 * 8];
        System.out.println(VCOLBUF.length);
        this.INDBUF = new int[this.voxel_num * 36];

        int j = 0;

        for(Voxel vox: voxels){

            float tmp[] = vox.getVerticesAndColors();
            int tmplen = tmp.length;
            for(int i = 0; i < tmplen;i++){
                VCOLBUF[ tmplen * j + i] = tmp[i];
            }

            //store indices buffer
            int indtmp[] = vox.getIndices();
            int indlen = indtmp.length;
            for(int i = 0; i < indlen; i++){
                INDBUF[indlen * j + i] = indtmp[i];
            }

            j++;
        }
    }

    /*  calcolo centro della figura axis-wise
     *  queste distanze andranno tolte dai centri dei voxel, per centrarli col corrispettivo asse
     * x_centrato = x0 - x_centro_figura -> lo faccio nel translate matrix
     */
    public float[] calc_displacement_to_center(){
        float temp[] = vert_indexcol.get(0);
        xmin = temp[0];
        xmax = temp[0];
        ymin = temp[1];
        ymax = temp[1];
        zmin = temp[2];
        zmax = temp[2];

        for(float[] t:vert_indexcol){
            xmin = xmin < t[0] ? xmin : t[0];
            xmax = xmax > t[0] ? xmax : t[0];

            ymin = ymin < t[1] ? ymin : t[1];
            ymax = ymax > t[1] ? ymax : t[1];

            zmin = zmin < t[2] ? zmin : t[2];
            zmax = zmax > t[2] ? zmax : t[2];
        }

        float x_distance = (xmin + xmax) / 2;
        float y_distance = (ymin + ymax) / 2;
        float z_distance = (zmin + zmax) / 2;

        return new float[]{x_distance,y_distance,z_distance};
    }

    /*
     * funzioni per gestire scaling e distanza viewer a seconda della grandezza dell'oggetto da disegnare
     */
    public float get_Distance_camera(){
        return Math.max(gridWidth,Math.max(gridDepth,gridHeight));
    }
    public float getScaleF(){

        float xscale = abs(xmax-xmin);
        float yscale = abs(ymax-ymin);
        float zscale = abs(zmax-zmin);

        return Math.max(zscale,Math.max(xscale,yscale));
    }

    /*
     * getters di varie strutture dati utilizzate
     */
    public float[] getVertBuf(){return this.VCOLBUF;};

    public int[] getIndBuf(){return this.INDBUF;};

    public ArrayList<float[]> getVertArray(){
        return this.vert_indexcol;
    }
    public ArrayList<float[]> getColArray(){
        return this.colors;
    }

    public int getVoxel_num(){
        return this.voxel_num;
    }

    /*
     * mi calcolo il buffer di offset e colori che passo per instancing
     */
    public float[] getOffsetColor(){
        float[] buff = new float[voxel_num * 6];
        int n=0;
        for(int i=0;i<voxel_num;i++){
            float[] off = this.getVertArray().get(i);
            float[] col = this.getColArray().get((int)off[3]);
            for(int j=0;j<3;j++){
                buff[n++] = off[j];
            }
            for(int j=0;j<3;j++){
                buff[n++] = col[j];
            }
        }
        return buff;
    }

}
