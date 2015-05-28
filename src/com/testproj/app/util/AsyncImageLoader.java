package com.testproj.app.util;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

public class AsyncImageLoader implements ImageLoader {

    private Resources mResources;
    private BitmapCache mBitmapCache;
    private Bitmap mDefBmp;

    public AsyncImageLoader(Resources resources, BitmapCache cache, int defImageId) {
        mResources = resources;
        mBitmapCache = cache;
        mDefBmp = BitmapFactory.decodeResource(resources, defImageId);
    }

    public void loadBitmap(String url, ImageView imageView) {
        if (cancelPotentialWork(url, imageView)) {
            final BitmapWorkerTask task = new BitmapWorkerTask(imageView, mBitmapCache);
            final AsyncDrawable asyncDrawable = new AsyncDrawable(mResources, mDefBmp, task);
            imageView.setImageDrawable(asyncDrawable);
            task.execute(url);
        }
    }

    private boolean cancelPotentialWork(String url, ImageView imageView) {
        final BitmapWorkerTask bitmapWorkerTask = BitmapWorkerTask.getBitmapWorkerTask(imageView);

        if (bitmapWorkerTask != null) {
            final String bitmapUrl = bitmapWorkerTask.getUrl();
            if (bitmapUrl == null || !bitmapUrl.equals(url)) {
                bitmapWorkerTask.cancel(true);
            } else {
                return false;
            }
        }
        return true;
    }
}
