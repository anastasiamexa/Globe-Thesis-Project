package com.example.globe;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.nl.languageid.LanguageIdentification;
import com.google.mlkit.nl.languageid.LanguageIdentifier;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class Translation extends AppCompatActivity {

    TextView raw; // The editText which contains the raw input to be translated
    TextView translated; // The editText which contains the translated output
    String from = ""; // Language to be translated from
    String to = ""; // Language to be translated to
    Button btnfrom, btnto; // Buttons to choose the languages
    public static final int REQUEST_CODE = 111;
    public static final int REQUEST_CODE_MICROPHONE = 123;
    MyTTS tts; // Text to Speech variable
    ArrayList<String> matches = new ArrayList<>(); // List used for voice recognition results
    DatabaseReference database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_translation);
        raw = findViewById(R.id.editTextTextMultiLine);
        translated = findViewById(R.id.editTextTextMultiLine2);
        btnfrom = findViewById(R.id.lanFrom);
        btnto = findViewById(R.id.lanTo);
        tts = new MyTTS(this);
        database = FirebaseDatabase.getInstance().getReference("Users");

        // Coming from Select Image activity
        raw.setText(getIntent().getStringExtra("image text"));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Return here from previous activity (Adapter of recyclerView)
        // Get the selected language
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE) {
            // Language to be translated from
            if (data.getStringExtra("btnPressed").equals("lanFrom")) {
                // Change the text of the button and get the language tag
                btnfrom.setText(data.getStringExtra("language"));
                from = data.getStringExtra("languageTag");
            }
            // Language to be translated to
            else if (data.getStringExtra("btnPressed").equals("lanTo")) {
                // Change the text of the button and get the language tag
                btnto.setText(data.getStringExtra("language"));
                to = data.getStringExtra("languageTag");
            }
        }
        // Results of voice recognition
        if (requestCode == REQUEST_CODE_MICROPHONE && resultCode == RESULT_OK) {
            // Print the best match of voice recognition
            matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            raw.setText(matches.get(0));
        }
    }

    // Method is called when translate button is pressed
    public void translate(View view){
        // Check if editText is null
        if (raw.getText() == null || raw.getText().toString().length() == 0){
            showMessage(getString(R.string.error), getString(R.string.nothing_to_translate));
            // Check if user hasn't selected languages
        } else if (btnfrom.getText().equals("Select") || btnto.getText().equals("Select")) {
            showMessage(getString(R.string.error), getString(R.string.select_language));
        } else {
            // Check if language from and to are different
            if (from.equals(to)){
                showMessage(getString(R.string.error), getString(R.string.same_language));
            } else {
                // For language identification
                if (btnfrom.getText().equals("Auto")) {
                    identify(); // Identify the language
                } else {
                    makeTranslation(); // Translate
                }
            }
        }
    }

    // Do translation
    public void makeTranslation() {
        // Get the text to be translated
        String s = raw.getText().toString();
        // Create a translator
        TranslatorOptions options =
                new TranslatorOptions.Builder()
                        // Language identification
                        .setSourceLanguage(TranslateLanguage.fromLanguageTag(from))
                        .setTargetLanguage(TranslateLanguage.fromLanguageTag(to))
                        .build();
        final Translator translator = com.google.mlkit.nl.translate.Translation.getClient(options);

        // Download the necessary models
        DownloadConditions conditions = new DownloadConditions.Builder()
                .requireWifi()
                .build();
        translator.downloadModelIfNeeded(conditions)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        // Model downloaded successfully. Okay to start translating
                        translator.translate(s)
                                .addOnSuccessListener(new OnSuccessListener<String>() {
                                    @Override
                                    public void onSuccess(String s) {
                                        // Translation successful
                                        translated.setText(s);
                                        translator.close();
                                        // Update the number of translations in database
                                        database.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                // For every user (child)
                                                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                                                    User user = dataSnapshot.getValue(User.class);
                                                    // For currently connected user
                                                    if (user.getEmail().equals(FirebaseAuth.getInstance().getCurrentUser().getEmail())){
                                                        // Incrementing the total number of translations for the current user
                                                        int sum = user.getNumOfTranslations() + 1;
                                                        String key = dataSnapshot.getKey();
                                                        // Updating value
                                                        database.child(key).child("numOfTranslations").setValue(sum);

                                                        // Archive every translation in database
                                                        FirebaseDatabase database1 =  FirebaseDatabase.getInstance();
                                                        DatabaseReference mRef =  database1.getReference().child("Statistics").child("Translation").push();
                                                        mRef.child("languageFrom").setValue(from);
                                                        mRef.child("languageTo").setValue(to);
                                                        mRef.child("originalText").setValue(raw.getText().toString());
                                                        mRef.child("translatedText").setValue(translated.getText().toString());
                                                        mRef.child("username").setValue(user.getUsername());
                                                        mRef.child("timestamp").setValue(new SimpleDateFormat("dd/MM/yyyy hh:mm:ss").format(new Date()));
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
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // Error
                                        Toast.makeText(getApplicationContext(), getString(R.string.failed_translation),Toast.LENGTH_LONG).show();
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Model couldn’t be downloaded or other internal error
                        Toast.makeText(getApplicationContext(), getString(R.string.download_failed),Toast.LENGTH_LONG).show();
                    }
                });
    }

    // Method for language identification
    public void identify() {
        LanguageIdentifier languageIdentifier = LanguageIdentification.getClient();
        // Text to be identified
        languageIdentifier.identifyLanguage(raw.getText().toString())
                .addOnSuccessListener(new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(@NonNull String languageCode) {
                        if (languageCode.equals("und")) { // If language can't be identified
                            Toast.makeText(getApplicationContext(), getString(R.string.unidentified_language),Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(getApplicationContext(), getString(R.string.language) + languageCode,Toast.LENGTH_LONG).show();
                            // Set the language to be translated from
                            from = languageCode;
                            makeTranslation(); // Translate
                            from = "auto";
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Model could not be loaded or other internal error
                        Toast.makeText(getApplicationContext(), getString(R.string.failed_to_identify_language),Toast.LENGTH_LONG).show();
                    }
                });
    }

    // Method called when the language buttons are pressed
    public void language(View view) {
        // Start new activity to select language
        Intent intent = new Intent(this, SelectLanguage.class);
        intent.putExtra("btnPressed", view.getResources().getResourceEntryName(view.getId()));
        startActivityForResult(intent, REQUEST_CODE);
    }

    // Swap button to swap the languages
    public void swap(View view) {
        if (btnfrom.getText().equals("Auto")) {
            showMessage(getString(R.string.error), getString(R.string.swap_languages));
        } else {
            // Swap the language button text
            String text1 = btnfrom.getText().toString();
            String text2 = btnto.getText().toString();
            btnfrom.setText(text2);
            btnto.setText(text1);
            // Swap the language tag
            String tag1 = from;
            String tag2 = to;
            from = tag2;
            to = tag1;
        }
    }

    public void speak(View view) {
        if (view.getResources().getResourceEntryName(view.getId()).equals("speakFrom")) {
            tts.speak(raw.getText().toString(), from);
        } else if (view.getResources().getResourceEntryName(view.getId()).equals("speakTo")) {
            tts.speak(translated.getText().toString(), to);
        }
    }

    // Voice recognition method
    public void talk(View view) {
        // Request for microphone permission
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_CODE_MICROPHONE);
        } else {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, from); // Choose the language
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say something");
            startActivityForResult(intent,REQUEST_CODE_MICROPHONE);
        }
    }

    // Start new intent to load image, whose text will be translated, via camera or gallery
    public void selectImage(View view){
        Intent intent = new Intent(this, SelectImage.class);
        startActivity(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_MICROPHONE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, from); // Choose the language
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say something");
            startActivityForResult(intent,REQUEST_CODE_MICROPHONE);
        }
    }

    // Method used for showing a message
    void showMessage(String title, String message){
        new AlertDialog.Builder(this).setTitle(title).setMessage(message).setCancelable(true).show();
    }
}