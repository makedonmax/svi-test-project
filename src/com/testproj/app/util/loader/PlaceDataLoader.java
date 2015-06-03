package com.testproj.app.util.loader;

import com.testproj.app.data.Place;

import java.util.List;

public interface PlaceDataLoader {

    interface OnDataLoadedListener {
        void onDataLoaded(List<Place> data);
    }

    void getNextDataChunk(OnDataLoadedListener listener);
    void resetLoader();
}
