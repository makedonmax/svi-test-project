package com.testproj.app.ui;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.sothree.slidinguppanel.SlidingUpPanelLayout.PanelState;
import com.testproj.app.R;
import com.testproj.app.data.Place;
import com.testproj.app.util.ImageLoader;

import java.util.HashMap;
import java.util.List;

public class PlacesFragment extends Fragment implements SlidingUpPanelLayout.PanelSlideListener {

    private static final String MAP_FRAGMENT_TAG = "map";
    private static final String PLACES_LIST_STATE = "places_list_visibility";
    private static final float ZOOMTO_COLLAPSED_STATE = 14f;
    private static final float ZOOMTO_EXPANDED_STATE = 11f;
    private static final int ANIMATE_DURATION_MS = 800;
    public static final String LIST_SCROLL_POSITION = "scroll_position";

    private List<Place> mData;
    private HashMap<Marker, Place> mMarkerPlaceHashMap;
    private PlacesFragmentListener mListener;
    private OnPlaceClickListener mPlaceClickListener = new OnPlaceClickListener();

    private GoogleMap mMap;
    private ListView mPlacesListView;
    private SlidingUpPanelLayout mSlidingUpPanelLayout;
    private Button mShowBtn;
    private View mMainView;

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

        if (mMainView == null) {
            mMainView = inflater.inflate(R.layout.places, container, false);

            mPlacesListView = (ListView) mMainView.findViewById(R.id.list);
            mPlacesListView.setOnItemClickListener(mPlaceClickListener);

            mSlidingUpPanelLayout = (SlidingUpPanelLayout) mMainView.findViewById(R.id.sliding_layout);
            mSlidingUpPanelLayout.setAnchorPoint(0.6f);
            mSlidingUpPanelLayout.setPanelSlideListener(this);

            mShowBtn = (Button) mMainView.findViewById(R.id.show_content_btn);
            mShowBtn.setOnClickListener(new OnShowButtonClickListener());
        }

        PanelState state = PanelState.ANCHORED;
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(PLACES_LIST_STATE)) {
                state = PanelState.valueOf(savedInstanceState.getString(PLACES_LIST_STATE));
            }
            mPlacesListView.setSelection(savedInstanceState.getInt(LIST_SCROLL_POSITION));
        }
        mSlidingUpPanelLayout.setPanelState(state);

        int btnVisibility = (PanelState.COLLAPSED == mSlidingUpPanelLayout.getPanelState())
                            ? View.VISIBLE : View.INVISIBLE;
        mShowBtn.setVisibility(btnVisibility);

        return mMainView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        MapFragment mapFragment = MapFragment.newInstance();

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
        PlacesListAdapter adapter = new PlacesListAdapter(getActivity(), mData, mListener.getImageLoader());
        mPlacesListView.setAdapter(adapter);
        updateMap();
    }

    private void updateMap() {
        if (mData == null || mMap == null) return;
        HashMap<Marker, Place> hashMap = getMarkerPlaceHashMap();
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (Place place : mData) {
            LatLng position = new LatLng(place.getLocation().getLatitude(), place.getLocation().getLongitude());
            Marker marker = mMap.addMarker(new MarkerOptions().position(position).title(place.getTitle()));
            hashMap.put(marker, place);
            builder.include(position);
        }
        setCustomInfoWindow();
        mMap.setOnInfoWindowClickListener(mPlaceClickListener);
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setCompassEnabled(false);
        mMap.getUiSettings().setZoomControlsEnabled(false);

        LatLngBounds bounds = builder.build();
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 0);
        mMap.animateCamera(cu);
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
        String state = mSlidingUpPanelLayout.getPanelState().name();
        outState.putString(PLACES_LIST_STATE, state);
        outState.putInt(LIST_SCROLL_POSITION, mPlacesListView.getFirstVisiblePosition());
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

    @Override
    public void onPanelSlide(View panel, float slideOffset) {
    }

    @Override
    public void onPanelCollapsed(View panel) {
        mMap.animateCamera(CameraUpdateFactory.zoomTo(ZOOMTO_COLLAPSED_STATE), ANIMATE_DURATION_MS, null);

        mShowBtn.setVisibility(View.VISIBLE);
    }

    @Override
    public void onPanelExpanded(View panel) {
        mMap.animateCamera(CameraUpdateFactory.zoomTo(ZOOMTO_EXPANDED_STATE), ANIMATE_DURATION_MS, null);
    }

    @Override
    public void onPanelAnchored(View panel) {
    }

    @Override
    public void onPanelHidden(View panel) {
    }

    public interface PlacesFragmentListener {
        ImageLoader getImageLoader();
    }

    private class OnPlaceClickListener implements GoogleMap.OnInfoWindowClickListener,
            AdapterView.OnItemClickListener {

        @Override
        public void onInfoWindowClick(Marker marker) {
            Place place = getMarkerPlaceHashMap().get(marker);
            if (place != null) {
                startPlaceInfoActivity(place);
            }
        }

        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
            Place place = null;
            if (mData != null && (place = mData.get(position)) != null) {
                startPlaceInfoActivity(place);
            }
        }
    }

    private class OnShowButtonClickListener implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            if (mSlidingUpPanelLayout != null) {
                mPlacesListView.setSelection(0);
                mSlidingUpPanelLayout.setPanelState(PanelState.ANCHORED);
                mShowBtn.setVisibility(View.INVISIBLE);
            }
        }
    }
}
