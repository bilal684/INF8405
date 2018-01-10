package ca.polymtl.inf8405.testwebview;

/**
 * Created by Administrateur on 2017-04-13.
 */

public class Localisation {
    double latitude;
    double longitude;

    public Localisation(double latitude, double longitude){
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public void setLatitude(double latitude){
        this.latitude = latitude;
    }

    public void setLongitude(double longitude){
        this.longitude = longitude;
    }

    public double getLatitude(){
        return this.latitude;
    }

    public double getLongitude(){
        return this.longitude;
    }
}
