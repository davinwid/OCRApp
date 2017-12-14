package edu.illinois.finalproject;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {

    private EditText emailSignUp, passwordSignUp,
            repeatPassword, userId, nameField;
    private Button registerButton;
    private ProgressDialog progressDialog;
    private FirebaseDatabase database;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_user);

        emailSignUp = (EditText) findViewById(R.id.emailSignUp);
        passwordSignUp = (EditText) findViewById(R.id.passwordSignUp);
        repeatPassword = (EditText) findViewById(R.id.repeatPassword);
        registerButton = (Button) findViewById(R.id.registerButton);
        userId = (EditText) findViewById(R.id.userId);
        nameField = (EditText) findViewById(R.id.nameField);
        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        database = FirebaseDatabase.getInstance();

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });
    }

    /**
     * Nescessary authentication before continuing the sign up process
     */
    private void registerUser() {
        // passwords are not the same
        final String userName = userId.getText().toString().trim();
        final String email = emailSignUp.getText().toString().trim();
        String password = passwordSignUp.getText().toString();
        String repeatPasswordText = repeatPassword.getText().toString();
        final String nameText = nameField.getText().toString();

        // fields are all filled and registration is ready
        if (email.equals("") || password.equals("") || nameText.equals("")
                || repeatPasswordText.equals("") || userName.equals("")) {
            makeToastText("Please make sure to fill in the descriptions before continuing!");
            return;
        }

        // password is empty
        if (!password.equals(repeatPasswordText)) {
            makeToastText("Please make sure that both password fields are the same");
            return;
        }
        addDataToDatabase(userName, email, password, nameText);
    }

    /**
     * Add the data of the user and register it through the Firebase as well as local data
     *
     * @param userName unique userID
     * @param email    email of the registered user
     * @param password user's password
     * @param nameText name set by the user.
     */
    private void addDataToDatabase(final String userName, final String email,
                                   String password, final String nameText) {
        // notify users whether the process is being completed
        progressDialog.setMessage("Registering user...");
        progressDialog.show();

        // contacting firebase to create the specified user
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        makeToastText("Successfully Registered!");
                        registerButton.setClickable(false);

                        // add new user info to database
                        DatabaseReference userRef = database.getReference("users");

                        // makes a new map based on the user profile
                        UserProfile newUser = new UserProfile(nameText, userName, email);

                        // adds the new user to database and local array
                        userRef.child(firebaseAuth.getCurrentUser().getUid()).setValue(newUser);

                        // signal the end of the process
                        progressDialog.hide();
                        startActivity(new Intent(RegisterActivity.this, LogInActivity.class));

                        if (!task.isSuccessful()) {
                            makeToastText("Registration failed, please try again!");
                        }
                    }
                });
    }


    /**
     * Make a toast for the given message
     * @param message value of string to make the toast with
     */
    public void makeToastText(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

}
