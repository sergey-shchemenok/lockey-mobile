package ru.tradition.lockeymobile;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import ru.tradition.lockeymobile.subscriptions.ActivatingSubscriptionQueryUtils;
import ru.tradition.lockeymobile.subscriptions.DeactivatingSubscriptionQueryUtils;
import ru.tradition.lockeymobile.subscriptions.SubscriptionData;
import ru.tradition.lockeymobile.subscriptions.SubscriptionDataAdapter;
import ru.tradition.lockeymobile.subscriptions.SubscriptionQueryUtils;

import static ru.tradition.lockeymobile.AppData.ACTIVATE_SUBSCRIPTION_LOADER_ID;
import static ru.tradition.lockeymobile.AppData.DEACTIVATE_SUBSCRIPTION_LOADER_ID;
import static ru.tradition.lockeymobile.AppData.SUBSCRIPTIONS_LOADER_ID;


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

    //Contains subscription SID
    private static int mSID = -1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //go to auth activity. It need to prevent seeing the internal information without authorization
        if (AppData.usr.equals("") || AppData.pwd.equals("") || AppData.isAuthorized == false) {
            Intent intent = new Intent(this, AuthActivity.class);
            startActivity(intent);
        }


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
                    mSID = sid;
                    subscriptionDataAdapter.notifyDataSetChanged();
                    if (AppData.activatingSubscription.contains(mSID) ||
                            AppData.deactivatingSubscription.contains(mSID)) {
                        mMenu.getItem(2).setVisible(false);
                        mMenu.getItem(3).setVisible(false);
                        return;
                    }

                    if (sd.isSubscribed()) {
                        subscribedItemSelected = true;
                        itemSelected = false;
                        mMenu.getItem(2).setVisible(false);
                        mMenu.getItem(3).setVisible(true);
                    } else {
                        itemSelected = true;
                        subscribedItemSelected = false;
                        mMenu.getItem(2).setVisible(true);
                        mMenu.getItem(3).setVisible(false);
                    }
                } else {
                    AppData.selectedSubscription.clear();
                    mSID = -1;
                    subscriptionDataAdapter.notifyDataSetChanged();
                    itemSelected = false;
                    subscribedItemSelected = false;
                    mMenu.getItem(2).setVisible(false);
                    mMenu.getItem(3).setVisible(false);
                }
            }
        });

        //launch loading data from server
        getSubscriptions();

    }

    @Override
    protected void onResume() {
        super.onResume();
        startUpdater();
    }

    public void getSubscriptions() {
        Log.i(LOG_TAG, "....startSubscriptionLoader....");
        activeNetwork = connectivityManager.getActiveNetworkInfo();
        if (activeNetwork != null && activeNetwork.isConnected()) {
            loaderManager = getLoaderManager();
            loaderManager.initLoader(AppData.SUBSCRIPTIONS_LOADER_ID, null, this);
            Log.i(LOG_TAG, "initSubscritpionLoader");
        } else {
            infoMessage.setVisibility(View.VISIBLE);
            infoMessage.setText(R.string.no_connection);
        }
    }

    public synchronized void activateSubscription() {
        Log.i(LOG_TAG, "....start activating subscription....");
        activeNetwork = connectivityManager.getActiveNetworkInfo();
        if (activeNetwork != null && activeNetwork.isConnected()) {
            AppData.activatingSubscription.add(mSID);
            loaderManager = getLoaderManager();
            loaderManager.initLoader(AppData.ACTIVATE_SUBSCRIPTION_LOADER_ID, null, this);
            Log.i(LOG_TAG, "init activating subscription");
        } else {
            infoMessage.setVisibility(View.VISIBLE);
            infoMessage.setText(R.string.no_connection);
        }
    }

    public synchronized void deactivateSubscription() {
        Log.i(LOG_TAG, "....start deactivating subscription....");
        activeNetwork = connectivityManager.getActiveNetworkInfo();
        if (activeNetwork != null && activeNetwork.isConnected()) {
            AppData.deactivatingSubscription.add(mSID);
            loaderManager = getLoaderManager();
            loaderManager.initLoader(DEACTIVATE_SUBSCRIPTION_LOADER_ID, null, this);
            Log.i(LOG_TAG, "init deactivating subscription");
        } else {
            infoMessage.setVisibility(View.VISIBLE);
            infoMessage.setText(R.string.no_connection);
        }
    }

    @Override
    public Loader<LoadedData> onCreateLoader(int loaderId, Bundle bundle) {
        Log.i(LOG_TAG, "onCreateLoader");
        switch (loaderId) {
            case SUBSCRIPTIONS_LOADER_ID:
                return new DataLoader(this, AppData.SUBSCRIPTIONS_LIST_URL, SUBSCRIPTIONS_LOADER_ID);
            case ACTIVATE_SUBSCRIPTION_LOADER_ID:
                return new DataLoader(this, AppData.ACTIVATE_SUBSCRIPTION_REQUEST_URL, ACTIVATE_SUBSCRIPTION_LOADER_ID, mSID);
            case DEACTIVATE_SUBSCRIPTION_LOADER_ID:
                return new DataLoader(this, AppData.DEACTIVATE_SUBSCRIPTION_REQUEST_URL, DEACTIVATE_SUBSCRIPTION_LOADER_ID, mSID);
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(android.content.Loader<LoadedData> loader, LoadedData loadedData) {
        mEmptyStateTextView.setText(R.string.no_subscriptions);
        mEmptyStateTextView.setGravity(Gravity.CENTER);
//        mEmptyStateTextView.setText("Нет подписок.\nСоздайте подписки на портале my.lockey.ru\nПри создании подписки укажите способ доставки Push");

        progressCircle.setVisibility(View.GONE);
        loaderManager.destroyLoader(AppData.SUBSCRIPTIONS_LOADER_ID);
        loaderManager.destroyLoader(AppData.ACTIVATE_SUBSCRIPTION_LOADER_ID);
        loaderManager.destroyLoader(AppData.DEACTIVATE_SUBSCRIPTION_LOADER_ID);

        Log.i(LOG_TAG, "activatingSubscriptionUrlResponseCode " + ActivatingSubscriptionQueryUtils.activatingSubscriptionUrlResponseCode);
        Log.i(LOG_TAG, "deactivatingSubscriptionUrlResponseCode " + DeactivatingSubscriptionQueryUtils.deactivatingSubscriptionUrlResponseCode);


        if (ActivatingSubscriptionQueryUtils.activatingSubscriptionUrlResponseCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
            AppData.needToken = true;
            Log.i(LOG_TAG, "reactivating...");
            mSID = loadedData.getSid();
            activateSubscription();
            mSID = -1;
            return;
        }
        //whether it can be authorized. The token has not expired
        if (DeactivatingSubscriptionQueryUtils.deactivatingSubscriptionUrlResponseCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
            AppData.needToken = true;
            Log.i(LOG_TAG, "redeactivating...");
            mSID = loadedData.getSid();
            deactivateSubscription();
            mSID = -1;
            return;
        }
        if (SubscriptionQueryUtils.subscriptionsUrlResponseCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
            AppData.needToken = true;
//            getSubscriptions();
//            return;
        }

        if (loadedData.getResponseMessage() != null && !loadedData.getResponseMessage().isEmpty()) {
            Log.i(LOG_TAG, "Response Message:   " + loadedData.getResponseMessage() + " sid: " + loadedData.getSid());
            if (loadedData.getResponseMessage().equals("OK"))
                return;
            else {
                Toast.makeText(this, "Ошибка: " + loadedData.getResponseMessage(), Toast.LENGTH_LONG).show();
                if (AppData.deactivatingSubscription.contains(loadedData.getSid())) {
                    AppData.deactivatingSubscription.remove(loadedData.getSid());
                } else if (AppData.activatingSubscription.contains(loadedData.getSid())) {
                    AppData.activatingSubscription.remove(loadedData.getSid());
                }

            }
            return;
//                if (AppData.deactivatingSubscription.contains(loadedData.getSid())){
//                    mSID = loadedData.getSid();
//                    deactivateSubscription();
//                    mSID = -1;
//                } else if (AppData.activatingSubscription.contains(loadedData.getSid())){
//                    mSID = loadedData.getSid();
//                    activateSubscription();
//                    mSID = -1;
//                }
//                return;
//            }
        }


        if (loadedData.getSubscriptionMap() == null || loadedData.getSubscriptionMap().isEmpty()) {
            return;
        }
//        int f = subscriptionsListView.getFirstVisiblePosition();
//        subscriptionsListView.setSelectionFromTop(f,0);

        AppData.mSubscriptionsMap = loadedData.getSubscriptionMap();
        infoMessage.setVisibility(View.GONE);
        Log.v(LOG_TAG, "onLoadFinished");
        subscriptionDataAdapter.swapItems(new ArrayList<>(AppData.mSubscriptionsMap.values()));
        //subscriptionDataAdapter.notifyDataSetChanged();
        //subscriptionDataAdapter.addAll(new ArrayList<>(AppData.mSubscriptionsMap.values()));

    }

    @Override
    public void onLoaderReset(android.content.Loader<LoadedData> loader) {
        //subscriptionDataAdapter.clear();
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
        if (AppData.activatingSubscription.contains(mSID) ||
                AppData.deactivatingSubscription.contains(mSID)) {
            mMenu.getItem(2).setVisible(false);
            mMenu.getItem(3).setVisible(false);
        }

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
                if (!AppData.activatingSubscription.contains(mSID)) {
                    if (mSID != -1) {
                        activateSubscription();
                        AppData.selectedSubscription.clear();
                        mSID = -1;
                        mMenu.getItem(2).setVisible(false);
                        itemSelected = false;
                        subscribedItemSelected = false;
                        subscriptionDataAdapter.notifyDataSetChanged();
                    }
                }
                return true;
            case R.id.subscription_menu_unsubscribe:
                if (!AppData.deactivatingSubscription.contains(mSID)) {
                    if (mSID != -1) {
                        deactivateSubscription();
                        AppData.selectedSubscription.clear();

                        mSID = -1;
                        mMenu.getItem(3).setVisible(false);
                        itemSelected = false;
                        subscribedItemSelected = false;
                        subscriptionDataAdapter.notifyDataSetChanged();
                    }
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void logout() {
        Intent intent = new Intent(this, AuthActivity.class);
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


    //let's check up the Timer
    private Timer updaterTimer;
    private SubscriptionListUpdater mUpdater;

    private void startUpdater() {
        if (updaterTimer != null) {
            updaterTimer.cancel();
        }
        // re-schedule timer here otherwise, IllegalStateException of "TimerTask is scheduled already" will be thrown
        updaterTimer = new Timer();
        mUpdater = new SubscriptionListUpdater();
        updaterTimer.scheduleAtFixedRate(mUpdater, 5000, 5000);
    }

    class SubscriptionListUpdater extends TimerTask {

        private synchronized void processMenu() {
            for (int x : AppData.activatingSubscription) {
                Log.i(LOG_TAG, "activation value of " + x);
                if (AppData.mSubscriptionsMap.get(x) == null) {
                    Log.i(LOG_TAG, "activation broken " + x);
                    break;
                }
                if (AppData.mSubscriptionsMap.get(x).isSubscribed()) {
                    AppData.activatingSubscription.remove(x);
                    if (mSID == x && mMenu != null) {
                        mMenu.getItem(3).setVisible(true);
                        subscribedItemSelected = true;
                    }
                }
            }
            for (int x : AppData.deactivatingSubscription) {
                Log.i(LOG_TAG, "deactivation value of " + x);
                if (AppData.mSubscriptionsMap.get(x) == null) {
                    Log.i(LOG_TAG, "deactivation broken " + x);
                    break;
                }
                if (!AppData.mSubscriptionsMap.get(x).isSubscribed()) {
                    AppData.deactivatingSubscription.remove(x);
                    if (mSID == x && mMenu != null) {
                        mMenu.getItem(2).setVisible(true);
                        itemSelected = true;
                    }
                }
            }
        }

        @Override
        public void run() {
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    getSubscriptions();
                    processMenu();
                    subscriptionDataAdapter.notifyDataSetChanged();
                }
            });
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (updaterTimer != null) {
            updaterTimer.cancel();
        }
    }

}
