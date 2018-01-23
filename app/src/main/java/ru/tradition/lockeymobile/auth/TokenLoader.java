package ru.tradition.lockeymobile.auth;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.util.Log;


/**
 * Created by Caelestis on 23.01.2018.
 */

public class TokenLoader extends AsyncTaskLoader<String> {

    /** Tag for log messages */
    private static final String LOG_TAG = TokenLoader.class.getName();

    /** Query URL */
    private String mUrl;

    private String mPwd;
    private String mUsr;


    /**
     * Constructs a new {@link TokenLoader}.
     *
     * @param context of the activity
     * @param url to load data from
     */
    public TokenLoader(Context context, String url, String pwd, String usr) {
        super(context);
        mUrl = url;
        mPwd = pwd;
        mUsr = usr;
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
    public String loadInBackground() {
        if (mUrl == null) {
            return null;
        }

        Log.v(LOG_TAG, "loadInBackground");

        // Perform the network request, parse the response, and extract a list of assets.
        String message = AuthQueryUtils.fetchAuthData(mUrl, mPwd, mUsr);
        return message;
    }
}
