package com.testproj.app.util;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.widget.ImageView;

import java.lang.ref.WeakReference;

public class BitmapWorkerTask extends AsyncTask<String, Void, Bitmap> {
    private static final String LOG_TAG = BitmapWorkerTask.class.getSimpleName();

    private final WeakReference<ImageView> mImageViewReference;
    private String mUrl;
    private BitmapCache mBitmapCache;

    public BitmapWorkerTask(ImageView imageView, BitmapCache cache) {
        // Use a WeakReference to ensure the ImageView can be garbage collected
        mImageViewReference = new WeakReference<ImageView>(imageView);
        mBitmapCache = cache;
    }

    // Decode image in background.
    @Override
    protected Bitmap doInBackground(String... params) {
        mUrl = params[0];

        Bitmap bitmap = mBitmapCache.getBitmapFromCache(mUrl);
        if (bitmap == null) {
            bitmap = Utils.downloadBitmapByUrl(mUrl);
            if (bitmap != null) {
                mBitmapCache.addBitmapToCache(mUrl, bitmap);
            }
        }
        return bitmap;
    }

    // Once complete, see if ImageView is still around and set bitmap.
    @Override
    protected void onPostExecute(Bitmap bitmap) {
        if (isCancelled()) {
            bitmap = null;
        }
        if (mImageViewReference != null && bitmap != null) {
            final ImageView imageView = mImageViewReference.get();
            final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);
            if (this == bitmapWorkerTask && imageView != null) {
                imageView.setImageBitmap(bitmap);
            }
        }
    }

    public String getUrl() {
        return mUrl;
    }

    public static BitmapWorkerTask getBitmapWorkerTask(ImageView imageView) {
        if (imageView != null) {
            final Drawable drawable = imageView.getDrawable();
            if (drawable instanceof AsyncDrawable) {
                final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
                return asyncDrawable.getBitmapWorkerTask();
            }
        }
        return null;
    }
}