package com.example.globe;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class CurrencyConverter extends AppCompatActivity {

    List<String> keysList; // List containing the currencies (used for the spinners)
    Spinner fromCurrency;
    Spinner toCurrency;
    TextView textView;
    EditText conValue; // Value to be converted
    Button btnConvert;
    DatabaseReference database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_currency_converter);
        fromCurrency = findViewById(R.id.from_spinner);
        toCurrency = findViewById(R.id.to_spinner);
        conValue = findViewById(R.id.con_value);
        btnConvert = findViewById(R.id.button);
        textView = findViewById(R.id.con_value2);
        database = FirebaseDatabase.getInstance().getReference("Users");

        try {
            // Get the currencies from Exchange Rates Data API (https://apilayer.com/marketplace/exchangerates_data-api)
            loadConvTypes();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // When user clicks the convert button
        btnConvert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!conValue.getText().toString().isEmpty())
                {
                    String toCurr = toCurrency.getSelectedItem().toString();
                    String fromCurr = fromCurrency.getSelectedItem().toString();
                    double value = Double.valueOf(conValue.getText().toString());
                    try {
                        if (toCurr.equals(fromCurr)) { // The content of the spinners is the same
                            Toast.makeText(CurrencyConverter.this, getString(R.string.same_currency), Toast.LENGTH_SHORT).show();
                        } else {
                            // Call function to convert the value to appropriate currency
                            convertCurrency(fromCurr, toCurr, value);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(CurrencyConverter.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
                else
                {
                    // If no value is entered
                    Toast.makeText(CurrencyConverter.this, getString(R.string.enter_currency_value), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // Function that loads the currencies from API
    public void loadConvTypes() throws IOException {
        // Get the desired currencies (only the currency codes)
        String url = "https://api.apilayer.com/exchangerates_data/latest?symbols=ALL%2CBGN%2CCNY%2CHRK%2CCZK%2CDKK%2CGBP%2CEUR%2CINR%2CJPY%2CPLN%2CRON%2CRUB%2CSEK%2CUAH%2CUSD%2CAUD%2CCAD%2CCHF%2CAED&base=EUR";
        // This list contains the names and symbols of the currencies
        List<String> description = Arrays.asList(new String[]{"(Albania Lek, Lek)", "(Bulgaria Lev, лв)", "(China Yuan Renminbi, ¥)", "(Croatia Kuna, kn)", "(Czech Republic Koruna, Kč)", "(Denmark Krone, kr)", "(United Kingdom Pound, £)"
                , "(Euro Member Countries, €)", "(India Rupee, ₹)", "(Japan Yen, ¥)", "(Poland Zloty, zł)", "(Romania Leu, lei)", "(Russia Ruble, ₽)", "(Sweden Krona, kr)", "(Ukraine Hryvnia, ₴)", "(United States Dollar, $)", "(Australia Dollar, $)", "(Canada Dollar, $)", "(Switzerland Franc, CHF)", "(United Arab Emirates Dirham, د.إ)"});

        // Make the request
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(url)
                .addHeader("apikey", "")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                String mMessage = e.getMessage();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String mMessage = response.body().string();
                CurrencyConverter.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            // Get the rates values from JSON
                            JSONObject obj = new JSONObject(mMessage);
                            JSONObject  b = obj.getJSONObject("rates");

                            Iterator keysToCopyIterator = b.keys();
                            keysList = new ArrayList<>();

                            int i = 0;
                            while(keysToCopyIterator.hasNext()) {
                                // Add them to the list
                                String key = (String) keysToCopyIterator.next();
                                // List format: currency code (name, symbol)
                                keysList.add(key + "  " + description.get(i));
                                i++;
                            }

                            // Set the contents of the spinners according to list contents
                            ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item, keysList );
                            fromCurrency.setAdapter(spinnerArrayAdapter);
                            toCurrency.setAdapter(spinnerArrayAdapter);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }

    // Function that converts the currencies from API
    public void convertCurrency(String fromCurr, String toCurr, Double value) throws IOException {
        // Convert the desired amount from one currency to another
        String url = "https://api.apilayer.com/exchangerates_data/convert?to="+ toCurr.substring(0,3) +"&from="+ fromCurr.substring(0,3) +"&amount="+value;

        // Make the request
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(url)
                .addHeader("apikey", "")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                String mMessage = e.getMessage();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String mMessage = response.body().string();
                CurrencyConverter.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            // Get the result value from JSON
                            JSONObject obj = new JSONObject(mMessage);
                            Double output = obj.getDouble("result");
                            // Round result to two decimals
                            textView.setText(String.format("%.2f", output));

                            // Update the number of conversions in database
                            database.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    // For every user (child)
                                    for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                                        User user = dataSnapshot.getValue(User.class);
                                        // For currently connected user
                                        if (user.getEmail().equals(FirebaseAuth.getInstance().getCurrentUser().getEmail())){
                                            // Incrementing the total number of conversions for the current user
                                            int sum = user.getNumOfConversions() + 1;
                                            String key = dataSnapshot.getKey();
                                            // Updating value
                                            database.child(key).child("numOfConversions").setValue(sum);

                                            // Archive every conversion in database
                                            FirebaseDatabase database1 =  FirebaseDatabase.getInstance();
                                            DatabaseReference mRef =  database1.getReference().child("Statistics").child("Conversion").push();
                                            mRef.child("currencyFrom").setValue(fromCurr);
                                            mRef.child("currencyTo").setValue(toCurr);
                                            mRef.child("originalValue").setValue(value.toString());
                                            mRef.child("convertedValue").setValue(textView.getText().toString());
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
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }

    // Method used for showing a message
    void showMessage(String title, String message){
        new AlertDialog.Builder(this).setTitle(title).setMessage(message).setCancelable(true).show();
    }
}