package com.testproj.app.ui;

import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;
import com.testproj.app.R;
import com.testproj.app.util.ImageLoader;

import java.util.Objects;

public class PlaceInfoActivity extends Activity {

    public static final String TITLE_INTENT_KEY = "title";
    public static final String IMAGE_URL_INTENT_KEY = "image_url";
    private static ImageLoader mImageLoader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Objects.requireNonNull(mImageLoader, "Image loader didn't set. See \"setImageLoader()\" method.");

        setContentView(R.layout.placeinfo);

        getActionBar().setDisplayHomeAsUpEnabled(true);

        String title = getIntent().getStringExtra(TITLE_INTENT_KEY);
        if (title != null) {
            getActionBar().setTitle(title);
        }

        String imageUrl = getIntent().getStringExtra(IMAGE_URL_INTENT_KEY);
        ImageView imageView = (ImageView) findViewById(R.id.image);
        if (imageUrl != null) {
            mImageLoader.loadBitmap(imageUrl, imageView);
        }
        else {
            imageView.setScaleType(ImageView.ScaleType.CENTER);
            imageView.setImageResource(R.drawable.no_image);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                PlaceInfoActivity.this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static void setImageLoader(ImageLoader loader) {
        PlaceInfoActivity.mImageLoader = loader;
    }
}
