package ca.polymtl.inf8405.testwebview;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Administrateur on 2017-04-13.
 */

public class Utilisateur implements Parcelable {
    String pseudo;
    String email;
    String password;
    Localisation localisation;
    String rfidcode;
    //long taillePhoto = 0;

    //constructeur

    public String getPseudo(){return pseudo;}
    public String getPassword(){return password;}
    public String getEmail(){return this.email;}
    public String getRfidcode(){return this.rfidcode;}

    public Utilisateur(String pseudo, String email, String password, String rfidcode, Localisation localisation){
        this.pseudo = pseudo;
        this.email = email;
        this.password = password;
        this.rfidcode = rfidcode;
        this.localisation = localisation;
    }

    /*public void setTaillePhoto(long taillePhoto){
        this.taillePhoto = taillePhoto;
    }

    public long getTaillePhoto(){
        return taillePhoto;
    }*/

    public Utilisateur(String pseudo, String email, String password){
        this.pseudo = pseudo;
        this.email = email;
        this.password = password;
        this.localisation = new Localisation(0,0);
        this.rfidcode = "00000000";
    }

    public Utilisateur(Parcel in){
        String[] data = new String[4];
        double[] data2 = new double[2];

        in.readStringArray(data);
        in.readDoubleArray(data2);

        // the order needs to be the same as in writeToParcel() method
        this.pseudo = data[0];
        this.email = data[1];
        this.password = data[2];
        this.rfidcode = data[3];
        this.localisation = new Localisation(data2[0], data2[1]);
    }

    public Localisation getLocalisation(){
        return this.localisation;
    }


    public static final Parcelable.Creator CREATOR
            = new Parcelable.Creator() {
        public Utilisateur createFromParcel(Parcel in) {
            return new Utilisateur(in);
        }

        @Override
        public Utilisateur[] newArray(int size) {
            return new Utilisateur[size];
        }
    };


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringArray(new String[] {this.pseudo, this.email, this.password, this.rfidcode });
        dest.writeDoubleArray(new double[]{this.localisation.getLatitude(), this.localisation.getLongitude()});
    }

}