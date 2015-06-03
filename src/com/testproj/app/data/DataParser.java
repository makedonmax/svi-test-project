package com.testproj.app.data;

import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class DataParser {

    private static String LOG_TAG = DataParser.class.getSimpleName();

    public static List<Place> parseJSON(String data) {
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
                Log.e(LOG_TAG, "An error occur while parsing.", e);
            }
        }
        return result;
    }
}