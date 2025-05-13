package com.example.appco;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapPickerActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "MapPickerActivity";
    private GoogleMap mMap;
    private Marker selectedMarker;
    private Button btnConfirmLocation;
    private Button btnSearch;
    private EditText etSearch;
    private TextView tvAddressInfo;
    private FloatingActionButton fabMyLocation;
    private LatLng selectedLatLng;

    // Location services
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private boolean locationPermissionGranted = false;
    private static final int DEFAULT_ZOOM = 15;

    // Default location (Tunisia)
    private static final LatLng DEFAULT_LOCATION = new LatLng(36.8065, 10.1815);

    // Address components
    private String selectedStreet = "";
    private String selectedCity = "";
    private String selectedCountry = "";
    private String fullAddress = "";

    // Permission request launcher
    private final ActivityResultLauncher<String[]> locationPermissionRequest =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                Boolean fineLocationGranted = result.getOrDefault(
                        Manifest.permission.ACCESS_FINE_LOCATION, false);
                Boolean coarseLocationGranted = result.getOrDefault(
                        Manifest.permission.ACCESS_COARSE_LOCATION, false);

                if (fineLocationGranted != null && fineLocationGranted) {
                    // Precise location access granted
                    locationPermissionGranted = true;
                    updateLocationUI();
                    getCurrentLocation();
                } else if (coarseLocationGranted != null && coarseLocationGranted) {
                    // Only approximate location access granted
                    locationPermissionGranted = true;
                    updateLocationUI();
                    getCurrentLocation();
                } else {
                    // No location access granted
                    locationPermissionGranted = false;
                    Toast.makeText(this, "Permission de localisation refusée", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_picker);

        Log.d(TAG, "onCreate: Starting MapPickerActivity");

        try {
            // Initialize UI elements
            btnConfirmLocation = findViewById(R.id.btnConfirmLocation);
            tvAddressInfo = findViewById(R.id.tvAddressInfo);
            btnSearch = findViewById(R.id.btnSearch);
            etSearch = findViewById(R.id.etSearch);
            fabMyLocation = findViewById(R.id.fabMyLocation);

            Log.d(TAG, "Button found: " + (btnConfirmLocation != null));
            Log.d(TAG, "TextView found: " + (tvAddressInfo != null));
            Log.d(TAG, "Search button found: " + (btnSearch != null));
            Log.d(TAG, "Search EditText found: " + (etSearch != null));
            Log.d(TAG, "FAB found: " + (fabMyLocation != null));

            // Initialize location components
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

            // Initialize location callback
            locationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(@NonNull LocationResult locationResult) {
                    if (locationResult.getLastLocation() != null) {
                        Location location = locationResult.getLastLocation();
                        LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                        updateSelectedLocation(currentLatLng);
                    }
                }
            };

            // Obtain the SupportMapFragment and get notified when the map is ready to be used
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);

            if (mapFragment != null) {
                Log.d(TAG, "Getting map async");
                mapFragment.getMapAsync(this);
            } else {
                Log.e(TAG, "Map fragment is null");
                Toast.makeText(this, "Error: Map fragment not found", Toast.LENGTH_SHORT).show();
                finish();
            }

            // Setup search button click
            btnSearch.setOnClickListener(view -> {
                String query = etSearch.getText().toString().trim();
                if (!query.isEmpty()) {
                    searchLocation(query);
                } else {
                    Toast.makeText(MapPickerActivity.this,
                            "Veuillez entrer un pays ou une ville",
                            Toast.LENGTH_SHORT).show();
                }
            });

            // Setup search on enter key pressed
            etSearch.setOnEditorActionListener((v, actionId, event) -> {
                if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                        (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    String query = etSearch.getText().toString().trim();
                    if (!query.isEmpty()) {
                        searchLocation(query);
                        return true;
                    } else {
                        Toast.makeText(MapPickerActivity.this,
                                "Veuillez entrer un pays ou une ville",
                                Toast.LENGTH_SHORT).show();
                    }
                }
                return false;
            });

            // Setup my location FAB
            fabMyLocation.setOnClickListener(view -> {
                if (checkLocationPermissions()) {
                    getCurrentLocation();
                }
            });

            // Setup confirm location button
            btnConfirmLocation.setOnClickListener(view -> {
                Log.d(TAG, "Confirm button clicked");
                if (selectedLatLng != null) {
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("latitude", selectedLatLng.latitude);
                    resultIntent.putExtra("longitude", selectedLatLng.longitude);
                    resultIntent.putExtra("street", selectedStreet);
                    resultIntent.putExtra("city", selectedCity);
                    resultIntent.putExtra("country", selectedCountry);
                    resultIntent.putExtra("fullAddress", fullAddress);
                    setResult(RESULT_OK, resultIntent);
                    finish();
                } else {
                    Toast.makeText(MapPickerActivity.this,
                            "Veuillez sélectionner un emplacement",
                            Toast.LENGTH_SHORT).show();
                }
            });

            // Check location permissions
            checkLocationPermissions();

        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: " + e.getMessage());
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
            finish();
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        Log.d(TAG, "onMapReady called");
        try {
            mMap = googleMap;

            // Setup UI settings
            updateLocationUI();

            // Move camera to default location if we don't have permission
            if (!locationPermissionGranted) {
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(DEFAULT_LOCATION, 10));
                getAddressFromLocation(DEFAULT_LOCATION);
            } else {
                // Get current location if we have permission
                getCurrentLocation();
            }

            // Setup map click listener
            mMap.setOnMapClickListener(latLng -> {
                Log.d(TAG, "Map clicked at: " + latLng.latitude + ", " + latLng.longitude);
                updateSelectedLocation(latLng);
            });
        } catch (Exception e) {
            Log.e(TAG, "Error in onMapReady: " + e.getMessage());
            Toast.makeText(this, "Error loading map: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    /**
     * Check if location permissions are granted, request if needed
     * @return true if permissions are granted
     */
    private boolean checkLocationPermissions() {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this.getApplicationContext(),
                        Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true;
            return true;
        } else {
            // Request permissions
            locationPermissionRequest.launch(new String[] {
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
            return false;
        }
    }

    /**
     * Update the map UI based on permission status
     */
    @SuppressLint("MissingPermission")
    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }

        try {
            if (locationPermissionGranted) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(false); // We use our own FAB
            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
            }
        } catch (SecurityException e) {
            Log.e(TAG, "Exception: " + e.getMessage());
        }
    }

    /**
     * Get the current device location
     */
    @SuppressLint("MissingPermission")
    private void getCurrentLocation() {
        if (!locationPermissionGranted) {
            Toast.makeText(this, "Permission de localisation requise", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            Toast.makeText(this, "Récupération de votre position...", Toast.LENGTH_SHORT).show();

            // Get the most recent location
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        if (location != null) {
                            // Get current location
                            LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                            updateSelectedLocation(currentLatLng);
                            Toast.makeText(MapPickerActivity.this, "Position actuelle trouvée", Toast.LENGTH_SHORT).show();
                        } else {
                            // Last location might be null, request location updates
                            requestLocationUpdates();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error getting location: " + e.getMessage());
                        Toast.makeText(MapPickerActivity.this,
                                "Erreur lors de la récupération de votre position",
                                Toast.LENGTH_SHORT).show();

                        // Fallback to location updates
                        requestLocationUpdates();
                    });
        } catch (SecurityException e) {
            Log.e(TAG, "Exception: " + e.getMessage());
        }
    }

    /**
     * Request location updates if getLastLocation failed
     */
    @SuppressLint("MissingPermission")
    private void requestLocationUpdates() {
        if (!locationPermissionGranted) {
            return;
        }

        try {
            // Create location request
            LocationRequest locationRequest = new LocationRequest.Builder(10000)
                    .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                    .setMaxUpdates(1) // We only need one update
                    .build();

            // Request location updates
            fusedLocationClient.requestLocationUpdates(locationRequest,
                    locationCallback,
                    Looper.getMainLooper());
        } catch (SecurityException e) {
            Log.e(TAG, "Exception: " + e.getMessage());
        }
    }

    /**
     * Search for a location by name (country, city, address)
     * @param query The search query
     */
    private void searchLocation(String query) {
        Log.d(TAG, "Searching for location: " + query);
        try {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocationName(query, 5);

            if (addresses == null || addresses.isEmpty()) {
                Toast.makeText(this, "Aucun lieu trouvé pour cette recherche", Toast.LENGTH_SHORT).show();
                return;
            }

            // Get the first result
            Address address = addresses.get(0);
            LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());

            // Update the map
            updateSelectedLocation(latLng);

            // Show a success message
            Toast.makeText(this, "Lieu trouvé: " + address.getAddressLine(0), Toast.LENGTH_SHORT).show();

        } catch (IOException e) {
            Log.e(TAG, "Error searching for location: " + e.getMessage());
            Toast.makeText(this, "Erreur lors de la recherche: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Update the selected location on the map
     * @param latLng The selected coordinates
     */
    private void updateSelectedLocation(LatLng latLng) {
        selectedLatLng = latLng;

        if (selectedMarker != null) {
            selectedMarker.remove();
        }

        selectedMarker = mMap.addMarker(new MarkerOptions()
                .position(latLng)
                .title("Emplacement sélectionné"));

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM));

        // Get address from the selected location
        getAddressFromLocation(latLng);
    }

    private void getAddressFromLocation(LatLng latLng) {
        try {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);

            if (addresses != null && addresses.size() > 0) {
                Address address = addresses.get(0);

                // Get address components
                selectedStreet = address.getThoroughfare() != null ? address.getThoroughfare() : "";
                if (selectedStreet.isEmpty() && address.getSubThoroughfare() != null) {
                    selectedStreet = address.getSubThoroughfare();
                }

                selectedCity = address.getLocality() != null ? address.getLocality() : "";
                if (selectedCity.isEmpty() && address.getSubAdminArea() != null) {
                    selectedCity = address.getSubAdminArea();
                }

                selectedCountry = address.getCountryName() != null ? address.getCountryName() : "";

                // Get full address
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                    sb.append(address.getAddressLine(i));
                    if (i < address.getMaxAddressLineIndex()) {
                        sb.append(", ");
                    }
                }
                fullAddress = sb.toString();

                // Display address information
                String displayText = "Adresse: " + fullAddress;
                if (tvAddressInfo != null) {
                    tvAddressInfo.setText(displayText);
                    tvAddressInfo.setVisibility(View.VISIBLE);
                }

                Log.d(TAG, "Address found: " + fullAddress);
                Log.d(TAG, "Street: " + selectedStreet + ", City: " + selectedCity + ", Country: " + selectedCountry);

                // Update marker title
                if (selectedMarker != null) {
                    selectedMarker.setTitle(fullAddress);
                    selectedMarker.showInfoWindow();
                }
            } else {
                Log.e(TAG, "No address found for the location");
                if (tvAddressInfo != null) {
                    tvAddressInfo.setText("Adresse non trouvée pour cet emplacement");
                    tvAddressInfo.setVisibility(View.VISIBLE);
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "Error getting address: " + e.getMessage());
            if (tvAddressInfo != null) {
                tvAddressInfo.setText("Erreur lors de la récupération de l'adresse");
                tvAddressInfo.setVisibility(View.VISIBLE);
            }
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error in getAddressFromLocation: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Stop location updates when activity is no longer in foreground
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }
}