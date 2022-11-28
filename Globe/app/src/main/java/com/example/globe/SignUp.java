package com.example.globe;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.hbb20.CountryCodePicker;

public class SignUp extends AppCompatActivity {

    private FirebaseAuth mAuth;
    EditText email, password, fullname, username, age;
    CountryCodePicker ccp;
    DatabaseReference database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_sign_up);

        mAuth = FirebaseAuth.getInstance();
        email = findViewById(R.id.inputEmail);
        password = findViewById(R.id.inputPassword);
        fullname = findViewById(R.id.fullName);
        username = findViewById(R.id.username);
        age = findViewById(R.id.age);
        ccp = findViewById(R.id.ccp);
        database = FirebaseDatabase.getInstance().getReference("Users");
    }

// ==================== Methods for validating user input ====================
    private Boolean validateName() {
        String val = fullname.getText().toString();
        String namePattern = "^[a-zA-Z ]+$";

        if (val.isEmpty()) {
            fullname.setError(getString(R.string.empty_field));
            return false;
        } else if (!val.matches(namePattern)) {
            fullname.setError(getString(R.string.only_letters));
            return false;
        } else {
            fullname.setError(null);
            return true;
        }
    }

    private Boolean validateUsername() {
        String val = username.getText().toString();
        String noWhiteSpace = "\\A\\w{4,20}\\z";

        if (val.isEmpty()) {
            username.setError(getString(R.string.empty_field));
            return false;
        } else if (val.length() >= 15) {
            username.setError(getString(R.string.long_username));
            return false;
        } else if (val.length() <= 3) {
            username.setError(getString(R.string.short_username));
            return false;
        } else if (!val.matches(noWhiteSpace)) {
            username.setError(getString(R.string.white_space));
            return false;
        } else {
            username.setError(null);
            return true;
        }
    }

    private Boolean validateEmail() {
        String val = email.getText().toString();
        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";

        if (val.isEmpty()) {
            email.setError(getString(R.string.empty_field));
            return false;
        } else if (!val.matches(emailPattern)) {
            email.setError(getString(R.string.invalid_email));
            return false;
        } else {
            email.setError(null);
            return true;
        }
    }

    private Boolean validatePassword() {
        String val = password.getText().toString();
        String passwordVal = "^" +
                "(?=.*[0-9])" +         //at least 1 digit
                "(?=.*[a-z])" +         //at least 1 lower case letter
                "(?=.*[A-Z])" +         //at least 1 upper case letter
                "(?=.*[a-zA-Z])" +      //any letter
                "(?=.*[@#$%^&+=!])" +    //at least 1 special character
                "(?=\\S+$)" +           //no white spaces
                ".{4,}" +               //at least 4 characters
                "$";

        if (val.isEmpty()) {
            password.setError(getString(R.string.empty_field));
            return false;
        } else if (!val.matches(passwordVal)) {
            password.setError(getString(R.string.weak_password));
            return false;
        } else {
            password.setError(null);
            return true;
        }
    }

    private Boolean validateAge() {
        String val = age.getText().toString();

        if (val.isEmpty()) {
            age.setError(getString(R.string.empty_field));
            return false;
        } else if (Integer.parseInt(val) <= 0 || Integer.parseInt(val) >= 100) {
            age.setError(getString(R.string.invalid_age));
            return false;
        } else {
            age.setError(null);
            return true;
        }
    }
// ========================================

    // Method for user sign up
    public void signup(View view) {
        // Validate user input
        if(!validateName() | !validatePassword()  | !validateEmail() | !validateUsername() | !validateAge()){
            return;
        }
        mAuth.createUserWithEmailAndPassword(email.getText().toString(), password.getText().toString())
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            // Get the country of user
                            String country = ccp.getSelectedCountryEnglishName();
                            // Create a User object with the data provided by the user
                            User user = new User(fullname.getText().toString(), username.getText().toString(), email.getText().toString(),
                                    age.getText().toString(), country, 0, 0, 0);
                            // Insert the new user to database
                            database.push().setValue(user);
                            // Go to next activity
                            Intent intent = new Intent(SignUp.this, MainMenu.class);
                            startActivity(intent);
                        } else {
                            showMessage(getString(R.string.error), task.getException().getLocalizedMessage());
                        }
                    }
                });
    }

    public void goLogin(View view) {
        onBackPressed(); // Go back to Login activity (animation working)
    }

    // Method used for showing a message
    void showMessage(String title, String message){
        new AlertDialog.Builder(this).setTitle(title).setMessage(message).setCancelable(true).show();
    }
}