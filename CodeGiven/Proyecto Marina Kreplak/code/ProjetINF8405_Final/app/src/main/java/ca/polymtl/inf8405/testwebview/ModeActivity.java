package ca.polymtl.inf8405.testwebview;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.File;
import java.security.Security;

public class ModeActivity extends AppCompatActivity {

    private Robot robotControle;
    private Utilisateur utilisateur;
    private FireBaseManager fireBaseManager;
    private DatabaseHelper myDB;

    Button buttonJouer, buttonPlatoon, buttonSecurite, buttonLineFollower;
    ImageView imageViewRobot, imageViewUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mode);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        robotControle = (Robot) extras.get("ROBOT");
        utilisateur = (Utilisateur) extras.get("UTILISATEUR");
        myDB = new DatabaseHelper(this);

        //utilisateur



        buttonJouer = (Button)findViewById(R.id.button2);
        buttonPlatoon = (Button)findViewById(R.id.button6);
        buttonSecurite = (Button)findViewById(R.id.button7);
        buttonLineFollower = (Button)findViewById(R.id.button5);

        imageViewRobot = (ImageView)findViewById(R.id.imageView);
        imageViewUser = (ImageView)findViewById(R.id.imageView2);

        fireBaseManager = new FireBaseManager(robotControle);
        //imageView.setImageBitmap(BitmapFactory.decodeFile(fireBaseManager.getUtilisateurPhoto(utilisateur).getAbsolutePath()));
        Cursor res = myDB.getAllData();


        byte[] byteArray = null;
        while (res != null && res.moveToNext()) {
            if (res.getString(2).equals(utilisateur.getPseudo())) {
                byteArray = res.getBlob(4);
            }
        }
        if (byteArray != null){
            Bitmap bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
            imageViewUser.setImageBitmap(bitmap);
        }
        else
            imageViewUser.setImageBitmap(BitmapFactory.decodeFile(fireBaseManager.getUtilisateurPhoto(utilisateur).getAbsolutePath()));

        imageViewRobot.setImageBitmap(BitmapFactory.decodeFile(fireBaseManager.getRobotPhoto(robotControle).getAbsolutePath()));


        StartJeuActivity();

        StartSecurityActivity();

        StartPlatoonActivity();

        StartLineFollowerActivity();

    }

    public void StartJeuActivity (){
        buttonJouer.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ModeActivity.this, JeuActivity.class);
                intent.putExtra("ROBOT", robotControle);
                startActivity(intent);
            }
        });
    }

   public void StartPlatoonActivity (){
        buttonPlatoon.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ModeActivity.this, PlatoonActivity.class);
                intent.putExtra("ROBOT", robotControle);
                startActivity(intent);
            }
        });
    }

    public void StartSecurityActivity (){
        buttonSecurite.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ModeActivity.this, SecurityActivity.class);
                intent.putExtra("ROBOT", robotControle);
                startActivity(intent);
            }
        });
    }

    public void StartLineFollowerActivity (){
        buttonLineFollower.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ModeActivity.this, LineFollowerActivity.class);
                intent.putExtra("ROBOT", robotControle);
                startActivity(intent);
            }
        });
    }
}
