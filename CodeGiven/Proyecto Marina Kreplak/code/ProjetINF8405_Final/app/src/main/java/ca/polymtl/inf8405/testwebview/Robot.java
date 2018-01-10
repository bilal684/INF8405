package ca.polymtl.inf8405.testwebview;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Administrateur on 2017-04-13.
 */

public class Robot implements Parcelable {

    String nom;
    String ipAdress;
    Localisation localisation;
    String pseudo_proprietaire;
    //long taillePhoto = 0;

    public Robot(String nom, String ipAdress, Localisation localisation, String pseudo_proprietaire){
        this.nom = nom;
        this.ipAdress = ipAdress;
        this.localisation = localisation;
        this.pseudo_proprietaire = pseudo_proprietaire;
    }

    public Robot(Parcel in){
        String[] data = new String[3];
        double[] data2 = new double[2];
        //long[] data3 = new long[1];

        in.readStringArray(data);
        in.readDoubleArray(data2);
        //in.readLongArray(data3);

        // the order needs to be the same as in writeToParcel() method
        this.nom = data[0];
        this.ipAdress = data[1];
        this.pseudo_proprietaire = data[2];
        this.localisation = new Localisation(data2[0],data2[1]);
        //this.taillePhoto = data3[0];
    }

    public static final Parcelable.Creator CREATOR
            = new Parcelable.Creator() {
        public Robot createFromParcel(Parcel in) {
            return new Robot(in);
        }

        @Override
        public Robot[] newArray(int size) {
            return new Robot[size];
        }
    };



    public String getNom(){return nom;}

    public String getIpAdress(){return ipAdress;}

    public Localisation getLocalisation(){return localisation;}

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringArray(new String[] {this.nom, this.ipAdress, this.pseudo_proprietaire });
        dest.writeDoubleArray(new double[]{this.localisation.getLatitude(), this.localisation.getLongitude()});
        //dest.writeLongArray(new long[]{this.taillePhoto});
    }

    /*public void setTaillePhoto(long taillePhoto){
        this.taillePhoto = taillePhoto;
    }

    public long getTaillePhoto(){
        return taillePhoto;
    }*/

}


