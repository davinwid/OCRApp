package edu.illinois.finalproject;

import android.app.ProgressDialog;
import android.content.Context;
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

public class RegisterActivity extends AppCompatActivity {

    private EditText emailSignUp, passwordSignUp, repeatPassword, userId, nameField;
    private Button registerButton;
    private ProgressDialog progressDialog;
    private FirebaseAuth firebaseAuth;
    private final Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_user);

        initializeScreenComponents();
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
        String name = userId.getText().toString().trim();
        String email = emailSignUp.getText().toString().trim();
        String password = passwordSignUp.getText().toString();
        String repeatPasswordText = repeatPassword.getText().toString();
        String nameText = nameField.getText().toString();

        // fields are all filled and registration is ready
        if (email.equals("") || password.equals("") || nameText.equals("")
                || repeatPasswordText.equals("") || name.equals("")) {
            makeToastText("Please make sure to fill in the descriptions before continuing!");
            return;
        }

        // password is empty
        if (!password.equals(repeatPasswordText)) {
            makeToastText("Please make sure that both password fields are the same");
            return;
        }


        // notify users whether the process is being completed
        progressDialog.setMessage("Registering user...");
        progressDialog.show();

        // contacting firebase to create the specified user
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        makeToastText("Successfully Registered!");
                        if (!task.isSuccessful()){
                            makeToastText("Registration failed, please try again!");
                        }
                    }
                });
        progressDialog.hide();

        /*Intent mainActivity = new Intent(context, LogInActivity.class);
        context.startActivity(mainActivity);*/
        //TODO add email, user id and name to the firebase.


    }

    /**
     * Make sure that every screen component is associated with the right values.
     */
    private void initializeScreenComponents() {
        emailSignUp = (EditText) findViewById(R.id.emailSignUp);
        passwordSignUp = (EditText) findViewById(R.id.passwordSignUp);
        repeatPassword = (EditText) findViewById(R.id.repeatPassword);
        registerButton = (Button) findViewById(R.id.registerButton);
        userId = (EditText) findViewById(R.id.userId);
        nameField = (EditText) findViewById(R.id.nameField);
        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
    }

    /**
     * Make a toast for the given message
     *
     * @param message value of string to make the toast with
     */
    public void makeToastText(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }


}
