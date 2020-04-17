package com.example.takeme;

import com.google.android.gms.maps.model.LatLng;

public class CurrentLocationStore {

    private static CurrentLocationStore instance = null;

    private LatLng location;

    private CurrentLocationStore() {
        location = null;
    }

    public static CurrentLocationStore getInstance() {
        if (instance == null) {
            instance = new CurrentLocationStore();
        }
        return instance;
    }

    public LatLng getcLocation() {
        return location;
    }

    public void setcLocation(LatLng location) {
        this.location = location;
    }
}
