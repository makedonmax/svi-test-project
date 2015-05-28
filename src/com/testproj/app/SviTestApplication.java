package com.testproj.app;

import android.app.Application;
import com.testproj.app.ui.PlacesActivity;
import com.testproj.app.ui.PlaceInfoActivity;
import com.testproj.app.util.AsyncImageLoader;
import com.testproj.app.util.BitmapCache;
import com.testproj.app.util.DiskBitmapCache;
import com.testproj.app.util.ImageLoader;

public class SviTestApplication extends Application {

    private ImageLoader mImageLoader;
    private BitmapCache mCache;

    @Override
    public void onCreate() {
        super.onCreate();

        mCache = new DiskBitmapCache(getApplicationContext());
        mImageLoader = new AsyncImageLoader(getResources(), mCache, R.drawable.no_image);
        initActivities();
    }

    private void initActivities() {
        PlacesActivity.setImageLoader(mImageLoader);
        PlaceInfoActivity.setImageLoader(mImageLoader);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();

        mCache.flush();
    }
}
