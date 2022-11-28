package com.example.globe;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> implements Filterable {

    Context context;
    ArrayList<Language> list;
    ArrayList<Language> listFull; // List copy used for search

    // Constructor
    public MyAdapter(Context context, ArrayList<Language> list) {
        this.context = context;
        this.list = list;
        // New list containing the same items, for independent usage
        listFull = new ArrayList<>(this.list);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item, parent, false);
        return new MyViewHolder(v);
    }

    // Method to bind the view in Card view with data in Language class
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        // Here the list is full, so fill up the listFull
        if (listFull.size() == 0 || listFull.size() == 1){
            listFull = new ArrayList<>(this.list);
        }
        // Add attribute values from model class to appropriate view in Card view
        Language lan = list.get(position);
        holder.title.setText(lan.getTitle());
        // Get language cover images from Firebase Storage
        StorageReference imageRef2 = holder.storageReference.child(lan.getIcon());
        imageRef2.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            // Use Glide to get the image through url
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(context).load(uri).into(holder.cover);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                e.getLocalizedMessage();
            }
        });

        holder.mainLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // When user clicks on a card view, go to the next activity and pass the language information
                String btn = ((Activity)v.getContext()).getIntent().getStringExtra("btnPressed");
                Intent intent = new Intent();
                intent.putExtra("language", lan.getTitle());
                intent.putExtra("languageTag", lan.getTag());
                intent.putExtra("btnPressed", btn);
                ((Activity)v.getContext()).setResult(Activity.RESULT_OK, intent);
                ((Activity)v.getContext()).finish();
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }


    // Class to create references of the views in Card view
    public static class MyViewHolder extends RecyclerView.ViewHolder{

        TextView title;
        ImageView cover;
        FirebaseStorage firebaseStorage;
        StorageReference storageReference;
        ConstraintLayout mainLayout;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            firebaseStorage = FirebaseStorage.getInstance();
            storageReference = firebaseStorage.getReference();
            cover = itemView.findViewById(R.id.cover);
            title = itemView.findViewById(R.id.title);
            mainLayout = itemView.findViewById(R.id.mainLayout);
        }
    }

    @Override
    public Filter getFilter() {
        return listFilter;
    }

    // Return the filter results
    private Filter listFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            ArrayList<Object> filteredList = new ArrayList<>();
            // constraint variable is the input of the search bar
            // If constraint is null, return all the results
            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(listFull);
            } else { // Create a string with the filter pattern (case insensitive)
                String filterPattern = constraint.toString().toLowerCase().trim();
                // Iterate all languages from list and find those that match the filter pattern
                for (Object item : listFull) {
                    if (((Language) item).getTitle().toLowerCase().startsWith(filterPattern)){
                        filteredList.add(item); // add it to the filtered list
                    }
                }
            }
            FilterResults results = new FilterResults();
            results.values = filteredList;
            // Returns the filtered list to the publishResults method
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            // Delete all items from list and add only those that match the filter pattern
            list.clear();
            list.addAll((ArrayList) results.values);
            notifyDataSetChanged();
        }
    };
}
