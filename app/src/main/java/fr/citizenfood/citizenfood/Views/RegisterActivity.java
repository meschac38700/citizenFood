package fr.citizenfood.citizenfood.Views;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import fr.citizenfood.citizenfood.Model.User;
import fr.citizenfood.citizenfood.R;

public class RegisterActivity extends BaseActivity implements View.OnClickListener {

    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;

    private static final String TAG = "RegisterActivity";


    private EditText usernameField;
    private EditText passwordField;
    private Button registerButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //init firebase components
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        //init views
        usernameField = findViewById(R.id.registerUsername);
        passwordField = findViewById(R.id.registerPassword);
        registerButton = findViewById(R.id.registerButton);

        //onClickEffect
        effetAuClic(registerButton);


        registerButton.setOnClickListener(this);
    }


    private void signUp() {
        Log.d(TAG, "signUp");
        if (!validateForm()) {
            return;
        }

        showProgressDialog();
        String email = usernameField.getText().toString();
        String password = passwordField.getText().toString();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "createUser:onComplete:" + task.isSuccessful());
                        hideProgressDialog();

                        if (task.isSuccessful()) {
                            onAuthSuccess(task.getResult().getUser());
                            Toast.makeText(RegisterActivity.this, "Sign Up suceed",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(RegisterActivity.this, "Sign Up Failed",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
    private void onAuthSuccess(FirebaseUser user) {
        String username = usernameFromEmail(user.getEmail());

        // Write new user
        writeNewUser(user.getUid(), username, user.getEmail());

        // Go to MainActivity
        startActivity(new Intent(RegisterActivity.this, MainActivity.class));
        finish();
    }

    private String usernameFromEmail(String email) {
        if (email.contains("@")) {
            return email.split("@")[0];
        } else {
            return email;
        }
    }

    // [START basic_write]
    private void writeNewUser(String userId, String name, String email) {
        User user = new User(name, email);

        mDatabase.child("users").child(userId).setValue(user);
    }
    // [END basic_write]

    private boolean validateForm() {
        boolean result = true;
        if (TextUtils.isEmpty(usernameField.getText().toString())) {
            usernameField.setError("Required");
            result = false;
        } else {
            usernameField.setError(null);
        }

        if (TextUtils.isEmpty(passwordField.getText().toString())) {
            passwordField.setError("Required");
            result = false;
        } else {
            passwordField.setError(null);
        }

        return result;
    }

    @Override
    public void onClick(View v) {

        signUp();
    }


}
