package ru.tradition.lockeymobile.obtainingassets;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;

/**
 * Created by Caelestis on 22.01.2018.
 */

public class AssetsLoader extends AsyncTaskLoader<Map<Integer, AssetsData>> {

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
    public Map<Integer, AssetsData> loadInBackground() {
        if (mUrl == null) {
            return null;
        }

        Log.v(LOG_TAG, "loadInBackground");

        // Perform the network request, parse the response, and extract a list of assets.
        Map<Integer, AssetsData> assetsList = AssetsQueryUtils.fetchAssetsData(mUrl);
        return assetsList;
    }
}
