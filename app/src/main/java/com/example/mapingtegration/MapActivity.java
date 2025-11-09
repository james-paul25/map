package com.example.mapingtegration;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;

import java.util.Arrays;


public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private Button btnCurrentLocation, btnChangeMapType;
    private TextView tvCoordinates;

    @Override
    protected void onCreate(Bundle savedInstance){
        super.onCreate(savedInstance);
        setContentView(R.layout.activity_map);

        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), getString(R.string.google_map_key));
        }

        initAutocomplete();

        btnCurrentLocation = findViewById(R.id.btnCurrentLocation);
        tvCoordinates = findViewById(R.id.tvCoordinates);
        btnChangeMapType = findViewById(R.id.btnChangeMapType);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);

        btnCurrentLocation.setOnClickListener(v -> {
            getCurrentLocation();
        });

        btnChangeMapType.setOnClickListener(v -> changeMapType());
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        LatLng manila = new LatLng(14.5995, 120.9842);
        mMap.addMarker(new MarkerOptions()
                .position(manila)
                .title("Manila, Philippines"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(manila, 12));
        checkLocationPermission();
    }

    private void checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            enableMyLocation();
        }
    }
    private void enableMyLocation() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            getCurrentLocation();
        }
    }

    private void changeMapType(){
        if (mMap != null) {
            int currentType = mMap.getMapType();

            if (currentType == GoogleMap.MAP_TYPE_NORMAL) {
                mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
            }
            else if (currentType == GoogleMap.MAP_TYPE_SATELLITE) {
                mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
            }
            else {
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            }
        }
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            Toast.makeText(this, "Location permission not granted",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "Getting current location...",
                Toast.LENGTH_SHORT).show();

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @SuppressLint("DefaultLocale")
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            double latitude = location.getLatitude();
                            double longitude = location.getLongitude();

                            tvCoordinates.setText(String.format(
                                    "Lat: %.4f, Lng: %.4f", latitude, longitude));
                            LatLng currentLocation = new LatLng(latitude, longitude);
                            mMap.clear();
                            mMap.addMarker(new MarkerOptions()
                                    .position(currentLocation)
                                    .title("You are here"))
                                    .setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                            mMap.animateCamera(
                                    CameraUpdateFactory.newLatLngZoom(currentLocation, 15),
                                    2000,
                                    null);

                            Toast.makeText(MapActivity.this,
                                    "Location found!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(MapActivity.this,
                                    "Unable to get location. Try again.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void initAutocomplete() {
        AutocompleteSupportFragment autocompleteFragment =
                (AutocompleteSupportFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.autocomplete_fragment);

        assert autocompleteFragment != null;

        EditText editText = autocompleteFragment.getView()
                .findViewById(com.google.android.libraries.places.R.id.places_autocomplete_search_input);

        editText.setBackgroundResource(R.drawable.search_bar_bg);
        editText.setTextColor(Color.WHITE);
        editText.setHintTextColor(Color.BLACK);
        editText.setTextSize(10);
        editText.setPadding(40, 10, 10, 10);
        editText.setHint("Search location");

        autocompleteFragment.setPlaceFields(Arrays.asList(
                Place.Field.ID,
                Place.Field.NAME,
                Place.Field.LAT_LNG,
                Place.Field.ADDRESS
        ));

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                Log.d("AUTOCOMPLETE", "Place: " + place.getName());
                if (place.getLatLng() != null) {
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), 16));
                }
            }

            @Override
            public void onError(@NonNull Status status) {
                Log.e("AUTOCOMPLETE", "Error: " + status);
            }
        });
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableMyLocation();
                Toast.makeText(this, "Location permission granted",
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Location permission denied",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }
}