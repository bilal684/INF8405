package com.example.ikram.myapplicationinf8405;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {


    Button connexion;
    EditText adresseIP ;
    String adresseIp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ///WebView webView =new WebView(this);
        //webView.loadUrl("https://www.youtube.com/watch?v=8V-XBA2qAKY ");
        connexion = ((Button) this.findViewById(R.id.connexion));
        adresseIP=((EditText)this.findViewById(R.id.adressIP)) ;



    }


    public void Connexion (View view){

            adresseIp =adresseIP.getText().toString() ;
            Intent i = new Intent(this, VideoActivity.class);
            i.putExtra("ip",adresseIp) ;
            startActivity(i);


    }

}

