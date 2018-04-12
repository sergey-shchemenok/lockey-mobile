package ru.tradition.lockeymobile;

import android.app.LoaderManager;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.content.Loader;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import static ru.tradition.lockeymobile.AppData.SUBSCRIPTIONS_LOADER_ID;


public class SubscriptionsActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<LoadedData>{

    private static final String LOG_TAG = SubscriptionsActivity.class.getSimpleName();

    private Toolbar toolbar;

    private ConnectivityManager connectivityManager;
    private NetworkInfo activeNetwork;
    private LoaderManager loaderManager;
    private ProgressBar progressCircle;
    private TextView infoMessage;
    private TextView mEmptyStateTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subscriptions);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //to add up button
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);

        }

        toolbar.setTitle(R.string.subscriptions_activity_title);

        progressCircle = (ProgressBar) findViewById(R.id.subscription_loading_spinner);
        mEmptyStateTextView = (TextView) findViewById(R.id.subscription_empty_view);
        infoMessage = (TextView) findViewById(R.id.subscription_info_message);
        infoMessage.setVisibility(View.GONE);
        progressCircle.setVisibility(View.VISIBLE);

        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        //launch loading data from server
        startLoader();

    }


    //For data loading
    public void startLoader() {
        Log.i(LOG_TAG, "....startSuscriptionLoader....");
        activeNetwork = connectivityManager.getActiveNetworkInfo();
        if (activeNetwork != null && activeNetwork.isConnected()) {
            loaderManager = getLoaderManager();
            loaderManager.initLoader(AppData.ASSETS_LOADER_ID, null, this);
            Log.i(LOG_TAG, "initSubscritpionLoader");
        } else {
            infoMessage.setVisibility(View.VISIBLE);
            infoMessage.setText(R.string.no_connection);
            //todo repeat loader
        }
    }

    @Override
    public Loader<LoadedData> onCreateLoader(int loaderId, Bundle bundle) {
        Log.i(LOG_TAG, "onCreateLoader");
        return new DataLoader(this, AppData.ASSETS_REQUEST_URL, SUBSCRIPTIONS_LOADER_ID);
    }

    @Override
    public void onLoadFinished(android.content.Loader<LoadedData> loader, LoadedData data) {

    }

    @Override
    public void onLoaderReset(android.content.Loader<LoadedData> loader) {

    }


}
