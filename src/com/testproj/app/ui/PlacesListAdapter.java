package com.testproj.app.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.testproj.app.R;
import com.testproj.app.data.Place;
import com.testproj.app.util.ImageLoader;

import java.util.List;

public class PlacesListAdapter extends BaseAdapter {

    private ImageLoader mImageLoader;
    private List<Place> mPlacesList;
    private Context mContext;

    public PlacesListAdapter(Context context, List<Place> places, ImageLoader loader) {
        mPlacesList = places;
        mImageLoader = loader;
        mContext = context;
    }

    @Override
    public int getCount() {
        return mPlacesList.size();
    }

    @Override
    public Object getItem(int position) {
        return mPlacesList.get(position);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final PlaceViewHolder placeViewHolder;

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView  = inflater.inflate(R.layout.placecard, parent, false);
            placeViewHolder = new PlaceViewHolder(convertView);
            convertView.setTag(placeViewHolder);
        } else {
            placeViewHolder = (PlaceViewHolder) convertView.getTag();
        }

        placeViewHolder.title.setText(mPlacesList.get(position).getTitle());
        placeViewHolder.street.setText(mPlacesList.get(position).getLocation().getStreet());
        if (mImageLoader != null) {
            mImageLoader.loadBitmap(mPlacesList.get(position).getImageUrl(), placeViewHolder.image);
        }

        return convertView;
    }

    public static class PlaceViewHolder{

        private TextView title;
        private TextView street;
        private ImageView image;

        PlaceViewHolder(View itemView) {
            title = (TextView) itemView.findViewById(R.id.place_title);
            street = (TextView) itemView.findViewById(R.id.place_street);
            image = (ImageView) itemView.findViewById(R.id.place_image);
        }
    }
}
