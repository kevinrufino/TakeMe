package com.example.takeme;

import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.mancj.materialsearchbar.MaterialSearchBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.util.List;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    //fetches map object
    private GoogleMap googleMap;
    //fetches current location of device
    private FusedLocationProviderClient fusedLocationProviderClient;
    //loads suggestions
    private PlacesClient placesClient;
    //list of predictions
    private List<AutocompletePrediction> predictionList;
    //last location of device
    private Location lastKnownLocation;
    //used if last location is not valid?
    private LocationCallback locationCallback;

    //views
    private MaterialSearchBar materialSearchBar;
    private View mapView;
    private final float DEFAULT_ZOOM = 20;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        materialSearchBar = findViewById(R.id.searchBar);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //for my location button to be relocated
        mapView = mapFragment.getView();

        //for getDeviceLocation method
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MapActivity.this);
        Places.initialize(MapActivity.this, Constants.GOOGLE_MAPS_APIKEY);
        placesClient = Places.createClient(this);
        AutocompleteSessionToken token = AutocompleteSessionToken.newInstance();

        //places api search bar
    }

    //called when map is ready and loaded
    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        this.googleMap.setMyLocationEnabled(true);
        this.googleMap.getUiSettings().setMyLocationButtonEnabled(true);
        googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.style_json));

        /** FIX ME **/ // mapView is never true
        if(mapView != null && mapView.findViewById(Integer.parseInt("1")) != null){
            View myLocationButton = ((View) mapView.findViewById(Integer.parseInt("1"))
                    .getParent()).findViewById(Integer.parseInt("2"));
            //fetches layout parameters
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) myLocationButton.getLayoutParams();

            //sets location properties for button
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
            layoutParams.setMargins(0, 0, 40, 180);

        }

        View myLocationButton = ((View) mapView.findViewById(Integer.parseInt("1"))
                .getParent()).findViewById(Integer.parseInt("2"));
        //fetches layout parameters
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) myLocationButton.getLayoutParams();

        //sets location properties for button
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        layoutParams.setMargins(0, 0, 40, 180);


        //GPS Enabled checker
        //request user to enable it

        LocationRequest locReq = LocationRequest.create();
        locReq.setInterval(10000);
        locReq.setFastestInterval(5000);
        locReq.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locReq);

        SettingsClient settingsClient = LocationServices.getSettingsClient(MapActivity.this);
        Task<LocationSettingsResponse> task = settingsClient.checkLocationSettings(builder.build());

        task.addOnSuccessListener(MapActivity.this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                getDeviceLocation();
            }
        });

        task.addOnFailureListener(MapActivity.this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    ResolvableApiException resolvable = (ResolvableApiException) e;
                    try {
                        resolvable.startResolutionForResult(MapActivity.this, 51);
                    } catch (IntentSender.SendIntentException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 51) {
            if (resultCode == RESULT_OK) {
                getDeviceLocation();
            }
        }
    }

    private void getDeviceLocation() {
        fusedLocationProviderClient.getLastLocation()
                .addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                if(task.isSuccessful()){
                    lastKnownLocation = task.getResult();
                    //even if task is successful, it could be null
                    if(lastKnownLocation != null){
                        //this moves map to current location
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new
                                LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude()),
                                DEFAULT_ZOOM));
                    } else {
                        //check if last location and fused location is null
                        final LocationRequest locRequest = LocationRequest.create();
                        locRequest.setInterval(10000);
                        locRequest.setFastestInterval(5000);
                        locRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                        locationCallback = new LocationCallback() {
                            @Override
                            public void onLocationResult(LocationResult locationResult) {
                                super.onLocationResult(locationResult);
                                if (locationResult == null) {
                                    return;
                                }
                                lastKnownLocation = locationResult.getLastLocation();
                                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new
                                        LatLng(lastKnownLocation.getLatitude(),
                                        lastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                                fusedLocationProviderClient.removeLocationUpdates(locationCallback);
                            }
                        };
                        //hand new location request over to locationCallback
                        fusedLocationProviderClient.requestLocationUpdates(locRequest,
                                locationCallback, null);
                    }
                }

                //task not sucessful
                else {
                    Toast.makeText(MapActivity.this, "last location unavailable",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

}
