package com.testproj.app.ui;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import com.testproj.app.R;
import com.testproj.app.data.DataParser;
import com.testproj.app.data.Place;
import com.testproj.app.util.Utils;
import com.testproj.app.util.loader.ImageLoader;
import com.testproj.app.util.loader.PlaceDataLoader;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Objects;

public class PlacesActivity extends Activity implements PlacesFragment.PlacesFragmentListener,
                                                        PlaceDataLoader {

    private static final String LOG_TAG = PlacesActivity.class.getSimpleName();
    public static final String PLACES_FRAGMENT_TAG = "places_fragment";
    public static final String URL_KEY = "url";
    public static final String DATA_IND_KEY = "data_ind";
    private static ImageLoader mImageLoader;

    private static final String[] mDataLinks = {"https://gist.githubusercontent.com/benigeri/1ba45a098aed0b21ae0c/raw/db28f872d6dd59c5766710abc685e01c25a0f020/places1.json",
                                        "https://gist.githubusercontent.com/benigeri/1ba45a098aed0b21ae0c/raw/1fccaeb4fefc105ed2d0430eea80ede57fe2a6e9/places2.json"};
    private int mDataInd = 0;

    private PlacesFragment mPlacesFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Objects.requireNonNull(mImageLoader, "Image loader didn't set. See \"setImageLoader()\" method.");

        setContentView(R.layout.main);

        if (savedInstanceState != null) {
            mDataInd = savedInstanceState.getInt(DATA_IND_KEY);
        }

        mPlacesFragment = (PlacesFragment) getFragmentManager().findFragmentByTag(PLACES_FRAGMENT_TAG);
        if (mPlacesFragment == null) {
            mPlacesFragment = new PlacesFragment();
            getFragmentManager().beginTransaction().replace(R.id.mainlayout, mPlacesFragment, PLACES_FRAGMENT_TAG).commit();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(DATA_IND_KEY, mDataInd);
    }

    @Override
    public ImageLoader getImageLoader() {
        return mImageLoader;
    }

    @Override
    public PlaceDataLoader getDataLoader() {
        return this;
    }

    public static void setImageLoader(ImageLoader loader) {
        PlacesActivity.mImageLoader = loader;
    }

    @Override
    public void getNextDataChunk(OnDataLoadedListener listener) {
        if (listener != null) {
            if (mDataInd < mDataLinks.length) {
                new Thread(new DownloadDataTask(mDataLinks[mDataInd], listener)).start();
                mDataInd++;
            } else {
                listener.onDataLoaded(null);
            }
        }
    }

    @Override
    public void resetLoader() {
        mDataInd = 0;
    }

    private static class DownloadDataTask implements Runnable {

        private static final String LOG_TAG = DownloadDataTask.class.getSimpleName();
        public static final int READ_TIMEOUT_MILLIS = 10000;
        public static final int CONNECT_TIMEOUT_MILLIS = 15000;

        private String mUrl;
        private WeakReference<OnDataLoadedListener> mListener;

        public DownloadDataTask(String url, OnDataLoadedListener listener) {
            mUrl = url;
            mListener = new WeakReference<OnDataLoadedListener>(listener);
        }

        @Override
        public void run() {
            String data = null;
            try {
                data = Utils.downloadStringByUrl(mUrl, READ_TIMEOUT_MILLIS, CONNECT_TIMEOUT_MILLIS);
            } catch (IOException e) {
                Log.e(LOG_TAG, "An error occur while downloading data.", e);
            }
            List<Place> res = DataParser.parseJSON(data);
            OnDataLoadedListener listener = mListener.get();
            if (listener != null) {
                listener.onDataLoaded(res);
            }
        }
    }
}
