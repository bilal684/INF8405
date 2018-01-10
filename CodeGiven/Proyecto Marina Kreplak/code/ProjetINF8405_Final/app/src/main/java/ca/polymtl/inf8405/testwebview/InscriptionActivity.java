package ca.polymtl.inf8405.testwebview;

import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class InscriptionActivity extends AppCompatActivity {
    private EditText editEmail, editPseudo, editPassword;
    private Button buttonAdd;
    private Utilisateur utilisateur;
    private DatabaseHelper myDB;
    private FireBaseManager fm;
    private Boolean Loginaccepted;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inscription);
        buttonAdd = (Button) findViewById(R.id.button);
        editEmail = (EditText) findViewById(R.id.editText1);
        editPseudo = (EditText) findViewById(R.id.editText3);
        editPassword = (EditText) findViewById(R.id.editText4);
        fm = new FireBaseManager(null);
        myDB = new DatabaseHelper(this);
        Insert();
    }
    public void Insert (){
        buttonAdd.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                editEmail.setError(null);
                editPseudo.setError(null);
                editPassword.setError(null);
                String email = editEmail.getText().toString();
                String pseudo = editPseudo.getText().toString();
                String password = editPassword.getText().toString();

                boolean cancel = false;
                View focusView = null;
                if (TextUtils.isEmpty(email)) {
                    editEmail.setError(getString(R.string.error_field_required));
                    focusView = editEmail;
                    cancel = true;
                }
                if (TextUtils.isEmpty(pseudo)) {
                    editPseudo.setError(getString(R.string.error_field_required));
                    focusView = editPseudo;
                    cancel = true;
                }
                if (TextUtils.isEmpty(password)) {
                    editPassword.setError(getString(R.string.error_field_required));
                    focusView = editPassword;
                    cancel = true;
                }
                if (cancel) {
                    // There was an error; don't attempt login and focus the first
                    // form field with an error.
                    focusView.requestFocus();
                } else {
                    utilisateur = new Utilisateur(editPseudo.getText().toString(), editEmail.getText().toString(), editPassword.getText().toString());
                    Loginaccepted = false;

                    Cursor res = myDB.getAllData();
                    while (res != null && res.moveToNext()) {
                        if (res.getString(2).equals(pseudo)) {
                                Loginaccepted = true;
                        }
                    }

                    if((fm.isPseudoExisting(utilisateur.getPseudo()))|| Loginaccepted ) {
                        editEmail.setError("This account already exist! Please go the the sign up page to login");
                        focusView = editEmail;
                        focusView.requestFocus();
                    }
                    else {
                        Intent intent = new Intent(InscriptionActivity.this, PictureActivity.class);
                        intent.putExtra("UTILISATEUR", utilisateur);
                        startActivity(intent);
                        InscriptionActivity.this.finish();
                    }
                }
            }
        });
    }
}
