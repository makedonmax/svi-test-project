package com.testproj.app.ui;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.testproj.app.util.AsyncDrawable;
import com.testproj.app.util.BitmapWorkerTask;
import com.testproj.app.R;
import com.testproj.app.data.Place;
import com.testproj.app.util.BitmapCache;

import java.util.List;

class PlacesListAdapter extends RecyclerView.Adapter<PlacesListAdapter.PersonViewHolder> {

    private Resources mResources;
    private List<Place> mPlacesList;
    private Bitmap mDefBmp;
    private BitmapCache mBitmapCache;

    public PlacesListAdapter(Resources resources, List<Place> places, BitmapCache cache) {
        mResources = resources;
        mPlacesList = places;
        mDefBmp = BitmapFactory.decodeResource(mResources, R.drawable.ic_launcher);
        mBitmapCache = cache;
    }

    @Override
    public PersonViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.placecard, viewGroup, false);
        return new PersonViewHolder(v);
    }

    @Override
    public void onBindViewHolder(PersonViewHolder personViewHolder, int i) {
        personViewHolder.personName.setText(mPlacesList.get(i).getTitle());
        personViewHolder.personAge.setText(mPlacesList.get(i).getLocation().getStreet());
        loadBitmap(mPlacesList.get(i).getImageUrl(), personViewHolder.personPhoto);
    }

    @Override
    public int getItemCount() {
        return mPlacesList.size();
    }

    public static class PersonViewHolder extends RecyclerView.ViewHolder {
        TextView personName;
        TextView personAge;
        ImageView personPhoto;

        PersonViewHolder(View itemView) {
            super(itemView);
            personName = (TextView) itemView.findViewById(R.id.person_name);
            personAge = (TextView) itemView.findViewById(R.id.person_age);
            personPhoto = (ImageView) itemView.findViewById(R.id.person_photo);
        }
    }

    private void loadBitmap(String url, ImageView imageView) {
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