package com.example.ikram.myapplicationinf8405;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.net.URI;

public class VideoActivity extends Activity {
    private static final String TAG = "VideoActivity";

    private VideoView mv;
    String URL = "";

    //declare accelerometer text view
    //TextView textAccesX, textAccesY, textAccesZ;

    //declare sensors
    SensorManager sensorManager;
    Sensor sensorAccelerometer;

    //assign initial values to gravity, acceleration and alpha
    double[] gravity = {0, 0, 0};
    double[] linear_acceleration = {0, 0, 0};
    double alpha = 0.8;
    boolean firstTime = false;
    double posX = 0;
    double posY = 0;
    double posZ = 0;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        mv = new VideoView(this);
        setContentView(mv);

        //set up sensors and accelerometer
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensorAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        //get the text view of X,Y,Z
        /*textAccesX = findViewById(R.id.accesX);
        textAccesY = findViewById(R.id.accesY);
        textAccesZ = findViewById(R.id.accesZ);*/

        Bundle extras = getIntent().getExtras();
        if (extras != null && !extras.getString("ip").equals("")) {
            URL = extras.getString("ip");
        } else {
            URL = "http://webcam.aui.ma/axis-cgi/mjpg/video.cgi?resolution=CIF&amp";
        }

        new DoRead().execute(URL);
    }

    //On resume, register accelerometer listener
    public void onResume() {
        super.onResume();
        sensorManager.registerListener(accelerometerListener, sensorAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void onPause() {
        super.onPause();
        mv.stopPlayback();
        finish();
    }

    //On stop , unregister accelerometer listener
    public void onStop() {
        super.onStop();
        sensorManager.unregisterListener(accelerometerListener);
    }

    //Accelerometer listener, set the values to the text view
    public SensorEventListener accelerometerListener = new SensorEventListener() {
        public void onAccuracyChanged(Sensor sensor, int acc) { }

        public void onSensorChanged(SensorEvent event) {

            // Isolate the force of gravity with the low-pass filter.
            gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
            gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
            gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];

            // Remove the gravity contribution with the high-pass filter.
            linear_acceleration[0] = event.values[0] ;//- gravity[0];
            linear_acceleration[1] = event.values[1] ;//- gravity[1];
            linear_acceleration[2] = event.values[2] ;//- gravity[2];

            if (firstTime == false){
                posX = linear_acceleration[0];
                posY = linear_acceleration[1];
                posZ = linear_acceleration[2];
                firstTime = true;
            }

            //Display acceleration in console by taking into account the relative position
            Log.d("X", "X : " + (int)(posX - linear_acceleration[0]) + " m/s^2");
            Log.d("Y", "Y : " + (int)(posY - linear_acceleration[1]) + " m/s^2");
            Log.d("Z", "Z : " + (int)(posZ - linear_acceleration[2]) + " m/s^2");

            //Display normal acceleration
            //Log.d("X", "X : " + (int)linear_acceleration[0] + " m/s^2");
            //Log.d("Y", "Y : " + (int)linear_acceleration[1] + " m/s^2");
            //Log.d("Z", "Z : " + (int)linear_acceleration[2] + " m/s^2");

            //textAccesX.setText("X : " + (int)linear_acceleration[0] + " m/s^2");
            //textAccesY.setText("Y : " + (int)linear_acceleration[1] + " m/s^2");
            //textAccesZ.setText("Z : " + (int)linear_acceleration[2] + " m/s^2");
        }
    };

    public class DoRead extends AsyncTask<String, Void, VideoStreamActivity> {
        protected VideoStreamActivity doInBackground(String... url) {
            HttpResponse res = null;
            DefaultHttpClient httpclient = new DefaultHttpClient();
            Log.d(TAG, "1. Sending http request");
            try {
                res = httpclient.execute(new HttpGet(URI.create(url[0])));
                Log.d(TAG, "2. Request finished, status = "
                        + res.getStatusLine().getStatusCode());
                if (res.getStatusLine().getStatusCode() == 401) {
                    return null;
                }
                return new VideoStreamActivity(res.getEntity().getContent());
            } catch (ClientProtocolException e) {
                e.printStackTrace();
                Log.d(TAG, "Request failed-ClientProtocolException", e);
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG, "Request failed-IOException", e);
            }

            return null;
        }

        protected void onPostExecute(VideoStreamActivity result) {
            mv.setSource(result);
            mv.setDisplayMode(VideoView.SIZE_BEST_FIT);
            mv.showFps(true);
        }
    }
}
