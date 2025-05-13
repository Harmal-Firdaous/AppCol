package com.example.appco;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class AjouterAnnonceActivity extends AppCompatActivity {
    private static final String TAG = "AjouterAnnonceActivity";
    private EditText editTitre, editDescription;
    private ImageView imagePreview, menuIcon, profileImage;
    private View menuContainer, profileContainer;
    private Button btnTakePhoto, btnSaveAnnonce, btnPickLocation;
    private TextView selectedLocation;
    private Uri photoUri;
    private String currentPhotoPath;
    private String base64Image;
    private double latitude = 0.0;
    private double longitude = 0.0;
    private String street = "";
    private String city = "";
    private String country = "";
    private String fullAddress = "";

    private FirebaseFirestore db;
    private CollectionReference annoncesRef;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_CAMERA_PERMISSION = 100;
    private static final int REQUEST_LOCATION_PICKER = 200;
    // Maximum image size for Base64 encoding (to keep Firestore document under 1MB)
    private static final int MAX_IMAGE_SIZE = 500; // pixels for width/height

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ajouter_annonce);
        Log.d(TAG, "onCreate: Starting AjouterAnnonceActivity");

        try {
            // Initialize Firebase
            FirebaseApp.initializeApp(this);
            db = FirebaseFirestore.getInstance();
            // Create a reference to the "annonces" collection
            annoncesRef = db.collection("annonces");

            // Find all views
            editTitre = findViewById(R.id.editTitre);
            editDescription = findViewById(R.id.editDescription);
            imagePreview = findViewById(R.id.imagePreview);
            btnTakePhoto = findViewById(R.id.btnTakePhoto);
            btnSaveAnnonce = findViewById(R.id.btnSaveAnnonce);
            menuIcon = findViewById(R.id.menuIcon);
            profileImage = findViewById(R.id.profileImage);
            menuContainer = findViewById(R.id.menuContainer);
            profileContainer = findViewById(R.id.profileContainer);
            btnPickLocation = findViewById(R.id.btnPickLocation);
            selectedLocation = findViewById(R.id.selectedLocation);

            // Set click listeners
            menuContainer.setOnClickListener(v -> showPopupMenu(menuIcon));
            profileContainer.setOnClickListener(v -> startActivity(new Intent(this, Profil.class)));

            // Request camera permission if needed
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
            }

            // Set button click listeners
            btnTakePhoto.setOnClickListener(v -> dispatchTakePictureIntent());
            btnSaveAnnonce.setOnClickListener(v -> saveAnnonce());

            // Set map button click listener
            btnPickLocation.setOnClickListener(v -> {
                Log.d(TAG, "Location button clicked");
                try {
                    Intent intent = new Intent(AjouterAnnonceActivity.this, MapPickerActivity.class);
                    startActivityForResult(intent, REQUEST_LOCATION_PICKER);
                } catch (Exception e) {
                    Log.e(TAG, "Error starting MapPickerActivity: " + e.getMessage());
                    Toast.makeText(AjouterAnnonceActivity.this,
                            "Erreur: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: " + e.getMessage(), e);
            Toast.makeText(this, "Erreur d'initialisation: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void showPopupMenu(View anchor) {
        try {
            PopupMenu popup = new PopupMenu(this, anchor);
            MenuInflater inflater = popup.getMenuInflater();
            inflater.inflate(R.menu.top_nav_menu, popup.getMenu());
            popup.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                if (id == R.id.menu_home) {
                    startActivity(new Intent(this, Home.class));
                    return true;
                } else if (id == R.id.menu_annonce) {
                    startActivity(new Intent(this, AnnoncesActivity.class));
                    return true;
                } else if (id == R.id.menu_logout) {
                    FirebaseAuth.getInstance().signOut();
                    startActivity(new Intent(this, Register.class));
                    return true;
                }
                return false;
            });
            popup.show();
        } catch (Exception e) {
            Log.e(TAG, "Error showing popup menu: " + e.getMessage());
        }
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = createImageFile();
            if (photoFile != null) {
                photoUri = FileProvider.getUriForFile(this,
                        getPackageName() + ".provider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            } else {
                Toast.makeText(this, "Erreur : Fichier photo introuvable", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Aucune application appareil photo disponible", Toast.LENGTH_SHORT).show();
        }
    }

    private File createImageFile() {
        try {
            String imageFileName = "JPEG_" + System.currentTimeMillis();
            File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            File image = File.createTempFile(imageFileName, ".jpg", storageDir);
            currentPhotoPath = image.getAbsolutePath();
            return image;
        } catch (IOException ex) {
            Log.e(TAG, "Error creating image file: " + ex.getMessage(), ex);
            Toast.makeText(this, "Erreur lors de la création du fichier", Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: requestCode=" + requestCode + ", resultCode=" + resultCode);

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            try {
                // Load the full-sized image
                Bitmap fullBitmap = BitmapFactory.decodeFile(currentPhotoPath);

                // Resize the image to a reasonable size before encoding
                Bitmap resizedBitmap = getResizedBitmap(fullBitmap, MAX_IMAGE_SIZE);

                // Convert to Base64
                base64Image = encodeToBase64(resizedBitmap, Bitmap.CompressFormat.JPEG, 75);

                // Show in preview
                imagePreview.setImageBitmap(resizedBitmap);
                Toast.makeText(this, "Photo prise avec succès", Toast.LENGTH_SHORT).show();

                // Clean up the full bitmap to save memory
                fullBitmap.recycle();
            } catch (Exception e) {
                Log.e(TAG, "Error processing image: " + e.getMessage(), e);
                Toast.makeText(this, "Erreur lors du traitement de l'image", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_LOCATION_PICKER && resultCode == RESULT_OK && data != null) {
            try {
                // Get coordinates
                latitude = data.getDoubleExtra("latitude", 0.0);
                longitude = data.getDoubleExtra("longitude", 0.0);

                // Get address components
                street = data.getStringExtra("street");
                city = data.getStringExtra("city");
                country = data.getStringExtra("country");
                fullAddress = data.getStringExtra("fullAddress");

                // Display the address information
                StringBuilder locationText = new StringBuilder();

                if (fullAddress != null && !fullAddress.isEmpty()) {
                    locationText.append(fullAddress);
                } else {
                    // Build address manually from components
                    if (street != null && !street.isEmpty()) {
                        locationText.append(street);
                    }

                    if (city != null && !city.isEmpty()) {
                        if (locationText.length() > 0) locationText.append(", ");
                        locationText.append(city);
                    }

                    if (country != null && !country.isEmpty()) {
                        if (locationText.length() > 0) locationText.append(", ");
                        locationText.append(country);
                    }

                    // If we still don't have an address, fall back to coordinates
                    if (locationText.length() == 0) {
                        locationText.append("Latitude : ").append(latitude)
                                .append(", Longitude : ").append(longitude);
                    }
                }

                selectedLocation.setText(locationText.toString());
                Log.d(TAG, "Location selected: " + locationText.toString());
            } catch (Exception e) {
                Log.e(TAG, "Error processing location result: " + e.getMessage(), e);
                Toast.makeText(this, "Erreur lors de la récupération de l'emplacement", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Helper method to resize bitmap to a maximum size
    private Bitmap getResizedBitmap(Bitmap bitmap, int maxSize) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        float bitmapRatio = (float) width / (float) height;

        if (bitmapRatio > 1) {
            // Width is greater, so scale width to maxSize
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            // Height is greater, so scale height to maxSize
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }

        return Bitmap.createScaledBitmap(bitmap, width, height, true);
    }

    // Helper method to encode bitmap to Base64
    private String encodeToBase64(Bitmap bitmap, Bitmap.CompressFormat format, int quality) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(format, quality, outputStream);
        return Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT);
    }

    private void saveAnnonce() {
        Log.d(TAG, "saveAnnonce: Starting to save annonce");

        // Get input values
        String titre = editTitre.getText().toString().trim();
        String description = editDescription.getText().toString().trim();

        // Validate inputs
        if (titre.isEmpty()) {
            Toast.makeText(this, "Veuillez entrer un titre", Toast.LENGTH_SHORT).show();
            return;
        }

        if (description.isEmpty()) {
            Toast.makeText(this, "Veuillez entrer une description", Toast.LENGTH_SHORT).show();
            return;
        }

        if (base64Image == null || base64Image.isEmpty()) {
            Toast.makeText(this, "Veuillez prendre une photo", Toast.LENGTH_SHORT).show();
            return;
        }

        if (latitude == 0.0 && longitude == 0.0) {
            Toast.makeText(this, "Veuillez choisir un emplacement", Toast.LENGTH_SHORT).show();
            return;
        }

        // Disable save button to prevent multiple submissions
        btnSaveAnnonce.setEnabled(false);
        Toast.makeText(this, "Enregistrement en cours...", Toast.LENGTH_SHORT).show();

        try {
            // Get current user
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            String userId = (user != null) ? user.getUid() : "anonymous";
            String userEmail = (user != null && user.getEmail() != null) ? user.getEmail() : "";
            String userName = (user != null && user.getDisplayName() != null) ? user.getDisplayName() :
                    (userEmail.isEmpty() ? "User-" + userId.substring(0, 5) : userEmail.split("@")[0]);

            // Create annonce object with all data
            Map<String, Object> annonce = new HashMap<>();
            annonce.put("titre", titre);
            annonce.put("description", description);
            annonce.put("imageBase64", base64Image);
            annonce.put("latitude", latitude);
            annonce.put("longitude", longitude);

            // Add address information
            annonce.put("street", street);
            annonce.put("city", city);
            annonce.put("country", country);
            annonce.put("fullAddress", fullAddress);

            annonce.put("timestamp", System.currentTimeMillis());
            annonce.put("ownerID", userId);
            annonce.put("ownerName", userName);
            annonce.put("ownerEmail", userEmail);

            // Add the annonce to Firestore
            annoncesRef.add(annonce)
                    .addOnSuccessListener(documentReference -> {
                        Log.d(TAG, "Annonce added with ID: " + documentReference.getId());
                        Toast.makeText(AjouterAnnonceActivity.this,
                                "Annonce ajoutée avec succès!",
                                Toast.LENGTH_SHORT).show();

                        // Return to the previous activity
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error adding annonce: " + e.getMessage(), e);
                        Toast.makeText(AjouterAnnonceActivity.this,
                                "Erreur: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();

                        // Re-enable save button on failure
                        btnSaveAnnonce.setEnabled(true);
                    });

        } catch (Exception e) {
            Log.e(TAG, "Exception in saveAnnonce: " + e.getMessage(), e);
            Toast.makeText(this, "Erreur: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            btnSaveAnnonce.setEnabled(true);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission appareil photo accordée", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permission appareil photo refusée", Toast.LENGTH_SHORT).show();
            }
        }
    }
}