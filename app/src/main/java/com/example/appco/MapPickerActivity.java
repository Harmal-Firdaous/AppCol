package com.example.appco;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapPickerActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "MapPickerActivity";
    private GoogleMap mMap;
    private Marker selectedMarker;
    private Button btnConfirmLocation;
    private LatLng selectedLatLng;
    // Default location (Tunisia)
    private static final LatLng DEFAULT_LOCATION = new LatLng(36.8065, 10.1815);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_picker);

        Log.d(TAG, "onCreate: Starting MapPickerActivity");

        try {
            btnConfirmLocation = findViewById(R.id.btnConfirmLocation);
            Log.d(TAG, "Button found: " + (btnConfirmLocation != null));

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

            btnConfirmLocation.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d(TAG, "Confirm button clicked");
                    if (selectedLatLng != null) {
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("latitude", selectedLatLng.latitude);
                        resultIntent.putExtra("longitude", selectedLatLng.longitude);
                        setResult(RESULT_OK, resultIntent);
                        finish();
                    } else {
                        Toast.makeText(MapPickerActivity.this,
                                "Veuillez sélectionner un emplacement",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            });
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

            // Move camera to default location
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(DEFAULT_LOCATION, 10));

            mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                @Override
                public void onMapClick(LatLng latLng) {
                    Log.d(TAG, "Map clicked at: " + latLng.latitude + ", " + latLng.longitude);
                    selectedLatLng = latLng;

                    if (selectedMarker != null) {
                        selectedMarker.remove();
                    }

                    selectedMarker = mMap.addMarker(new MarkerOptions()
                            .position(latLng)
                            .title("Emplacement sélectionné"));
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error in onMapReady: " + e.getMessage());
            Toast.makeText(this, "Error loading map: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
}
