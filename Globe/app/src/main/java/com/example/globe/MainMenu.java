package com.example.globe;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class MainMenu extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    DrawerLayout drawerLayout;
    NavigationView navigationView;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main_menu);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

        navigationView.bringToFront();
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);

        navigationView.setCheckedItem(R.id.nav_home);
    }

    @Override
    protected void onResume() {
        super.onResume();
        navigationView.setCheckedItem(R.id.nav_home);
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
        else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_home:
                break;
            case R.id.nav_translation:
                //START INTENT
                Intent intentTranslation = new Intent(MainMenu.this, Translation.class);
                startActivity(intentTranslation);
                break;
            case R.id.nav_landmarks:
                //START INTENT
                Intent intentLandmarks = new Intent(MainMenu.this, Landmarks.class);
                startActivity(intentLandmarks);
                break;
            case R.id.nav_currency:
                //START INTENT
                Intent intentCurrencyConverter = new Intent(MainMenu.this, CurrencyConverter.class);
                startActivity(intentCurrencyConverter);
                break;
            case R.id.nav_profile:
                //START INTENT
                Intent intentUserProfile = new Intent(MainMenu.this, UserProfile.class);
                startActivity(intentUserProfile);
                break;
            case R.id.nav_logout:
                //START INTENT
                FirebaseAuth.getInstance().signOut();
                Intent intentLogout = new Intent(MainMenu.this, Authentication.class);
                startActivity(intentLogout);
                break;
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    public void goTranslation(View view) {
        Intent intentTranslation = new Intent(MainMenu.this, Translation.class);
        startActivity(intentTranslation);
    }

    public void goLandmarks(View view) {
        Intent intentLandmarks = new Intent(MainMenu.this, Landmarks.class);
        startActivity(intentLandmarks);
    }

    public void goCurrencyConverter(View view) {
        Intent intentCurrencyConverter = new Intent(MainMenu.this, CurrencyConverter.class);
        startActivity(intentCurrencyConverter);
    }

    // Method used for showing a message
    void showMessage(String title, String message){
        new AlertDialog.Builder(this).setTitle(title).setMessage(message).setCancelable(true).show();
    }
}