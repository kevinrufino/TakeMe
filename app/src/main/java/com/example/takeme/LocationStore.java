package com.example.takeme;

import com.google.android.gms.maps.model.LatLng;

public class LocationStore {

    private static LocationStore instance = null;

    private LatLng location;

    private LocationStore() {
        location = null;
    }

    public static LocationStore getInstance() {
        if (instance == null) {
            instance = new LocationStore();
        }
        return instance;
    }

    public LatLng getLocation() {
        return location;
    }

    public void setLocation(LatLng location) {
        this.location = location;
    }
}
