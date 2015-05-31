package com.testproj.app.ui;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import com.testproj.app.R;
import com.testproj.app.data.Place;
import com.testproj.app.data.PlaceLocation;
import com.testproj.app.util.ImageLoader;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PlacesActivity extends Activity implements PlacesFragment.PlacesFragmentListener {

    private static final String LOG_TAG = PlacesActivity.class.getSimpleName();
    public static final String PLACES_FRAGMENT_TAG = "places_fragment";
    private static ImageLoader mImageLoader;

    private PlacesFragment mPlacesFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Objects.requireNonNull(mImageLoader, "Image loader didn't set. See \"setImageLoader()\" method.");

        setContentView(R.layout.main);

        mPlacesFragment = (PlacesFragment) getFragmentManager().findFragmentByTag(PLACES_FRAGMENT_TAG);
        if (mPlacesFragment == null) {
            mPlacesFragment = new PlacesFragment();
            getFragmentManager().beginTransaction().replace(R.id.mainlayout, mPlacesFragment, PLACES_FRAGMENT_TAG).commit();

            ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected()) {
                String stringUrl = "https://gist.githubusercontent.com/benigeri/1ba45a098aed0b21ae0c/raw/db28f872d6dd59c5766710abc685e01c25a0f020/places1.json";
                new DownloadDataTask().execute(stringUrl);
            } else {
                Toast.makeText(this, "No network connection available.", Toast.LENGTH_LONG);
            }
        }
    }

    @Override
    public ImageLoader getImageLoader() {
        return mImageLoader;
    }

    public static void setImageLoader(ImageLoader loader) {
        PlacesActivity.mImageLoader = loader;
    }

    private class DownloadDataTask extends AsyncTask<String, Void, List<Place>> {

        public static final int READ_TIMEOUT_MILLIS = 10000;
        public static final int CONNECT_TIMEOUT_MILLIS = 15000;

        @Override
        protected List<Place> doInBackground(String... urls) {
            String data = null;
            try {
                data = downloadUrl(urls[0]);
            } catch (IOException e) {
                Log.e(LOG_TAG, "An error occur while downloading data.", e);
            }
            return parseData(data);
        }

        @Override
        protected void onPostExecute(List<Place> result) {
            mPlacesFragment.setData(result);
        }

        private String downloadUrl(String url) throws IOException {
            BufferedReader reader = null;
            HttpURLConnection conn = null;

            try {
                URL reqUrl = new URL(url);
                conn = (HttpURLConnection) reqUrl.openConnection();
                conn.setReadTimeout(READ_TIMEOUT_MILLIS);
                conn.setConnectTimeout(CONNECT_TIMEOUT_MILLIS);
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

        private List<Place> parseData(String data) {
            List<Place> result = new ArrayList<Place>();
            if (data != null) {
                try {
                    JSONObject jsonObj = new JSONObject(data);

                    JSONObject block = jsonObj.getJSONObject("block");
                    JSONArray items = block.getJSONArray("items");

                    for (int i = 0; i < items.length(); i++) {

                        JSONObject item = items.getJSONObject(i);
                        Place place = new Place();

                        place.setId(item.getString("id"));
                        place.setCategory(item.getString("category"));
                        place.setTitle(item.getString("title"));
                        place.setImageUrl(item.getString("image"));

                        JSONObject location = item.getJSONObject("location");
                        PlaceLocation placeLocation = new PlaceLocation();
                        placeLocation.setDistance(location.getDouble("distance"));
                        placeLocation.setLatitude(location.getDouble("latitude"));
                        placeLocation.setLongitude(location.getDouble("longitude"));
                        placeLocation.setStreet(location.getString("street"));

                        place.setLocation(placeLocation);

                        result.add(place);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                Log.e("ServiceHandler", "Couldn't get any data from the url");
            }

            return result;
        }
    }
}
