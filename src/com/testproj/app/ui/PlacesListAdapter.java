package com.testproj.app.ui;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.testproj.app.R;
import com.testproj.app.data.Place;
import com.testproj.app.util.ImageLoader;

import java.util.List;

class PlacesListAdapter extends RecyclerView.Adapter<PlacesListAdapter.PlaceViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    private OnItemClickListener mItemClickListener;
    private ImageLoader mImageLoader;
    private List<Place> mPlacesList;

    public PlacesListAdapter(List<Place> places, ImageLoader loader, OnItemClickListener clickListener) {
        mPlacesList = places;
        mImageLoader = loader;
        mItemClickListener = clickListener;
    }

    @Override
    public PlaceViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.placecard, viewGroup, false);
        return new PlaceViewHolder(v, mItemClickListener);
    }

    @Override
    public void onBindViewHolder(PlaceViewHolder placeViewHolder, int i) {
        placeViewHolder.title.setText(mPlacesList.get(i).getTitle());
        placeViewHolder.street.setText(mPlacesList.get(i).getLocation().getStreet());
        if (mImageLoader != null) {
            mImageLoader.loadBitmap(mPlacesList.get(i).getImageUrl(), placeViewHolder.image);
        }
    }

    @Override
    public int getItemCount() {
        return mPlacesList.size();
    }

    public static class PlaceViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private OnItemClickListener mClickListener;
        private TextView title;
        private TextView street;
        private ImageView image;

        PlaceViewHolder(View itemView, OnItemClickListener listener) {
            super(itemView);
            mClickListener = listener;
            title = (TextView) itemView.findViewById(R.id.place_title);
            street = (TextView) itemView.findViewById(R.id.place_street);
            image = (ImageView) itemView.findViewById(R.id.place_image);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) {
                mClickListener.onItemClick(getAdapterPosition());
            }
        }
    }
}