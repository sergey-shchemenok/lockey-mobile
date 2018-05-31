package ru.tradition.lockeymobile.auth;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.util.Log;

import java.util.Map;

import ru.tradition.lockeymobile.AppData;
import ru.tradition.lockeymobile.DataLoader;
import ru.tradition.lockeymobile.LoadedData;
import ru.tradition.lockeymobile.subscriptions.ActivatingSubscriptionQueryUtils;
import ru.tradition.lockeymobile.subscriptions.DeactivatingSubscriptionQueryUtils;
import ru.tradition.lockeymobile.subscriptions.SubscriptionData;
import ru.tradition.lockeymobile.subscriptions.SubscriptionQueryUtils;
import ru.tradition.lockeymobile.tabs.assetstab.AssetsData;
import ru.tradition.lockeymobile.tabs.assetstab.AssetsQueryUtils;
import ru.tradition.lockeymobile.tabs.maptab.GeofencePolygon;
import ru.tradition.lockeymobile.tabs.maptab.GeofenceQueryUtils;

import static ru.tradition.lockeymobile.AppData.ACTIVATE_SUBSCRIPTION_LOADER_ID;
import static ru.tradition.lockeymobile.AppData.ASSETS_LOADER_ID;
import static ru.tradition.lockeymobile.AppData.ASSETS_REQUEST_URL;
import static ru.tradition.lockeymobile.AppData.DEACTIVATE_SUBSCRIPTION_LOADER_ID;
import static ru.tradition.lockeymobile.AppData.SUBSCRIPTIONS_LOADER_ID;
import static ru.tradition.lockeymobile.AppData.ZONES_LIST_URL;
import static ru.tradition.lockeymobile.AppData.ZONES_LOADER_ID;

public class AuthTokenLoader extends AsyncTaskLoader<String> {

    /**
     * Tag for log messages
     */
    private static final String LOG_TAG = DataLoader.class.getName();

    private String mUrl;
    private String pwd;
    private String usr;

    public AuthTokenLoader(Context context, String url, String pwd, String usr) {
        super(context);
        mUrl = url;
        this.pwd = pwd;
        this.usr = usr;
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
            Log.i(LOG_TAG, "mURL == null");
            return null;
        }
        String message = AuthQueryUtils.fetchAuthData(mUrl, pwd, usr);

        return message;

    }
}
