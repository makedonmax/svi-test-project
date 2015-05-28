package com.testproj.app.util;

import android.graphics.Bitmap;

public interface BitmapCache {
    void addBitmapToCache(String data, Bitmap value);
    Bitmap getBitmapFromCache(String data);
    void flush();
    void close();
}
