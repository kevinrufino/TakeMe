package com.example.takeme;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;

import com.example.takeme.directions.FetchURL;
import com.example.takeme.directions.TaskLoadedCallback;
import com.google.android.gms.common.api.ApiException;
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
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FetchPlaceResponse;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.mancj.materialsearchbar.adapter.SuggestionsAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, TaskLoadedCallback {

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
    //directions route line
    private Polyline currentPolyline;

    //views
    private MaterialSearchBar materialSearchBar;
    private View mapView;
    private final float DEFAULT_ZOOM = 19;

    //this is for the button on the location fragment
    private Button getDirection;
    private LatLng currentLocation, gotoLocation;

    LocationFragment locationFrag;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        /** FIX ME **/ //get current to read our current location
//        currentLocation = new LatLng(fusedLocationProviderClient.getLastLocation().getResult().getLatitude(),
//                fusedLocationProviderClient.getLastLocation().getResult().getLongitude());

        //static current location
        currentLocation = new LatLng(42.9635, -85.8886);
        //static places location
        gotoLocation = new LatLng(42.9669, 85.8872);


//        new FetchURL(MapActivity.this).execute
//                (getUrl(currentLocation,gotoLocation), "walking");
//        getDirection = getDirection.findViewById(R.id.directions_button);
//        getDirection.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                new FetchURL(MapActivity.this).execute
//                        (getUrl(currentLocation,gotoLocation), "walking");
//            }
//        });

        materialSearchBar = findViewById(R.id.searchBar);

        final SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);

        //for my location button to be relocated
        mapView = mapFragment.getView();

        Fragment f = getSupportFragmentManager().findFragmentById(R.id.locationFragment);
        if (f instanceof LocationFragment) {
            locationFrag = (LocationFragment) f;
        }

        //for getDeviceLocation method
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MapActivity.this);

        new FetchURL(MapActivity.this).execute(getUrl(currentLocation,
                gotoLocation), "walking");

        //places api and autofill
        Places.initialize(MapActivity.this, Constants.GOOGLE_MAPS_APIKEY);
        placesClient = Places.createClient(this);
        final AutocompleteSessionToken token = AutocompleteSessionToken.newInstance();

        //material search bar
        materialSearchBar.setOnSearchActionListener(new MaterialSearchBar.OnSearchActionListener() {
            @Override
            public void onSearchStateChanged(boolean enabled) {

            }

            @Override
            public void onSearchConfirmed(CharSequence text) {
                startSearch(text.toString(), true, null, true);
            }

            @Override
            public void onButtonClicked(int buttonCode) {
                if (buttonCode == MaterialSearchBar.BUTTON_NAVIGATION) {
                    //opening or closing a navigation drawer
                } else if (buttonCode == MaterialSearchBar.BUTTON_BACK) {
                    materialSearchBar.disableSearch();
                }
            }
        });

        materialSearchBar.addTextChangeListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                FindAutocompletePredictionsRequest predictionsRequest = FindAutocompletePredictionsRequest.builder()
                        .setTypeFilter(TypeFilter.ESTABLISHMENT)
                        .setSessionToken(token)
                        .setQuery(s.toString())
                        .build();
                placesClient.findAutocompletePredictions(predictionsRequest).addOnCompleteListener(new OnCompleteListener<FindAutocompletePredictionsResponse>() {
                    @Override
                    public void onComplete(@NonNull Task<FindAutocompletePredictionsResponse> task) {
                        if (task.isSuccessful()) {
                            FindAutocompletePredictionsResponse predictionsResponse = task.getResult();
                            if (predictionsResponse != null) {
                                predictionList = predictionsResponse.getAutocompletePredictions();
                                List<String> suggestionsList = new ArrayList<>();
                                for (int i = 0; i < predictionList.size(); i++) {
                                    AutocompletePrediction prediction = predictionList.get(i);
                                    suggestionsList.add(prediction.getFullText(null).toString());
                                }
                                materialSearchBar.updateLastSuggestions(suggestionsList);
                                if (!materialSearchBar.isSuggestionsVisible()) {
                                    materialSearchBar.showSuggestionsList();
                                }
                            }
                        } else {
                            Log.i("mytag", "prediction fetching task unsuccessful");
                        }
                    }
                });
            }

            @Override
            public void afterTextChanged(Editable s) {
                materialSearchBar.clearSuggestions();
            }
        });

        materialSearchBar.setSuggstionsClickListener(new SuggestionsAdapter.OnItemViewClickListener() {
            @Override
            public void OnItemClickListener(int position, View v) {
                if (position >= predictionList.size()) {
                    return;
                }

                AutocompletePrediction selectedPrediction = predictionList.get(position);
                String suggestion = materialSearchBar.getLastSuggestions().get(position).toString();
                materialSearchBar.setText(suggestion);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        materialSearchBar.clearSuggestions();
                    }
                }, 1);

                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                if (inputMethodManager != null)
                    inputMethodManager.hideSoftInputFromWindow(materialSearchBar.getWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY);

                /** FIX ME**/ //we want to take the place id or the latlng for FetchURL
                final String placeId = selectedPrediction.getPlaceId();
                List<Place.Field> placeFields = Arrays.asList(Place.Field.LAT_LNG);

                FetchPlaceRequest fetchPlaceRequest = FetchPlaceRequest.builder(placeId, placeFields).build();
                placesClient.fetchPlace(fetchPlaceRequest).addOnSuccessListener(new OnSuccessListener<FetchPlaceResponse>() {
                    @Override
                    public void onSuccess(FetchPlaceResponse fetchPlaceResponse) {
                        Place place = fetchPlaceResponse.getPlace();
                        Log.i("mytag", "Place found: " + place.getName());
                        LatLng latLngOfPlace = place.getLatLng();
                        if (latLngOfPlace != null) {
                            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLngOfPlace, DEFAULT_ZOOM));
                            materialSearchBar.clearSuggestions();
                            materialSearchBar.disableSearch();

                            //ConstraintLayout.LayoutParams mapParams = (ConstraintLayout.LayoutParams) mapFragment.getView().getLayoutParams();

                            // KABOOM!!!  This line crashes at runtime as it says the parent of the view is a FrameLayout, though I would
                            // expect it to be a ConstraintLayout. 
                            ConstraintLayout.LayoutParams locationParams = (ConstraintLayout.LayoutParams) locationFrag.getView().getLayoutParams();

                            //mapParams.verticalWeight = 1;
                            locationParams.verticalWeight = 0;

//                            mapFragment.getView().setLayoutParams(mapParams);
                            locationFrag.getView().setLayoutParams(locationParams);

//                            mapFragment.getView().invalidate();
//                            mapFragment.getView().requestLayout();
                            locationFrag.getView().invalidate();
                            locationFrag.getView().requestLayout();

//                            getSupportFragmentManager()
//                                    .beginTransaction()
//                                    .detach(mapFragment)
//                                    .attach(mapFragment)
//                                    .commit();
//                            getSupportFragmentManager()
//                                    .beginTransaction()
//                                    .detach(locationFrag)
//                                    .attach(locationFrag)
//                                    .commit();

                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if (e instanceof ApiException) {
                            ApiException apiException = (ApiException) e;
                            apiException.printStackTrace();
                            int statusCode = apiException.getStatusCode();
                            Log.i("mytag", "place not found: " + e.getMessage());
                            Log.i("mytag", "status code: " + statusCode);
                        }
                    }
                });
            }

            @Override
            public void OnItemDeleteListener(int position, View v) {
                materialSearchBar.clearSuggestions();
            }
        });

    }

    //called when map is ready and loaded
    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        this.googleMap.setMyLocationEnabled(true);
        this.googleMap.getUiSettings().setMyLocationButtonEnabled(true);
        googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.style_json));

//        /** FIX ME **/ // mapView is never true
//        if(mapView != null && mapView.findViewById(Integer.parseInt("1")) != null){
//            View myLocationButton = ((View) mapView.findViewById(Integer.parseInt("1"))
//                    .getParent()).findViewById(Integer.parseInt("2"));
//            //fetches layout parameters
//            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) myLocationButton.getLayoutParams();
//
//            //sets location properties for button
//            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
//            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
//            layoutParams.setMargins(0, 0, 40, 180);
//        }

        //this sets the current location to clear all search bar
        googleMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                if (materialSearchBar.isSuggestionsVisible())
                    materialSearchBar.clearSuggestions();
                if (materialSearchBar.isSearchEnabled())
                    materialSearchBar.disableSearch();
                return false;
            }
        });

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

    /** FIX ME Get current location**/
    private void getDeviceLocation() {
        fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
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

    private String getUrl(LatLng cl, LatLng dest) {
        // current location
        String str_origin = "origin=" + cl.latitude + "," + cl.longitude;
        // goto location
        String str_dest = "destination=place_id:ChIJNY9oTuKiGYgROn64oGsb9zI"; //dest.latitude + "," + dest.longitude;
        // Mode
        String mode = "mode=" + "walking";
        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + mode;
        // Output format
        String output = "json";
        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters + "&key=" + Constants.GOOGLE_MAPS_APIKEY;
        return url;
    }

    @Override
    public void onTaskDone(Object... values) {
        if (currentPolyline != null)
            currentPolyline.remove();
        currentPolyline = googleMap.addPolyline((PolylineOptions) values[0]);
    }

//    /**
//     * A simple {@link Fragment} subclass.
//     * Use the {@link LocationFragment#newInstance} factory method to
//     * create an instance of this fragment.
//     */
//    public static class LocationFragment extends Fragment {
//        // TODO: Rename parameter arguments, choose names that match
//        // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
//        private static final String ARG_PARAM1 = "param1";
//        private static final String ARG_PARAM2 = "param2";
//
//        /**
//         //set <FRAGMENT/>: android:layout_height = "350dp"
//         //set <MAP/>: android:layout_height = "0dp"
//         //take Place Photos and set it to imageview
//         //take Places latlng and set up button to draw route
//         **/
//
//        //this is for the button on the location fragment
//        private Button getDirection;
//        private LatLng currentLocation, gotoLocation;
//
//
//        public LocationFragment() {
//            // Required empty public constructor
//        }
//
//        /**
//         * Use this factory method to create a new instance of
//         * this fragment using the provided parameters.
//         *
//         * @param currentLocation current location of device.
//         * @param gotoLocation location we want directions to.
//         * @return A new instance of fragment BlankFragment.
//         */
//        // TODO: Rename and change types and number of parameters
//        LocationFragment newInstance(LatLng currentLocation, LatLng gotoLocation) {
//            LocationFragment fragment = new LocationFragment();
//            Bundle args = new Bundle();
////        args.putString(ARG_PARAM1, param1);
////        args.putString(ARG_PARAM2, param2);
////        fragment.setArguments(args);
//            return fragment;
//        }
//
//        @Override
//        public void onCreate(Bundle savedInstanceState) {
//            super.onCreate(savedInstanceState);
////        if (getArguments() != null) {
////            mParam1 = getArguments().getString(ARG_PARAM1);
////            mParam2 = getArguments().getString(ARG_PARAM2);
////        }
////         get
//
//        }
//
//
//        @Override
//        public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                                 Bundle savedInstanceState) {
//            // Inflate the layout for this fragment
//            return inflater.inflate(R.layout.location_fragment, container, false);
//        }
//
//    }
}
