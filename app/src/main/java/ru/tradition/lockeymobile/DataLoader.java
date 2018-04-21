package ru.tradition.lockeymobile;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.util.Log;

import java.util.Map;

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
import static ru.tradition.lockeymobile.AppData.DEACTIVATE_SUBSCRIPTION_LOADER_ID;
import static ru.tradition.lockeymobile.AppData.SUBSCRIPTIONS_LOADER_ID;
import static ru.tradition.lockeymobile.AppData.ZONES_LIST_URL;
import static ru.tradition.lockeymobile.AppData.ZONES_LOADER_ID;

/**
 * Created by Caelestis on 11.04.2018.
 */

public class DataLoader extends AsyncTaskLoader<LoadedData> {

    /**
     * Tag for log messages
     */
    private static final String LOG_TAG = DataLoader.class.getName();

    private int loaderID;

    /**
     * Query URL
     */
    private String mUrl;

    //for subscribing
    private int sid;

    /**
     * Constructs a new {@link DataLoader}.
     *
     * @param context of the activity
     * @param url     to load data from
     */
    public DataLoader(Context context, String url, int loaderID) {
        super(context);
        mUrl = url;
        this.loaderID = loaderID;
    }

    public DataLoader(Context context, String url, int loaderID, int sid) {
        super(context);
        mUrl = url;
        this.loaderID = loaderID;
        this.sid = sid;
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
    public LoadedData loadInBackground() {
        if (mUrl == null) {
            return null;
        }
        switch (loaderID) {
            case ASSETS_LOADER_ID:
                Log.v(LOG_TAG, "loadInBackground");
                // Perform the network request, parse the response, and extract a list of assets.
                Map<Integer, AssetsData> assetsMap = AssetsQueryUtils.fetchAssetsData(mUrl);
                return new LoadedData(assetsMap, null, null);

            case ZONES_LOADER_ID:
                Map<Integer, GeofencePolygon> polygonsMap = GeofenceQueryUtils.fetchZonesData(mUrl);
                return new LoadedData(null, polygonsMap, null);

            case SUBSCRIPTIONS_LOADER_ID:
                AppData.mPolygonsMap = GeofenceQueryUtils.fetchZonesData(ZONES_LIST_URL);
                Map<Integer, SubscriptionData> subscriptionsMap = SubscriptionQueryUtils.fetchSubscriptionsData(mUrl);
                return new LoadedData(null, null, subscriptionsMap);

            case ACTIVATE_SUBSCRIPTION_LOADER_ID:
                String responseMessage = ActivatingSubscriptionQueryUtils.fetchResponseMessage(mUrl, sid);
                return new LoadedData(responseMessage, sid);


            case DEACTIVATE_SUBSCRIPTION_LOADER_ID:
                String responseMessageD = DeactivatingSubscriptionQueryUtils.fetchResponseMessage(mUrl, sid);
                return new LoadedData(responseMessageD, sid);
            default:
                return null;

        }
    }

}
