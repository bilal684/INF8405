package ca.polymtl.inf8405.testwebview;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.BatteryManager;
import android.support.v4.app.ActivityCompat;
        import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
        import android.os.Bundle;
        import android.view.View;
        import android.widget.Button;
        import android.Manifest;

import java.text.SimpleDateFormat;
import java.util.Date;


public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_TO_GEOLOCALISE = 1;

    private Button buttonSignIn, buttonSignUp;
    private boolean firstStart = true;

    private float batterieAuDebut;

    private String dateDebut;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        buttonSignIn = (Button) findViewById(R.id.button3);
        buttonSignUp = (Button) findViewById(R.id.button4);

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.VIBRATE)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.VIBRATE }, PERMISSION_TO_GEOLOCALISE);
        }


        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = registerReceiver(null, ifilter);
        batterieAuDebut = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) / (float)batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        SignIn();
        SignUp();

        Date d = new Date();
        SimpleDateFormat f = new SimpleDateFormat("h:mm a");
        dateDebut = f.format(d);


    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!firstStart)showBatteryStatus();
        firstStart = false;
    }

    public void SignIn (){
        buttonSignIn.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, InscriptionActivity.class);
                startActivity(intent);
            }
        });
    }

    public void SignUp (){
        buttonSignUp.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, ConnectActivity.class);
                startActivity(intent);
            }
        });
    }

    private void showBatteryStatus(){
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = registerReceiver(null, ifilter);
        float batterieALaFin = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) / (float)batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        Date d = new Date();
        SimpleDateFormat f = new SimpleDateFormat("h:mm a");
        String dateCourante = f.format(d);

        //DialogBox pour afficher la perte de batterie depuis le début.
        AlertDialog.Builder boite = new AlertDialog.Builder(MainActivity.this);
        boite.setTitle("Utilisation de la batterie");
        boite.setMessage("Vous êtes de retour au menu :)\nPour information, vous avez commencé avec "+(int)(batterieAuDebut*100)+"% de batterie (à "+dateDebut+").\nOn en est à "+(int)(batterieALaFin*100)+"% (à "+dateCourante+").\nSoit une utilisation de "+((int)(batterieAuDebut*100)-(int)(batterieALaFin*100))+"% pour cette session d'utilisation.");
        boite.setPositiveButton("Ok", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        
                    }
                }
        );
        boite.show();
    }
}