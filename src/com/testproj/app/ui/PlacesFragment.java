package com.testproj.app.ui;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.testproj.app.R;
import com.testproj.app.data.Place;
import com.testproj.app.util.BitmapCache;
import com.testproj.app.util.DiskBitmapCache;
import com.testproj.app.util.ImageLoader;

import java.util.HashMap;
import java.util.List;

public class PlacesFragment extends Fragment {

    public interface PlacesFragmentListener {
        ImageLoader getImageLoader();
    }

    private static final String MAP_FRAGMENT_TAG = "map";
    public static final String PLACES_LIST_VISIBILITY_KEY = "places_list_visibility";

    private List<Place> mData;
    private HashMap<Marker, Place> mMarkerPlaceHashMap;
    private PlacesFragmentListener mListener;

    private GoogleMap mMap;
    private Button mListButton;
    private RecyclerView mPlacesListView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            mListener = (PlacesFragmentListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement PlacesFragmentListener");
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View mainView = inflater.inflate(R.layout.places, container, false);

        mPlacesListView = (RecyclerView)mainView.findViewById(R.id.list);
        mPlacesListView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mPlacesListView.setLayoutManager(layoutManager);

        mListButton = (Button) mainView.findViewById(R.id.list_btn);
        mListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int visibility = mPlacesListView.getVisibility() == View.VISIBLE ? View.INVISIBLE : View.VISIBLE;
                mPlacesListView.setVisibility(visibility);
            }
        });

        if (savedInstanceState != null) {
            int visibility = savedInstanceState.getBoolean(PLACES_LIST_VISIBILITY_KEY) ? View.VISIBLE : View.INVISIBLE;
            mPlacesListView.setVisibility(visibility);
        }

        return mainView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        MapFragment mapFragment = new MapFragment();

        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, mapFragment, MAP_FRAGMENT_TAG).commit();

        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mMap = googleMap;
                updateMap();
            }
        });
    }

    public void setData(List<Place> data) {
        mData = data;
        PlacesListAdapter adapter = new PlacesListAdapter(mData, mListener.getImageLoader(),
                new OnPlacesListItemClickListener());
        mPlacesListView.setAdapter(adapter);
        updateMap();
    }

    private void updateMap() {
        if (mData == null || mMap == null) return;
        HashMap<Marker, Place> hashMap = getMarkerPlaceHashMap();
        for (Place place : mData) {
            LatLng position = new LatLng(place.getLocation().getLatitude(), place.getLocation().getLongitude());
            Marker marker = mMap.addMarker(new MarkerOptions().position(position).title(place.getTitle()));
            hashMap.put(marker, place);
        }
        setCustomInfoWindow();
        mMap.setOnInfoWindowClickListener(new OnPlaceInfoWindowClickListener());
        mMap.setMyLocationEnabled(true);
    }

    private HashMap<Marker, Place> getMarkerPlaceHashMap() {
        if (mMarkerPlaceHashMap == null) {
            mMarkerPlaceHashMap = new HashMap<Marker, Place>();
        }
        return mMarkerPlaceHashMap;
    }

    private void setCustomInfoWindow() {
        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            @Override
            public View getInfoWindow(Marker arg0) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                View v = getActivity().getLayoutInflater().inflate(R.layout.infowindow, null);
                Place place = getMarkerPlaceHashMap().get(marker);
                TextView title = (TextView) v.findViewById(R.id.title);
                TextView street = (TextView) v.findViewById(R.id.street);
                TextView distance = (TextView) v.findViewById(R.id.distance);
                title.setText(place.getTitle());
                street.setText(place.getLocation().getStreet());
                distance.setText(String.format("%.2f mi", place.getLocation().getDistance()));
                return v;
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(PLACES_LIST_VISIBILITY_KEY, mPlacesListView.getVisibility() == View.VISIBLE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mMap != null) mMap.setOnInfoWindowClickListener(null);
    }

    private void startPlaceInfoActivity(Place place) {
        Intent intent = new Intent(getActivity(), PlaceInfoActivity.class);
        intent.putExtra(PlaceInfoActivity.TITLE_INTENT_KEY, place.getTitle());
        intent.putExtra(PlaceInfoActivity.IMAGE_URL_INTENT_KEY, place.getImageUrl());
        startActivity(intent);
    }

    private class OnPlaceInfoWindowClickListener implements GoogleMap.OnInfoWindowClickListener {

        @Override
        public void onInfoWindowClick(Marker marker) {
            Place place = getMarkerPlaceHashMap().get(marker);
            startPlaceInfoActivity(place);
        }
    }

    private class OnPlacesListItemClickListener implements PlacesListAdapter.OnItemClickListener {

        @Override
        public void onItemClick(int position) {
            Place place = mData.get(position);
            startPlaceInfoActivity(place);
        }
    }
}
