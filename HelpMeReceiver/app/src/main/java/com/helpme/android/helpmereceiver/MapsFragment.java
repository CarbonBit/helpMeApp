package com.helpme.android.helpmereceiver;
/*
    Programmer: Vitaly Simonovich
    ID: 309398311
 */
import android.graphics.Color;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;


public class MapsFragment extends Fragment {
    GoogleMap googleMap;
    MapView mMapView;
    DatabaseHelper db;
    ArrayList<LatLng> GPScords;
    String victimName = "";
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // inflate and return the layout
        View v = inflater.inflate(R.layout.fragment_maps, container,false);

        db = new DatabaseHelper(getActivity());
        victimName = getArguments().getString("VICTIMNAME");

        mMapView = (MapView) v.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);

        mMapView.onResume();// needed to get the map to display immediately

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        googleMap = mMapView.getMap();
        GPScords = db.getGPSCor(victimName);


        if(GPScords.size() == 0){
            Toast.makeText(getActivity(),"Downloading gps coordinates...",Toast.LENGTH_LONG).show();
        }else{
            //google maps camera view start
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(GPScords.get(0), 20));

            //Draw path from gps point
            Polyline line = googleMap.addPolyline(new PolylineOptions()
                    .addAll(GPScords)
                    .width(5)
                    .color(Color.BLUE)
                    .geodesic(true));
        }




        return v;
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }
}
