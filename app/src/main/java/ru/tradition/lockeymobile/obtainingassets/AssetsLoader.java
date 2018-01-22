package ru.tradition.lockeymobile.obtainingassets;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.util.Log;

import java.util.List;

/**
 * Created by Caelestis on 22.01.2018.
 */

public class AssetsLoader extends AsyncTaskLoader<List<AssetsData>> {

    /** Tag for log messages */
    private static final String LOG_TAG = AssetsLoader.class.getName();

    /** Query URL */
    private String mUrl;

    /**
     * Constructs a new {@link AssetsLoader}.
     *
     * @param context of the activity
     * @param url to load data from
     */
    public AssetsLoader(Context context, String url) {
        super(context);
        mUrl = url;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
        Log.v(LOG_TAG, "onStartLoading");

    }

    /**
     * This is on a background thread.
     */
    @Override
    public List<AssetsData> loadInBackground() {
        if (mUrl == null) {
            return null;
        }

        Log.v(LOG_TAG, "loadInBackground");

        // Perform the network request, parse the response, and extract a list of assets.
        List<AssetsData> assetsList = AssetsQueryUtils.fetchAssetsData(mUrl);
        return assetsList;
    }
}
