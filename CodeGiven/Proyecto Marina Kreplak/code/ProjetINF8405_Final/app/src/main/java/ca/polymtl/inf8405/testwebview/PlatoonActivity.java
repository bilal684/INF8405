package ca.polymtl.inf8405.testwebview;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebView;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class PlatoonActivity extends AppCompatActivity {

    private Switch mySwitch;
    private Robot robotControle;
    private FirebaseDatabase database;
    private DatabaseReference modePlatoon;
    private DatabaseReference modeSecurite;
    private DatabaseReference modeLineFollower;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_platoon);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        robotControle = (Robot) extras.get("ROBOT");

        database = FirebaseDatabase.getInstance();
        modePlatoon = database.getReference(robotControle.getNom() + "/modePlatoon");
        modeSecurite = database.getReference(robotControle.getNom() + "/securite/modeSecurite");
        modeLineFollower = database.getReference(robotControle.getNom() + "/modeLineFollower");

        WebView myWebView = (WebView) findViewById(R.id.webview2);
        myWebView.loadUrl("http://"+robotControle.getIpAdress()+":8081/");

        mySwitch = (Switch) findViewById(R.id.switch2);
        //set the switch to OFF
        mySwitch.setChecked(false);
        modeLineFollower.setValue(false);
        modeSecurite.setValue(false);

        //attach a listener to check for changes in state
        mySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                modePlatoon.setValue(isChecked);

            }
        });

    }
}
