package ca.polymtl.inf8405.testwebview;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.BatteryManager;
import android.os.Vibrator;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SecurityActivity extends AppCompatActivity {

    private Switch mySwitch;
    private Robot robotControle;

    private FirebaseDatabase database;
    private DatabaseReference presenceDetectee;
    private DatabaseReference modeSecurite;
    private DatabaseReference modeLineFollower;
    private DatabaseReference modePlatoon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_security);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        robotControle = (Robot) extras.get("ROBOT");

        database = FirebaseDatabase.getInstance();
        presenceDetectee = database.getReference(robotControle.getNom() + "/securite/presenceDetectee");
        modeSecurite = database.getReference(robotControle.getNom() + "/securite/modeSecurite");
        modeLineFollower = database.getReference(robotControle.getNom() + "/modeLineFollower");
        modePlatoon = database.getReference(robotControle.getNom() + "/modePlatoon");


        presenceDetectee.setValue(false);

        mySwitch = (Switch) findViewById(R.id.switch1);

        //set the switch to OFF
        mySwitch.setChecked(false);
        modePlatoon.setValue(false);
        modeLineFollower.setValue(false);

        //showDetectedPresence();

        //attach a listener to check for changes in state
        mySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                modeSecurite.setValue(isChecked);

            }
        });

        modeSecurite.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mySwitch.setChecked((boolean) dataSnapshot.getValue());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        presenceDetectee.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if ((boolean) dataSnapshot.getValue()){
                    showDetectedPresence();

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void showDetectedPresence(){

        //DialogBox to show that a presence has been detected
        AlertDialog.Builder box = new AlertDialog.Builder(SecurityActivity.this);
        box.setTitle("Attention!")
                .setMessage("Le robot a détecté une présence");

        box.setPositiveButton("Ok", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        presenceDetectee.setValue(false);
                        Intent intent = new Intent(SecurityActivity.this, JeuActivity.class);
                        intent.putExtra("ROBOT", robotControle);
                        startActivity(intent);
                    }
                }
        );
        box.show();
        //J'ai un problème de permissions, je la demande mais Android ne la demande pas...
        //Vibrator vib=(Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
        //vib.vibrate(10000);
    }

}