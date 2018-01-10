package ca.polymtl.inf8405.testwebview;

import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;

public class PictureActivity extends AppCompatActivity {
    DatabaseHelper myDB;
    Utilisateur utilisateur;
    private FireBaseManager fm;
    Intent i;
    Bundle extras;
    private static final int CAM_REQUEST=1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture);
        myDB=new DatabaseHelper(this);
        fm = new FireBaseManager(null);
        i = getIntent();
        extras = i.getExtras();
        utilisateur = (Utilisateur)extras.get("UTILISATEUR");
        Intent cameraintent=new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraintent, CAM_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == CAM_REQUEST ) {
            if (resultCode == RESULT_OK) {
                Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                thumbnail.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] byteArray = stream.toByteArray();

                boolean isInserted = myDB.insertData(utilisateur.getEmail(),
                        utilisateur.getPseudo(),
                        utilisateur.getPassword(),
                        byteArray);

                fm.addUtilisateur(utilisateur);
                fm.setUtilisateurPhoto(utilisateur, byteArray);

                if (isInserted) {
                    Toast.makeText(PictureActivity.this, "REGISTERED WITH SUCCESS", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(PictureActivity.this, LocationActivity.class);
                    intent.putExtra("UTILISATEUR", utilisateur);
                    startActivity(intent);
                    this.finish();
                } else
                    Toast.makeText(PictureActivity.this, "PROBLEM WITH REGISTERING", Toast.LENGTH_LONG).show();
            }
            else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(PictureActivity.this, "You need to take a profile picture", Toast.LENGTH_LONG).show();
                this.finish();
            }
        }
    }
}
