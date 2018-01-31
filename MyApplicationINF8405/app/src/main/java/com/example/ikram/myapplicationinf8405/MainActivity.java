package com.example.ikram.myapplicationinf8405;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {

    //Button connexion;
    private Button calibrateButton;
    private EditText adresseIPText;
    private String adresseIP ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        adresseIPText = this.findViewById(R.id.adresseIPText);
        adresseIP = adresseIPText.getText().toString();

        calibrateButton = findViewById(R.id.buttonCalibrate);
        calibrateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent robotActivity = new Intent(MainActivity.this, com.example.ikram.myapplicationinf8405.CalibrationActivity.class);
                startActivity(robotActivity);
            }
        });

        //WebView webView =new WebView(this);
        //webView.loadUrl("https://www.youtube.com/watch?v=8V-XBA2qAKY ");
        //connexion = ((Button) this.findViewById(R.id.connexion));
        //adresseIPText = ((EditText) this.findViewById(R.id.adressIP));
    }

//    public void Connexion (View view){
//            adresseIP = adresseIPText.getText().toString() ;
//            Intent i = new Intent(this, VideoActivity.class);
//            i.putExtra("ip", adresseIP) ;
//            startActivity(i);
//    }

//    public String getAdresseIP(){
//        return adresseIP;
//    }

}

