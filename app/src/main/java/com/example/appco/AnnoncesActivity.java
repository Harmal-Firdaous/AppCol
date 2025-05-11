package com.example.appco;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class AnnoncesActivity extends AppCompatActivity {

    private static final String TAG = "AnnoncesActivity";
    private RecyclerView recyclerView;
    private AnnonceAdapter adapter;
    private List<Annonce> annonceList;
    private FirebaseFirestore db;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_annonces);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize UI elements
        recyclerView = findViewById(R.id.recyclerViewAnnonces);
        progressBar = findViewById(R.id.progressBar);

        if (progressBar == null) {
            // If progressBar doesn't exist in your layout, add this to your layout XML
            Log.w(TAG, "ProgressBar not found in layout");
        }

        // Set up RecyclerView
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2)); // 2 columns

        // Initialize empty list and adapter
        annonceList = new ArrayList<>();
        adapter = new AnnonceAdapter(annonceList);
        recyclerView.setAdapter(adapter);

        // Load data from Firestore
        loadAnnonces();
    }

    private void loadAnnonces() {
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }

        db.collection("annonces")
                .get()
                .addOnCompleteListener(task -> {
                    if (progressBar != null) {
                        progressBar.setVisibility(View.GONE);
                    }

                    if (task.isSuccessful()) {
                        List<Annonce> newAnnonces = new ArrayList<>();

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            try {
                                // Create an Annonce object from the document
                                Annonce annonce = new Annonce();
                                annonce.setId(document.getId());
                                annonce.setTitre(document.getString("titre"));
                                annonce.setDescription(document.getString("description"));

                                // Handle possible annotation field
                                if (document.contains("annotation")) {
                                    annonce.setAnnotation(document.getString("annotation"));
                                }

                                // Handle different image formats
                                if (document.contains("imageUrl")) {
                                    annonce.setImageUrl(document.getString("imageUrl"));
                                } else if (document.contains("imageBase64")) {
                                    annonce.setImageBase64(document.getString("imageBase64"));
                                }

                                // Handle location data
                                if (document.contains("latitude") && document.contains("longitude")) {
                                    annonce.setLatitude(document.getDouble("latitude"));
                                    annonce.setLongitude(document.getDouble("longitude"));
                                }

                                // Handle owner information if available
                                if (document.contains("ownerName")) {
                                    annonce.setOwnerName(document.getString("ownerName"));
                                }

                                if (document.contains("ownerEmail")) {
                                    annonce.setOwnerEmail(document.getString("ownerEmail"));
                                }

                                if (document.contains("ownerProfileImage")) {
                                    annonce.setOwnerProfileImage(document.getString("ownerProfileImage"));
                                }

                                // Handle rating if available
                                if (document.contains("rating")) {
                                    Double ratingDouble = document.getDouble("rating");
                                    if (ratingDouble != null) {
                                        annonce.setRating(ratingDouble.floatValue());
                                    }
                                } else {
                                    // Default rating if not available
                                    annonce.setRating(0f);
                                }

                                // Handle timestamp if available
                                if (document.contains("timestamp")) {
                                    annonce.setTimestamp(document.getLong("timestamp"));
                                }

                                // Add to our list
                                newAnnonces.add(annonce);
                                Log.d(TAG, "Added annonce: " + annonce.getTitre());

                            } catch (Exception e) {
                                Log.e(TAG, "Error parsing document " + document.getId(), e);
                            }
                        }

                        // Update the adapter with new data
                        annonceList.clear();
                        annonceList.addAll(newAnnonces);
                        adapter.notifyDataSetChanged();

                        Log.d(TAG, "Loaded " + annonceList.size() + " annonces from Firestore");

                    } else {
                        Log.e(TAG, "Error getting documents: ", task.getException());
                        Toast.makeText(AnnoncesActivity.this,
                                "Erreur lors du chargement des annonces",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}