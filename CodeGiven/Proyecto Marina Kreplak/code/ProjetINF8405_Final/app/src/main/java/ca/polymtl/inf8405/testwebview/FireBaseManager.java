package ca.polymtl.inf8405.testwebview;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by YannleChevoir on 13/04/2017.
 */

public class FireBaseManager {

    private Robot robotControle;

    private FirebaseDatabase database;
    private DatabaseReference leftMotorSpeed;
    private DatabaseReference rightMotorSpeed;
    private DatabaseReference marcheArriere;
    private final DatabaseReference clignotant;
    private final DatabaseReference utilisateurs_database;
    Map<String, Object> utilisateurs_local;

    private final DatabaseReference robots_database;
    Map<String, Object> robots_local;


    private StorageReference storageReference = FirebaseStorage.getInstance().getReference();


    public FireBaseManager(Robot robotControle){

        if(robotControle == null) this.robotControle = new Robot("arduino","",new Localisation(0,0),"");
        else this.robotControle = robotControle;

        this.database = FirebaseDatabase.getInstance();
        this.leftMotorSpeed = database.getReference(this.robotControle.getNom()+"/leftMotorSpeed");
        this.rightMotorSpeed = database.getReference(this.robotControle.getNom()+"/rightMotorSpeed");
        this.marcheArriere = database.getReference(this.robotControle.getNom()+"/marcheArriere");
        this.clignotant = database.getReference(this.robotControle.getNom()+"/clignotant");
        this.utilisateurs_database = database.getReference("utilisateurs");
        this.utilisateurs_local = new HashMap<>();

        this.robots_database = database.getReference("robots");
        this.robots_local = new HashMap<>();

        getUtilisateurs_database();
        getRobots_database();

        leftMotorSpeed.setValue(0);
        rightMotorSpeed.setValue(0);
        clignotant.setValue(0);

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }


    public void setMotorsSpeed(double rapportDeVitesse, double leftSpeed, double rightSpeed, JeuActivity jeuActivity){

        //Mise à jour de la barre de vitesse
        double tmp = rapportDeVitesse*100;
        jeuActivity.getSeekBarVitesse().setProgress((int)tmp);

        double left = leftSpeed*rapportDeVitesse*155;
        if(left!=0){
            left+=100;
        }

        double right = rightSpeed*rapportDeVitesse*155;
        if(right!=0){
            right+=100;
        }

        leftMotorSpeed.setValue((int)left);
        rightMotorSpeed.setValue((int)right);
    }

    public void setRear(boolean rear){
        marcheArriere.setValue(rear);
    }

    public void setClignotantValue(int clignotantState){
        clignotant.setValue(clignotantState);
    }

    public void addUtilisateur(Utilisateur utilisateur){

        utilisateurs_local.put(utilisateur.getPseudo(), utilisateur);

        updateUtilisateurs_database();

    }

    public void addRobot(Robot robot){

        robots_local.put(robot.getNom(), robot);

        updateRobots_database();

    }

    public Utilisateur isUtilisateurExisting(final String pseudo, final String passwd){

        Utilisateur ret = null;

        if(utilisateurs_local.containsKey(pseudo)){
            Utilisateur user = (Utilisateur) utilisateurs_local.get(pseudo);
            if((user.getPassword()).equals(passwd)){
                ret = new Utilisateur(
                        pseudo,
                        user.getEmail(),
                        passwd,
                        user.getRfidcode(),
                        user.getLocalisation()
                );
            }
        }

        return ret;
    }

    public boolean isPseudoExisting(final String pseudo){

        boolean ret = false;

        if(utilisateurs_local.containsKey(pseudo)){
            ret = true;
        }

        return ret;
    }

    public Map<String, Object> getAllUtilisateursOnFirebase(){return utilisateurs_local;}
    public Map<String, Object> getAllRobotsOnFirebase(){return robots_local;}

    private void getUtilisateurs_database(){

        utilisateurs_database.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot child : dataSnapshot.getChildren()){
                    Utilisateur tmp = new Utilisateur(
                            (String)((Map)child.getValue()).get("pseudo"),
                            (String)((Map)child.getValue()).get("email"),
                            (String)((Map)child.getValue()).get("password"),
                            (String)((Map)child.getValue()).get("rfidcode"),
                            new Localisation(
                                    Double.parseDouble((((Map)(((Map)child.getValue()).get("localisation"))).get("latitude")).toString()),
                                    Double.parseDouble((((Map)(((Map)child.getValue()).get("localisation"))).get("longitude")).toString())
                            )
                    );
                    utilisateurs_local.put(tmp.getPseudo(), tmp);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void updateUtilisateurs_database(){

        utilisateurs_database.updateChildren(utilisateurs_local);

    }

    private void getRobots_database(){

        robots_database.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot child : dataSnapshot.getChildren()){
                    Robot tmp = new Robot(
                            (String)((Map)child.getValue()).get("nom"),
                            (String)((Map)child.getValue()).get("ipAdress"),
                            new Localisation(
                                    Double.parseDouble((((Map)(((Map)child.getValue()).get("localisation"))).get("latitude")).toString()),
                                    Double.parseDouble((((Map)(((Map)child.getValue()).get("localisation"))).get("longitude")).toString())
                            ),
                            (String)((Map)child.getValue()).get("pseudo_proprietaire")
                    );
                    robots_local.put(tmp.getNom(), tmp);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void updateRobots_database(){

        robots_database.updateChildren(robots_local);

    }


    public void setUtilisateurPhoto(Utilisateur utilisateur, byte[] byteArray){
        StorageReference photo = storageReference.child("Utilisateurs/"+utilisateur.getPseudo()+".png");
        photo.putBytes(byteArray);
    }

    public File getUtilisateurPhoto(Utilisateur utilisateur){
        StorageReference photo = storageReference.child("Utilisateurs/"+utilisateur.getPseudo()+".png");
        File localFile = null;
        try {
            localFile = File.createTempFile("images", "png");
            photo.getFile(localFile);
            Thread.sleep(1500); //Temporistaion pour récupérer la photo (dans un autre thread)
        } catch (Exception e) {
            e.printStackTrace();
        }
        return localFile;
    }

    public void setRobotPhoto(Robot robot, byte[] byteArray){
        StorageReference photo = storageReference.child("Robots/"+robot.getNom()+".png");
        photo.putBytes(byteArray);
    }

    public File getRobotPhoto(Robot robot){
        StorageReference photo = storageReference.child("Robots/"+robot.getNom()+".png");
        File localFile = null;
        try {
            localFile = File.createTempFile("images", "png");
            photo.getFile(localFile);
            Thread.sleep(1500); //Temporistaion pour récupérer la photo (dans un autre thread)
        } catch (Exception e) {
            e.printStackTrace();
        }
        return localFile;
    }

}
