package com.example.takeme;

import com.google.android.gms.maps.model.LatLng;

public class DestinationLocationStore {
    private static DestinationLocationStore instance = null;

    private LatLng location;

    private DestinationLocationStore() {
        location = null;
    }

    public static DestinationLocationStore getInstance() {
        if (instance == null) {
            instance = new DestinationLocationStore();
        }
        return instance;
    }

    public LatLng getdLocation() {
        return location;
    }

    public void setdLocation(LatLng location) {
        this.location = location;
    }
}
