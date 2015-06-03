package com.testproj.app.ui;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.sothree.slidinguppanel.SlidingUpPanelLayout.SlideState;
import com.testproj.app.R;
import com.testproj.app.data.Place;
import com.testproj.app.util.loader.ImageLoader;
import com.testproj.app.util.loader.PlaceDataLoader;
import com.testproj.app.util.loader.PlaceDataLoader.OnDataLoadedListener;

import java.util.HashMap;
import java.util.List;

public class PlacesFragment extends Fragment implements SlidingUpPanelLayout.PanelSlideListener {

    private static final String MAP_FRAGMENT_TAG = "map";
    public static final String LIST_SCROLL_POSITION = "scroll_position";
    private static final String SHOW_LIST_BTN_VISIBLE = "show_list_btn_visible";
    private static final float ZOOMTO_EXPANDED_STATE = 11f;
    private static final int ANIMATE_DURATION_MS = 800;
    public static final float ANCHOR_POINT = 0.4f;

    private List<Place> mData;
    private HashMap<Marker, Place> mMarkerPlaceHashMap;
    private PlacesFragmentListener mListener;
    private OnPlaceClickListener mPlaceClickListener = new OnPlaceClickListener();
    private PlacesListAdapter mAdapter;
    private OnDataLoadedListener mOnDataLoadedListener;
    private int mMapMarkersBoundPaddingPx;
    private LatLngBounds mMarkersBounds;

    private GoogleMap mMap;
    private ListView mPlacesListView;
    private SlidingUpPanelLayout mSlidingUpPanelLayout;
    private Button mShowBtn;
    private View mMainView;
    private PullToRefreshListView mPullToRefreshView;

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

        mMapMarkersBoundPaddingPx = getResources().getDimensionPixelSize(R.dimen.map_markers_bound_padding);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if (mMainView == null) {
            mMainView = inflater.inflate(R.layout.places, container, false);

            mPullToRefreshView = (PullToRefreshListView) mMainView.findViewById(R.id.pull_to_refresh_listview);
            mPullToRefreshView.setMode(PullToRefreshBase.Mode.PULL_FROM_END);
            mPullToRefreshView.setOnRefreshListener(new OnPlacesRefreshListener());

            mPlacesListView = mPullToRefreshView.getRefreshableView();
            mPlacesListView.setOnItemClickListener(mPlaceClickListener);

            mSlidingUpPanelLayout = (SlidingUpPanelLayout) mMainView.findViewById(R.id.sliding_layout);
            mSlidingUpPanelLayout.setEnableDragViewTouchEvents(true);
            mSlidingUpPanelLayout.setAnchorPoint(ANCHOR_POINT);
            mSlidingUpPanelLayout.setPanelHeight(0);
            mSlidingUpPanelLayout.setScrollableView(mPlacesListView, 0);
            mSlidingUpPanelLayout.setPanelSlideListener(this);
            mSlidingUpPanelLayout.expandPane(SlideState.ANCHORED);
            mSlidingUpPanelLayout.setVisibility(View.INVISIBLE);

            mShowBtn = (Button) mMainView.findViewById(R.id.show_content_btn);
            mShowBtn.setOnClickListener(new OnShowButtonClickListener());
        }

        if (savedInstanceState != null) {
            mPlacesListView.setSelection(savedInstanceState.getInt(LIST_SCROLL_POSITION));
        }

        int btnVisibility = mSlidingUpPanelLayout.isCollapsed() ? View.VISIBLE : View.INVISIBLE;
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

        if (mAdapter == null) {
            mListener.getDataLoader().resetLoader();
            mListener.getDataLoader().getNextDataChunk(getOnDataLoadedListener());
        }
    }

    private OnDataLoadedListener getOnDataLoadedListener() {
        if (mOnDataLoadedListener == null) {
            mOnDataLoadedListener = new OnDataLoadedListener() {
                @Override
                public void onDataLoaded(final List<Place> data) {
                    if (data != null) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mData = data;
                                mSlidingUpPanelLayout.setVisibility(View.VISIBLE);
                                mAdapter = new PlacesListAdapter(getActivity(), mData, mListener.getImageLoader());
                                mPlacesListView.setAdapter(mAdapter);
                                updateMap();
                            }
                        });
                    }
                }
            };
        }
        return mOnDataLoadedListener;
    }

    private void updateMap() {
        if (mData == null || mMap == null) return;
        HashMap<Marker, Place> hashMap = getMarkerPlaceHashMap();
        hashMap.clear();
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

        mMarkersBounds = builder.build();
        mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                animateMapToMarkers(mMap, mMarkersBounds);
            }
        });
    }

    private void animateMapToMarkers(GoogleMap map, LatLngBounds bounds) {
        if (map == null || bounds == null) return;
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, mMapMarkersBoundPaddingPx);
        map.animateCamera(cu);
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

    private void startPlaceInfoActivity(Place place) {
        Intent intent = new Intent(getActivity(), PlaceInfoActivity.class);
        intent.putExtra(PlaceInfoActivity.TITLE_INTENT_KEY, place.getTitle());
        intent.putExtra(PlaceInfoActivity.IMAGE_URL_INTENT_KEY, place.getImageUrl());
        startActivity(intent);
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(SHOW_LIST_BTN_VISIBLE, mSlidingUpPanelLayout.isCollapsed());
        outState.putInt(LIST_SCROLL_POSITION, mPlacesListView.getFirstVisiblePosition());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mMap != null) mMap.setOnInfoWindowClickListener(null);
    }

    @Override
    public void onPanelSlide(View panel, float slideOffset) {
    }

    @Override
    public void onPanelCollapsed(View panel) {
        animateMapToMarkers(mMap, mMarkersBounds);
        mShowBtn.setVisibility(View.VISIBLE);
    }

    @Override
    public void onPanelExpanded(View panel) {
        mMap.animateCamera(CameraUpdateFactory.zoomTo(ZOOMTO_EXPANDED_STATE), ANIMATE_DURATION_MS, null);
    }

    @Override
    public void onPanelAnchored(View panel) {
    }

    public interface PlacesFragmentListener {
        ImageLoader getImageLoader();
        PlaceDataLoader getDataLoader();
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
            Place place = (Place) mPlacesListView.getItemAtPosition(position);
            if (place != null) {
                startPlaceInfoActivity(place);
            }
        }
    }

    private class OnShowButtonClickListener implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            if (mSlidingUpPanelLayout != null) {
                mPlacesListView.setSelection(0);
                mShowBtn.setVisibility(View.INVISIBLE);
                mSlidingUpPanelLayout.expandPane(SlideState.ANCHORED);
            }
        }
    }

    private class OnPlacesRefreshListener implements PullToRefreshBase.OnRefreshListener {

        private OnDataLoadedListener mOnRefreshDataListener = new OnDataLoadedListener() {
            @Override
            public void onDataLoaded(final List<Place> data) {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        if (data != null) {
                            mData.addAll(data);
                            mAdapter.notifyDataSetChanged();
                            updateMap();
                        }
                        mPullToRefreshView.onRefreshComplete();
                    }
                });
            }
        };

        @Override
        public void onRefresh(PullToRefreshBase refreshView) {
            mListener.getDataLoader().getNextDataChunk(mOnRefreshDataListener);
        }
    }
}
