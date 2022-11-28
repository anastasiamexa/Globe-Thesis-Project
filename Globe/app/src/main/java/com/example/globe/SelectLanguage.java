package com.example.globe;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

public class SelectLanguage extends AppCompatActivity {

    RecyclerView recyclerView;
    DatabaseReference database;
    MyAdapter myAdapter;
    ArrayList<Language> list;
    ArrayList<String> matches;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_select_language);
        // Initialize the recyclerView and get the database reference
        recyclerView = findViewById(R.id.languageList);
        database = FirebaseDatabase.getInstance().getReference("Languages");
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        matches = new ArrayList<>();
        list = new ArrayList<>(); // This list contains all the data from database

        // Set the adapter
        myAdapter = new MyAdapter(this, list);
        recyclerView.setAdapter(myAdapter);

        // Show auto detection language option only when choosing language to translate from
        String btn = getIntent().getStringExtra("btnPressed");
        if (btn.equals("lanFrom")){
            Language auto = new Language("Auto", "auto.png", "AUTO","auto");
            list.add(auto);
        }

        // Read data from database
        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // For every language (child)
                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                    // Create a Language object and set it's attributes, according to database values
                    Language lan = dataSnapshot.getValue(Language.class);
                    list.add(lan);
                }
                myAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showMessage(getString(R.string.error), error.getMessage());
            }
        });
    }

    // Search
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.my_menu, menu);
        // Get reference to menu item
        MenuItem searchItem = menu.findItem(R.id.action_search);
        // Get reference to search menu
        SearchView searchView = (SearchView) searchItem.getActionView();
        // Set keyboard icon
        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            // There is no use for this method because the search happens on real time
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Filter the results according to the newText
                myAdapter.getFilter().filter(newText);
                return false;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    // Method used for showing a message
    void showMessage(String title, String message){
        new AlertDialog.Builder(this).setTitle(title).setMessage(message).setCancelable(true).show();
    }
}