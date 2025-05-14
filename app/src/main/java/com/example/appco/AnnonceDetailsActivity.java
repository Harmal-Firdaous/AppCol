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
    private TextView nomProprietaire, titreAnnonce, descriptionAnnonce, annotationAnnonce, locationText;
    private RatingBar ratingAnnonce;
    private Button buttonContacter;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_annonce_details);

        db = FirebaseFirestore.getInstance();

        // UI binding
        imageAnnonce = findViewById(R.id.imageAnnonce);
        imageProprietaire = findViewById(R.id.imageProprietaire);
        nomProprietaire = findViewById(R.id.nomProprietaire);
        titreAnnonce = findViewById(R.id.titreAnnonce);
        descriptionAnnonce = findViewById(R.id.descriptionAnnonce);
        annotationAnnonce = findViewById(R.id.annotationAnnonce);
        locationText = findViewById(R.id.locationText);
        ratingAnnonce = findViewById(R.id.ratingAnnonce);
        buttonContacter = findViewById(R.id.buttonContacter);

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
                .addOnSuccessListener(this::displayAnnonceDetails)
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Erreur lors du chargement de l'annonce", e);
                    Toast.makeText(this, "Erreur lors du chargement", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void displayAnnonceDetails(@NonNull DocumentSnapshot document) {
        try {
            String titre = document.getString("titre");
            String description = document.getString("description");
            String annotation = document.getString("annotation");
            String imageUrl = document.getString("imageUrl");
            String imageBase64 = document.getString("imageBase64");

            // Propriétaire
            String ownerName = document.getString("ownerName");
            String ownerEmail = document.getString("ownerEmail");
            String ownerId = document.getString("ownerId");
            String ownerProfileImage = document.getString("ownerProfileImage");

            // Location information
            String country = document.getString("country");
            String city = document.getString("city");
            String street = document.getString("street");

            // Format location text
            StringBuilder locationBuilder = new StringBuilder();
            if (country != null && !country.trim().isEmpty()) {
                locationBuilder.append(country);
            }
            if (city != null && !city.trim().isEmpty()) {
                if (locationBuilder.length() > 0) locationBuilder.append(" ");
                locationBuilder.append(city);
            }
            if (street != null && !street.trim().isEmpty()) {
                if (locationBuilder.length() > 0) locationBuilder.append(" ");
                locationBuilder.append(street);
            }

            String locationString = locationBuilder.length() > 0 ?
                    locationBuilder.toString() : "Localisation non spécifiée";
            locationText.setText(locationString);

            // Log the owner name to help debug
            Log.d(TAG, "Owner name: " + ownerName);
            Log.d(TAG, "Location: " + locationString);

            // Titre et description
            titreAnnonce.setText(titre != null ? titre : "Sans titre");
            descriptionAnnonce.setText(description != null ? description : "Pas de description");

            // Annotation
            if (annotation != null && !annotation.isEmpty()) {
                annotationAnnonce.setText(annotation);
                annotationAnnonce.setVisibility(android.view.View.VISIBLE);
            } else {
                annotationAnnonce.setVisibility(android.view.View.GONE);
            }

            // Nom du propriétaire with improved empty check
            if (ownerName != null && !ownerName.trim().isEmpty()) {
                nomProprietaire.setText(ownerName);
            } else {
                nomProprietaire.setText("Propriétaire anonyme");
            }

            // Image principale
            if (imageUrl != null && !imageUrl.isEmpty()) {
                Glide.with(this).load(imageUrl)
                        .placeholder(R.drawable.ic_launcher_background)
                        .into(imageAnnonce);
            } else if (imageBase64 != null && !imageBase64.isEmpty()) {
                try {
                    byte[] decodedString = Base64.decode(imageBase64, Base64.DEFAULT);
                    Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                    imageAnnonce.setImageBitmap(decodedBitmap);
                } catch (Exception e) {
                    Log.e(TAG, "Erreur de décodage image", e);
                    imageAnnonce.setImageResource(R.drawable.ic_launcher_background);
                }
            } else {
                imageAnnonce.setImageResource(R.drawable.ic_launcher_background);
            }

            // Image du propriétaire
            if (ownerProfileImage != null && !ownerProfileImage.isEmpty()) {
                Glide.with(this).load(ownerProfileImage)
                        .placeholder(R.drawable.ic_profile)
                        .into(imageProprietaire);
            } else {
                imageProprietaire.setImageResource(R.drawable.ic_profile);
            }

            // Étoiles
            float rating = 0f;
            if (document.contains("rating")) {
                Double ratingDouble = document.getDouble("rating");
                if (ratingDouble != null) {
                    rating = ratingDouble.floatValue();
                }
            }
            ratingAnnonce.setRating(rating);

            // Listener bouton contacter
            buttonContacter.setOnClickListener(v -> {
                Intent messagerieIntent = new Intent(this, Message.class);
                messagerieIntent.putExtra("receiverId", ownerId != null ? ownerId : "");
                messagerieIntent.putExtra("receiverName", ownerName != null ? ownerName : "Propriétaire");
                messagerieIntent.putExtra("receiverEmail", ownerEmail != null ? ownerEmail : "");
                messagerieIntent.putExtra("annonceId", document.getId());
                messagerieIntent.putExtra("annonceTitre", titre != null ? titre : "Sans titre");
                startActivity(messagerieIntent);
            });

        } catch (Exception e) {
            Log.e(TAG, "Erreur lors de l'affichage des détails", e);
            Toast.makeText(this, "Erreur d'affichage", Toast.LENGTH_SHORT).show();
        }
    }
}