package com.testproj.app.util.cache;

import android.graphics.Bitmap;
import android.util.LruCache;
import com.testproj.app.util.cache.BitmapCache;

public class MemoryCache implements BitmapCache {

    private LruCache<String, Bitmap> mMemoryCache;

    public MemoryCache() {
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        final int cacheSize = maxMemory / 8;

        mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                return bitmap.getByteCount() / 1024;
            }
        };
    }

    @Override
    public void addBitmapToCache(String data, Bitmap value) {
        if (getBitmapFromCache(data) == null) {
            mMemoryCache.put(data, value);
        }
    }

    @Override
    public Bitmap getBitmapFromCache(String data) {
        return mMemoryCache.get(data);
    }

    @Override
    public void flush() {

    }

    @Override
    public void close() {

    }
}
