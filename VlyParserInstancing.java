package DB.SimpleVoxelRenderer;

import android.util.Log;

import java.io.InputStream;
import java.util.ArrayList;

public class VlyParserInstancing extends Vly_Parser{

    public VlyParserInstancing(InputStream is){
        super(is);

    }
    /*
     * per instancing non mi serve avere array con vertici e attributi consecutivi
     * override vuoto per riutilizzare il codice che ho gia scritto e calcolare solo dati (vertici e colori) senza salvarli in unico array
     */
    @Override
    public void StoreVertexColorBufferAndIndicesBuffer() {
        ;
    }
}
