package com.example.appco;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class AnnonceDetailsActivity extends AppCompatActivity {

    private static final String TAG = "AnnonceDetailsActivity";
    private ImageView imageAnnonce, imageProprietaire;
    private TextView nomProprietaire, titreAnnonce, descriptionAnnonce, annotationAnnonce;
    private RatingBar ratingAnnonce;
    private Button buttonContacter;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_annonce_details);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize UI elements
        imageAnnonce = findViewById(R.id.imageAnnonce);
        imageProprietaire = findViewById(R.id.imageProprietaire);
        nomProprietaire = findViewById(R.id.nomProprietaire);
        titreAnnonce = findViewById(R.id.titreAnnonce);
        descriptionAnnonce = findViewById(R.id.descriptionAnnonce);
        annotationAnnonce = findViewById(R.id.annotationAnnonce);
        ratingAnnonce = findViewById(R.id.ratingAnnonce);
        buttonContacter = findViewById(R.id.buttonContacter);

        // Get annonce ID from intent
        String annonceId = getIntent().getStringExtra("annonceId");

        if (annonceId != null) {
            loadAnnonceDetails(annonceId);
        } else {
            Toast.makeText(this, "ID de l'annonce non trouvé", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void loadAnnonceDetails(String annonceId) {
        db.collection("annonces").document(annonceId).get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        displayAnnonceDetails(document);
                    } else {
                        Toast.makeText(this, "Annonce non trouvée", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading annonce details", e);
                    Toast.makeText(this, "Erreur lors du chargement", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void displayAnnonceDetails(@NonNull DocumentSnapshot document) {
        try {
            // Get data from document
            String titre = document.getString("titre");
            String description = document.getString("description");
            String annotation = document.getString("annotation");
            String imageUrl = document.getString("imageUrl");
            String imageBase64 = document.getString("imageBase64");
            String ownerName = document.getString("ownerName");
            String ownerEmail = document.getString("ownerEmail");
            String ownerId = document.getString("ownerId");
            String ownerProfileImage = document.getString("ownerProfileImage");

            // Handle rating if available
            float rating = 0f;
            if (document.contains("rating")) {
                Double ratingDouble = document.getDouble("rating");
                if (ratingDouble != null) {
                    rating = ratingDouble.floatValue();
                }
            }

            // Set text values
            titreAnnonce.setText(titre);
            descriptionAnnonce.setText(description);

            // Set annotation if available
            if (annotation != null && !annotation.isEmpty()) {
                annotationAnnonce.setText(annotation);
                annotationAnnonce.setVisibility(android.view.View.VISIBLE);
            } else {
                annotationAnnonce.setVisibility(android.view.View.GONE);
            }

            // Set rating
            ratingAnnonce.setRating(rating);

            // Set owner name if available
            if (ownerName != null && !ownerName.isEmpty()) {
                nomProprietaire.setText(ownerName);
            } else {
                nomProprietaire.setText("Propriétaire anonyme");
            }

            // Load annonce image
            if (imageUrl != null && !imageUrl.isEmpty()) {
                // Use Glide to load image from URL
                Glide.with(this)
                        .load(imageUrl)
                        .placeholder(R.drawable.ic_launcher_background)
                        .into(imageAnnonce);
            } else if (imageBase64 != null && !imageBase64.isEmpty()) {
                // Decode Base64 image
                try {
                    byte[] decodedString = Base64.decode(imageBase64, Base64.DEFAULT);
                    Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                    imageAnnonce.setImageBitmap(decodedBitmap);
                } catch (Exception e) {
                    Log.e(TAG, "Error decoding image", e);
                    imageAnnonce.setImageResource(R.drawable.ic_launcher_background);
                }
            } else {
                // Use default image if no image available
                imageAnnonce.setImageResource(R.drawable.ic_launcher_background);
            }

            // Load owner profile image if available
            if (ownerProfileImage != null && !ownerProfileImage.isEmpty()) {
                Glide.with(this)
                        .load(ownerProfileImage)
                        .placeholder(R.drawable.ic_profile)
                        .into(imageProprietaire);
            } else {
                imageProprietaire.setImageResource(R.drawable.ic_profile);
            }

            // Save final values for use in button click listener
            final String finalOwnerEmail = (ownerEmail != null && !ownerEmail.isEmpty()) ?
                    ownerEmail : "";
            final String finalOwnerId = (ownerId != null) ? ownerId : "";
            final String finalOwnerName = (ownerName != null && !ownerName.isEmpty()) ?
                    ownerName : "Propriétaire anonyme";

            // Set up contact button to navigate to Messagerie instead of email
            buttonContacter.setOnClickListener(v -> {
                Intent messagerieIntent = new Intent(AnnonceDetailsActivity.this, Messagerie.class);
                // Pass relevant information to the Messagerie activity
                messagerieIntent.putExtra("receiverId", finalOwnerId);
                messagerieIntent.putExtra("receiverName", finalOwnerName);
                messagerieIntent.putExtra("receiverEmail", finalOwnerEmail);
                messagerieIntent.putExtra("annonceId", document.getId());
                messagerieIntent.putExtra("annonceTitre", titre);
                startActivity(messagerieIntent);
            });

        } catch (Exception e) {
            Log.e(TAG, "Error displaying annonce details", e);
            Toast.makeText(this, "Erreur lors de l'affichage des détails", Toast.LENGTH_SHORT).show();
        }
    }
}