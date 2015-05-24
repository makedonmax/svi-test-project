package com.testproj.app;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

public class PlacesFragment extends Fragment {

    private GoogleMap mMap;
    private List<Place> mData;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.places, container, false);

        MapFragment mMapFragment = new MapFragment();
        mMapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mMap = googleMap;
                updateMap();
            }
        });
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.add(R.id.fragment_container, mMapFragment).commit();

        return view;
    }

    public void setData(List<Place> data) {
        mData = data;
        updateMap();
    }

    private void updateMap() {
        if (mData == null || mMap == null) return;
        for (Place place : mData) {
            mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(place.getLocation().getLatitude(), place.getLocation().getLongitude()))
                    .title(place.getTitle()));
        }
    }
}
