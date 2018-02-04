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

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;

public class VideoActivity extends Activity {
    private static final String TAG = "VideoActivity";

    private VideoView mv;
    String URL = "";

    //Declare sensors
    SensorManager sensorManager;
    Sensor sensorAccelerometer;

    //Assign initial values to acceleration, first time sensor change, initial device position
    double[] linear_acceleration = {0, 0, 0};
    double[] posXYZ = {0, 0, 0};

    // Max acceleration and Min acceleration
    double maxAcceleration;
    double minAcceleration;

    // number of interval and
    int numberOfInterval = 35;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        mv = new VideoView(this);
        setContentView(mv);

        //Set up sensors and accelerometer
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensorAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        //get extras passed from previous activity
        Bundle extras = getIntent().getExtras();
        if (extras != null && !extras.getString("ip").equals("")) {
            URL = extras.getString("ip");
        } else {
            URL = "http://webcam.aui.ma/axis-cgi/mjpg/video.cgi?resolution=CIF&amp";
        }

        if(extras != null){
            maxAcceleration = extras.getDouble("maxAcceleration");
            minAcceleration = extras.getDouble("minAcceleration");
            posXYZ = extras.getDoubleArray("posXYZ");
        }

        // Setup interval values
        double minIntervalValue = maxAcceleration/numberOfInterval;
        double maxIntervalValue = minAcceleration/numberOfInterval;

        ArrayList<Double> minInterval = new ArrayList<>();
        ArrayList<Double> maxInterval = new ArrayList<>();

        for (int i = 0; i < numberOfInterval; i++)
        {
            minInterval.add(i * minIntervalValue);
            maxInterval.add(i * maxIntervalValue);
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

    //On stop, unregister accelerometer listener
    public void onStop() {
        super.onStop();
        sensorManager.unregisterListener(accelerometerListener);
    }

    //Accelerometer listener, set the values
    public SensorEventListener accelerometerListener = new SensorEventListener() {
        public void onAccuracyChanged(Sensor sensor, int acc) { }

        public void onSensorChanged(SensorEvent event) {

            // Get the acceleration from sensors. Raw data
            linear_acceleration[0] = event.values[0] ;
            linear_acceleration[1] = event.values[1] ;
            linear_acceleration[2] = event.values[2] ;

            //Loop acceleration in XYZ
            for (int i = 0; i < linear_acceleration.length; i++){

                // Find the max value between the new position and the initial device position
                // Calculate the difference between them
                double m = Math.max(linear_acceleration[i], posXYZ[i]);
                double sendValue;

                if (m == linear_acceleration[i]){
                    sendValue = m - posXYZ[i];
                }
                else{
                    sendValue = m - linear_acceleration[i];
                }

                if(linear_acceleration[i] < posXYZ[i]){
                    if(i == 1){
                        Log.d("direction a", String.valueOf(sendValue));
                    }
                    else if(i == 0)
                    {
                        if (sendValue > maxAcceleration)
                        {
                            Log.d("direction w", String.valueOf(maxAcceleration));
                        }
                        else
                        {
                            Log.d("direction w", String.valueOf(sendValue));
                        }
                    }
                }
                else{
                    if(i == 1){
                        Log.d("direction d", String.valueOf(sendValue));
                    }
                    else if (i == 0)
                    {
                        if (sendValue > minAcceleration)
                        {
                            Log.d("direction s", String.valueOf(minAcceleration));
                        }
                        else
                        {
                            Log.d("direction s", String.valueOf(sendValue));
                        }
                    }
                }
            }
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
