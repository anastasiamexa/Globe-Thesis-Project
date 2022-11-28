package com.example.globe;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;

public class UserProfile extends AppCompatActivity {

    Button logout;
    TextView translations, landmarks, conversions, username;
    TextInputLayout fullName, email, country, age;
    DatabaseReference database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_user_profile);

        logout = findViewById(R.id.logout);
        translations = findViewById(R.id.translation_label);
        landmarks = findViewById(R.id.landmark_label);
        conversions = findViewById(R.id.conversion_label);
        username = findViewById(R.id.username_field);
        fullName = findViewById(R.id.full_name_profile);
        email = findViewById(R.id.email_profile);
        country = findViewById(R.id.country_profile);
        age = findViewById(R.id.age_profile);
        database = FirebaseDatabase.getInstance().getReference("Users");

        // Create and show a custom loading dialog
        LoadingDialog loadingDialog = new LoadingDialog(UserProfile.this);
        loadingDialog.startLoadingDialog();
        // After 3 seconds, dismiss the loading dialog
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                loadingDialog.dismissDialog();
            }
        },3000);

        // Load user's info from database
        database.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // For every user (child)
                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                    User user = dataSnapshot.getValue(User.class);
                    // For currently connected user
                    if (user.getEmail().equals(FirebaseAuth.getInstance().getCurrentUser().getEmail())){
                        translations.setText(String.valueOf(user.getNumOfTranslations()));
                        landmarks.setText(String.valueOf(user.getNumOfLandmarks()));
                        conversions.setText(String.valueOf(user.getNumOfConversions()));
                        username.setText(user.getUsername());
                        fullName.getEditText().setText(user.getFullName());
                        email.getEditText().setText(user.getEmail());
                        country.getEditText().setText(user.getCountry());
                        age.getEditText().setText(user.getAge());
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showMessage(getString(R.string.error), error.getMessage());
            }
        });
    }

    // Method for user to logout
    public void logout(View view){
        FirebaseAuth.getInstance().signOut();
        Intent intentLogout = new Intent(UserProfile.this, Authentication.class);
        startActivity(intentLogout);
    }

    // Method used for showing a message
    void showMessage(String title, String message){
        new AlertDialog.Builder(this).setTitle(title).setMessage(message).setCancelable(true).show();
    }
}