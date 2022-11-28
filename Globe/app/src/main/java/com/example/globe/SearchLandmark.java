package com.example.globe;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SearchLandmark extends AppCompatActivity {

    TextView title, type, desc, phone, link, maps;
    CardView cardView;
    ProgressBar progressbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_search_landmark);
        title = findViewById(R.id.landmarkTitle);
        type = findViewById(R.id.landmarkType);
        desc = findViewById(R.id.landmarkDesc);
        phone = findViewById(R.id.landmarkPhone);
        link = findViewById(R.id.link);
        maps = findViewById(R.id.website);
        progressbar = findViewById(R.id.progressBar);
        cardView = findViewById(R.id.cardview);
        progressbar.setVisibility(View.VISIBLE);
        cardView.setVisibility(View.INVISIBLE);

        // The query text is the name of the landmark detected in the previous activity
        String q = getIntent().getStringExtra("query");
        // Convert the desired amount from one currency to another
        String url = "https://serpapi.com/search.json?q="+q+"&api_key=";

        // Make the request
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                String mMessage = e.getMessage();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String mMessage = response.body().string();
                SearchLandmark.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            // Get the result value from JSON
                            JSONObject obj = new JSONObject(mMessage);
                            JSONObject obj2 = obj.getJSONObject("knowledge_graph");
                            JSONObject obj3 = obj2.getJSONObject("local_map");
                            title.setText(obj2.getString("title"));
                            type.setText(obj2.getString("type"));
                            desc.setText(obj2.getString("description"));
                            phone.setText(obj2.getString("phone"));
                            String linkText = "Visit the <a href='"+obj2.getString("website")+"'>website</a>.";
                            link.setText(Html.fromHtml(linkText));
                            link.setMovementMethod(LinkMovementMethod.getInstance());
                            String linkTextMaps = "Visit the <a href='"+obj3.getString("link")+"'>maps</a>.";
                            maps.setText(Html.fromHtml(linkTextMaps));
                            maps.setMovementMethod(LinkMovementMethod.getInstance());
                            progressbar.setVisibility(View.INVISIBLE);
                            cardView.setVisibility(View.VISIBLE);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }
}