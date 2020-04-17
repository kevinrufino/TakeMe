package com.example.takeme;

import android.app.Dialog;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.example.takeme.directions.FetchURL;
import com.example.takeme.directions.TaskLoadedCallback;
import com.google.android.gms.maps.model.LatLng;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link LocationFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LocationFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    /**
    //take Place Photos and set it to imageview
     **/

    //this is for the button on the location fragment
    private Button getDirection;

    private LocationFragment locationFrag;

    private MapActivity mapActivity;

    public LocationFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param currentLocation current location of device.
     * @param gotoLocation location we want directions to.
     * @return A new instance of fragment BlankFragment.
     */
    // TODO: Rename and change types and number of parameters
    private LocationFragment newInstance(LatLng currentLocation, LatLng gotoLocation) {
        LocationFragment fragment = new LocationFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View rootView =  inflater.inflate(R.layout.location_fragment, container, false);
//        final View coorView =  inflater.inflate(R.layout.activity_map, container, false);

        getDirection = rootView.findViewById(R.id.directions_button);
        getDirection.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                LatLng clocation = CurrentLocationStore.getInstance().getcLocation();
                Log.d("LocationFragment", "Destination Location is " + clocation.toString());
                LatLng dlocation = DestinationLocationStore.getInstance().getdLocation();
                Log.d("LocationFragment", "Destination Location is " + dlocation.toString());

                // Note that we need to get the parent view because this thing is a fragment container!
                View vp = (View) rootView.getParent();

                // Set this weight to 1 to make LocationFrag show up.  set it to 0 to make it disappear.
                ConstraintLayout.LayoutParams locationParams = (ConstraintLayout.LayoutParams) vp.getLayoutParams();
                locationParams.verticalWeight = 0;
                vp.setLayoutParams(locationParams);

                // once you update that weight, tell the entire view to refresh itself.
                // coordinatorlayout is null, crashes program
//                View coordinatorLayout = coorView.findViewById(R.id.topCoordinator);
//                coordinatorLayout.invalidate();
//                coordinatorLayout.requestLayout();

                //sets route
                new FetchURL(v.getContext()).execute(getUrl(clocation,dlocation), "walking");
                //call method to change camera back to original postion
                ((MapActivity)getActivity()).getDeviceLocation();
            }
        });
        return rootView;
    }

    private String getUrl(LatLng cl, LatLng dest) {
        // current location
        String str_origin = "origin=" + cl.latitude + "," + cl.longitude;
        // goto location
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
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
}
