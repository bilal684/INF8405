package ca.polymtl.inf8405.testwebview;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.SeekBar;

public class JeuActivity extends AppCompatActivity {

    private SensorManager sensorManager;

    private FireBaseManager fireBaseManager;
    private Accelerometre accelerometre;

    private Button rearButton, speedButton, clignotantGaucheButton, clignotantDroitButton, warningButton, resetButton;
    private SeekBar seekBarVitesse;
    byte clignotantState = 0;

    public SeekBar getSeekBarVitesse(){
        return seekBarVitesse;
    }

    private Robot robotControle;

    private Utilisateur utilisateur;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jeu);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        robotControle = (Robot) extras.get("ROBOT");
        //utilisateur = new Utilisateur();

        WebView myWebView = (WebView) findViewById(R.id.webview);
        myWebView.loadUrl("http://"+robotControle.getIpAdress()+":8081/");

        rearButton = (Button) findViewById(R.id.rearButton);
        speedButton = (Button) findViewById(R.id.speedButton);
        clignotantGaucheButton = (Button) findViewById(R.id.button_clignotantGauche);
        clignotantDroitButton = (Button) findViewById(R.id.button_clignotantDroit);
        warningButton = (Button) findViewById(R.id.button_warning);
        resetButton = (Button) findViewById(R.id.button_reset);

        seekBarVitesse = (SeekBar) findViewById(R.id.seekBarVitesse);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        fireBaseManager = new FireBaseManager(robotControle);
        accelerometre = new Accelerometre(fireBaseManager, this);
        sensorManager.registerListener(accelerometre, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_UI);

        speedButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN){
                    //Tout le temps où on appuie sur le bouton, on veut que la vitesse soit modifiée
                    accelerometre.setModifySpeed(true);
                }
                if(event.getAction() == MotionEvent.ACTION_UP){
                    //Dès qu'on lache le bouton, on veut que la vitesse reste la même
                    accelerometre.setModifySpeed(false);
                }
                return false;
            }
        });

        rearButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN){
                    //Tout le temps où on appuie sur le bouton, on veut que la vitesse soit modifiée
                    fireBaseManager.setRear(true);
                }
                if(event.getAction() == MotionEvent.ACTION_UP){
                    //Dès qu'on lache le bouton, on veut que la vitesse reste la même
                    fireBaseManager.setRear(false);
                }
                return false;
            }
        });

        clignotantGaucheButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    if(clignotantState==1){
                        clignotantState=(byte)0;
                    }else{
                        clignotantState=(byte)1;
                    }
                    fireBaseManager.setClignotantValue((int)clignotantState);
                }
                return false;
            }
        });

        clignotantDroitButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN){
                    if(clignotantState==2){
                        clignotantState=(byte)0;
                    }else{
                        clignotantState=(byte)2;
                    }
                    fireBaseManager.setClignotantValue((int)clignotantState);
                }
                return false;
            }
        });

        warningButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN){
                    if(clignotantState==3){
                        clignotantState=(byte)0;
                    }else{
                        clignotantState=(byte)3;
                    }
                    fireBaseManager.setClignotantValue((int)clignotantState);
                }
                return false;
            }
        });

        resetButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN){
                    seekBarVitesse.setProgress(0);
                }
                return false;
            }
        });

        seekBarVitesse.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {

                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progressValue, boolean fromUser) {
                        accelerometre.setRapportDeVitesse((double)progressValue/100);
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {}
                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {}
                });

    }

}