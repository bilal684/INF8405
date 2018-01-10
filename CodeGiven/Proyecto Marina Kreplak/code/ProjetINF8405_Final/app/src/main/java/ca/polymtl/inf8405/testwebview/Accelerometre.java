package ca.polymtl.inf8405.testwebview;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.widget.SeekBar;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by YannleChevoir on 01/03/2017.
 */

class Accelerometre implements SensorEventListener {

    private FireBaseManager fireBaseManager;
    private JeuActivity jeuActivity;

    private double previousAccelerometreXValue=0, currentAccelerometreXValue=0;
    private double previousAccelerometreYValue=0, currentAccelerometreYValue=0;

    private double rapportDeVitesse = 0, leftSpeed = 1, rightSpeed = 1;

    private boolean modifySpeed = false;

    public Accelerometre(FireBaseManager fireBaseManager, JeuActivity jeuActivity){
        this.fireBaseManager = fireBaseManager;
        this.jeuActivity = jeuActivity;
    }

    public void setModifySpeed(boolean modifySpeed){
        this.modifySpeed = modifySpeed;
    }

    public void setRapportDeVitesse(double rapport){
        rapportDeVitesse = rapport;
        fireBaseManager.setMotorsSpeed(rapportDeVitesse, leftSpeed, rightSpeed, jeuActivity);
    }


    @Override
    public void onSensorChanged(SensorEvent event) {

        if(modifySpeed){
            currentAccelerometreXValue=(int)event.values[0];
            if(currentAccelerometreXValue!=previousAccelerometreXValue){
                //On s'assure de ne pas revenir si le changement d'inclinaison est minime
                previousAccelerometreXValue=currentAccelerometreXValue;

                //Rapport de vitesse entre 0 (ecran droit) et 255 (écran couché) (simule la pédale d'accélérateur)
                rapportDeVitesse = ((10-Math.abs(currentAccelerometreXValue))/10);

                fireBaseManager.setMotorsSpeed(rapportDeVitesse, leftSpeed, rightSpeed, jeuActivity);
            }
        }


        currentAccelerometreYValue=(int)event.values[1];


        if(currentAccelerometreYValue<-1){
            currentAccelerometreYValue = -1;
        }else if(currentAccelerometreYValue>1){
            currentAccelerometreYValue = 1;
        }else{
            currentAccelerometreYValue = 0;
        }

        if(currentAccelerometreYValue!=previousAccelerometreYValue){

            //On s'assure de ne pas revenir si le changement d'inclinaison est minime
            previousAccelerometreYValue=currentAccelerometreYValue;

            //Calcul des rapports entre moteurs droit et gauche en fonction de virage serré (1 pour l'un, 0 pour l'autre)(écran tourné completement à droite ou à gauche) ou non (1 et 1) (écran tourné légèrement).
            if(currentAccelerometreYValue<0){
                //On va à gauche
                leftSpeed = 0;//(10+currentAccelerometreYValue)/10;
                rightSpeed = 1;
            }else if (currentAccelerometreYValue>0){
                //On va à droite
                leftSpeed = 1;
                rightSpeed = 0;//(10-currentAccelerometreYValue)/10;
            }else{
                //On va tout droit
                leftSpeed = 1;
                rightSpeed = 1;
            }

            fireBaseManager.setMotorsSpeed(rapportDeVitesse, leftSpeed, rightSpeed, jeuActivity);

        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

}
