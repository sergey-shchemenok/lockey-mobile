package ru.tradition.lockeymobile;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.content.Loader;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.net.HttpURLConnection;
import java.util.ArrayList;

import ru.tradition.lockeymobile.tabs.assetstab.AssetsData;
import ru.tradition.lockeymobile.tabs.assetstab.AssetsDataAdapter;
import ru.tradition.lockeymobile.tabs.assetstab.AssetsFragmentTab;
import ru.tradition.lockeymobile.tabs.maptab.GeofenceQueryUtils;

import static ru.tradition.lockeymobile.AppData.SUBSCRIPTIONS_LOADER_ID;
import static ru.tradition.lockeymobile.AppData.mAssetMap;


public class SubscriptionsActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<LoadedData> {

    private static final String LOG_TAG = SubscriptionsActivity.class.getSimpleName();

    private Toolbar toolbar;

    private ConnectivityManager connectivityManager;
    private NetworkInfo activeNetwork;
    private LoaderManager loaderManager;
    private ProgressBar progressCircle;
    private TextView infoMessage;
    private TextView mEmptyStateTextView;

    private SubscriptionDataAdapter subscriptionDataAdapter;
    private ListView subscriptionsListView;

    private static Menu mMenu;
    private static boolean itemSelected = false;
    private static boolean subscribedItemSelected = false;


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

        subscriptionsListView = (ListView) findViewById(R.id.subscriptions_list);
        subscriptionDataAdapter = new SubscriptionDataAdapter(this, new ArrayList<SubscriptionData>());
        subscriptionsListView.setAdapter(subscriptionDataAdapter);

        subscriptionsListView.setEmptyView(mEmptyStateTextView);

        subscriptionsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                SubscriptionData sd = (SubscriptionData) adapterView.getItemAtPosition(position);
                int sid = sd.getSid();
                if (!AppData.selectedSubscription.contains(sid)) {
                    AppData.selectedSubscription.clear();
                    AppData.selectedSubscription.add(sid);
                    subscriptionDataAdapter.notifyDataSetChanged();
                    if (sd.isSubscribed()) {
                        subscribedItemSelected = true;
                        mMenu.getItem(2).setVisible(false);
                        mMenu.getItem(3).setVisible(true);
                    } else {
                        itemSelected = true;
                        mMenu.getItem(2).setVisible(true);
                        mMenu.getItem(3).setVisible(false);
                    }
                } else {
                    AppData.selectedSubscription.clear();
                    subscriptionDataAdapter.notifyDataSetChanged();
                    itemSelected = false;
                    subscribedItemSelected = false;
                    mMenu.getItem(2).setVisible(false);
                    mMenu.getItem(3).setVisible(false);
                }
            }
        });

        //launch loading data from server
        startLoader();

    }

    //For data loading
    public void startLoader() {
        Log.i(LOG_TAG, "....startSuscriptionLoader....");
        activeNetwork = connectivityManager.getActiveNetworkInfo();
        if (activeNetwork != null && activeNetwork.isConnected()) {
            loaderManager = getLoaderManager();
            loaderManager.initLoader(AppData.SUBSCRIPTIONS_LOADER_ID, null, this);
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
        return new DataLoader(this, AppData.SUBSCRIPTIONS_LIST_URL, SUBSCRIPTIONS_LOADER_ID);
    }

    @Override
    public void onLoadFinished(android.content.Loader<LoadedData> loader, LoadedData loadedData) {
        mEmptyStateTextView.setText(R.string.no_subscriptions);
        progressCircle.setVisibility(View.GONE);

        subscriptionDataAdapter.clear();
        if (loadedData.getSubscriptionMap() == null || loadedData.getSubscriptionMap().isEmpty()) {
            return;
        }
        Log.v(LOG_TAG, "onLoadFinished");
        subscriptionDataAdapter.addAll(new ArrayList<>(loadedData.getSubscriptionMap().values()));
    }

    @Override
    public void onLoaderReset(android.content.Loader<LoadedData> loader) {
        subscriptionDataAdapter.clear();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_subscription, menu);
        mMenu = menu;
        if (!subscribedItemSelected)
            mMenu.getItem(3).setVisible(false);
        if (!itemSelected)
            mMenu.getItem(2).setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            case R.id.subscription_menu_logout:
                logout();
                return true;
            case R.id.subscription_menu_settings:
                Intent settingsIntent = new Intent(this, SettingsActivity.class);
                startActivity(settingsIntent);
                return true;
            case R.id.subscription_menu_subscribe:

                return true;
            case R.id.subscription_menu_unsubscribe:

                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void logout() {
        Intent intent = new Intent(this, AuthActivity.class);
        AppData.isFinished = false;
        AppData.isRepeated = false;
        startActivity(intent);
    }

    @Override
    public boolean onSupportNavigateUp() {
        super.onBackPressed();
        AppData.selectedSubscription.clear();
        subscriptionDataAdapter.notifyDataSetChanged();
        subscribedItemSelected = false;
        itemSelected = false;
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        AppData.selectedSubscription.clear();
        subscriptionDataAdapter.notifyDataSetChanged();
        subscribedItemSelected = false;
        itemSelected = false;
    }
}
