package com.testproj.app.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class Utils {

    public static final String LOG_TAG = Utils.class.getSimpleName();
    private static final int IO_BUFFER_SIZE = 8 * 1024;

    public static Bitmap downloadBitmapByUrl(String urlString) {
        HttpURLConnection urlConnection = null;
        BufferedInputStream in = null;
        Bitmap bmp = null;

        try {
            final URL url = new URL(urlString);
            urlConnection = (HttpURLConnection) url.openConnection();
            in = new BufferedInputStream(urlConnection.getInputStream(), IO_BUFFER_SIZE);

            bmp = BitmapFactory.decodeStream(in);

        } catch (final IOException e) {
            Log.e(LOG_TAG, "Error in downloadBitmap - " + e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            try {
                if (in != null) {
                    in.close();
                }
            } catch (final IOException e) {
            }
        }
        return bmp;
    }

    public static String bytesToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(0xFF & bytes[i]);
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }

    public static String downloadStringByUrl(String url, int readTimeoutMs, int connectTimeoutMs) throws IOException {
        BufferedReader reader = null;
        HttpURLConnection conn = null;

        try {
            URL reqUrl = new URL(url);
            conn = (HttpURLConnection) reqUrl.openConnection();
            conn.setReadTimeout(readTimeoutMs);
            conn.setConnectTimeout(connectTimeoutMs);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            conn.connect();
            int response = conn.getResponseCode();
            switch (response) {
                case HttpURLConnection.HTTP_OK:
                case HttpURLConnection.HTTP_CREATED: {
                    reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line).append('\n');
                    }
                    return sb.toString();
                }
            }

        } finally {
            if (reader != null) {
                reader.close();
            }
            if (conn != null) {
                conn.disconnect();
            }
        }
        return null;
    }
}
