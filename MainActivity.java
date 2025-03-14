package DB.SimpleVoxelRenderer;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

/*
 *                          MAIN ACTIVITY
 *                        Daniele Bianchini
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private int intent;
    private Bundle bundle;
    private String ContentViewSelector;

    private String vlyfile;


    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        this.ContentViewSelector = "RendererB";
        setContentView(R.layout.activity_main);

        Button button = findViewById(R.id.RendererB);
        button.setOnClickListener(this);

    }

    @Override
    public void onSaveInstanceState(Bundle b) {
        super.onSaveInstanceState(b);
    }

    @Override
    public void onStart(){
        super.onStart();
    }

    @Override
    public void onResume(){
        super.onResume();
    }

    @Override
    public void onPause(){
        super.onPause();
    }

    @Override
    public void onStop(){
        super.onStop();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
    }

    /*
     * schermata iniziale ha solo bottone render che fa apparire i radiobutton per la scelta di immagine da disegnare
     * switcho tra view quando si clicca su render o su back arrow
     * in questo modo posso evitare altre activity o fragments
     */
    @Override
    public void onClick(View view) {
        if(this.ContentViewSelector == "RendererB"){
            setContentView(R.layout.choose_vly);
            ImageButton backB = findViewById(R.id.backButton);
            this.ContentViewSelector = "backButton";
            backB.setOnClickListener(this);

            RadioButton[] radiogroup = {
                    findViewById(R.id.simple),
                    findViewById(R.id.chrk),
                    findViewById(R.id.dragon),
                    findViewById(R.id.monu2),
                    findViewById(R.id.monu16),
                    findViewById(R.id.christmas)
            };
            RadioButton simpleb = findViewById(R.id.simple);
            simpleb.setChecked(true);
            vlyfile = simpleb.getText().toString().toLowerCase() + ".vly";
            for (RadioButton button : radiogroup) {
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        RadioButton clickedButton = (RadioButton) v;
                        if (clickedButton.isChecked()) {
                            vlyfile = clickedButton.getText().toString().toLowerCase() + ".vly";
                            for (RadioButton otherButton : radiogroup) {
                                if (otherButton != clickedButton) {
                                    otherButton.setChecked(false);
                                }
                            }
                        }
                    }
                });
            }


            Button renderB = findViewById(R.id.button1);
            renderB.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this, Voxel_Unimore_Activity.class);
                    intent.putExtra("vlyfile", vlyfile);
                    intent.putExtra("RenderType","Simple");
                    startActivity(intent);
                }
            });
            Button renderB2 = findViewById(R.id.button);
            renderB2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this, Voxel_Unimore_Activity.class);
                    intent.putExtra("vlyfile", vlyfile);
                    intent.putExtra("RenderType","Instanced");
                    startActivity(intent);
                }
            });

        }
        else{
            setContentView(R.layout.activity_main);
            Button button = findViewById(R.id.RendererB);
            this.ContentViewSelector = "RendererB";
            button.setOnClickListener(this);
        }
    }
}