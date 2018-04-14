package ru.tradition.lockeymobile;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.firebase.messaging.FirebaseMessaging;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Collections;

import ru.tradition.lockeymobile.tabs.AppTabAdapter;
import ru.tradition.lockeymobile.tabs.assetstab.AssetsData;
import ru.tradition.lockeymobile.tabs.assetstab.AssetsFragmentTab;
import ru.tradition.lockeymobile.tabs.assetstab.AssetsQueryUtils;
import ru.tradition.lockeymobile.tabs.maptab.GeofenceQueryUtils;
import ru.tradition.lockeymobile.tabs.maptab.MapFragmentTab;
import ru.tradition.lockeymobile.tabs.notifications.NotificationsFragmentTab;

import static ru.tradition.lockeymobile.AppData.ASSETS_LOADER_ID;
import static ru.tradition.lockeymobile.AppData.ZONES_LOADER_ID;
import static ru.tradition.lockeymobile.AppData.mAssetMap;
import static ru.tradition.lockeymobile.tabs.assetstab.AssetsQueryUtils.assetsUrlResponseMessage;

public class MainActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<LoadedData>,
        MapFragmentTab.OnFragmentInteractionListener,
        NotificationsFragmentTab.OnFragmentInteractionListener,
        AssetsFragmentTab.OnFragmentInteractionListener {

    private AppTabAdapter adapter;

    private Toolbar toolbar;

    private TextView mEmptyStateTextView;
    private ProgressBar progressCircle;
    private TextView infoMessage;
    private ConnectivityManager connectivityManager;
    private NetworkInfo activeNetwork;
    private LoaderManager loaderManager;


    //preferences
    public static boolean allowNotification;
    public static String orderBy;

    public static final String LOG_TAG = MainActivity.class.getName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //go to auth activity
//        if (AppData.assetsUrlResponseCode == 0) {
//            Intent intent = new Intent(this, AuthActivity.class);
//            startActivity(intent);
//            Log.i(LOG_TAG, ".............assetsUrlResponseCode == 0");
//        }
        //go to auth activity
        if (AppData.usr.equals("") || AppData.pwd.equals("")) {
            Intent intent = new Intent(this, AuthActivity.class);
            startActivity(intent);
            Log.i(LOG_TAG, ".............no credentials");
            return;
        }

        AppData.mainActivity = this;

        setContentView(R.layout.activity_main);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //todo later
        FirebaseMessaging.getInstance().subscribeToTopic("NEWS");

        progressCircle = (ProgressBar) findViewById(R.id.loading_spinner);
        mEmptyStateTextView = (TextView) findViewById(R.id.empty_view);
        infoMessage = (TextView) findViewById(R.id.main_info_message);
        infoMessage.setVisibility(View.GONE);
        progressCircle.setVisibility(View.VISIBLE);

        //todo when server part be ready
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        allowNotification = sharedPrefs.getBoolean(getString(R.string.settings_allow_notifications_key), false);
        Log.i(LOG_TAG, "allowNotification.........." + allowNotification);
        orderBy = sharedPrefs.getString(
                getString(R.string.settings_order_by_key),
                getString(R.string.settings_order_by_default)
        );
        Log.i(LOG_TAG, "orderBy.........." + orderBy);
        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        //launch loading data from server
        startLoader();
        getZones();
    }

    //The method adds "up" button to toolbar
    public void setUpButton() {
        if (toolbar != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }
    }

    //The method adds "up" button from toolbar
    public void removeUpButton() {
        if (toolbar != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setHomeButtonEnabled(false);
        }
    }

    //For initial data loading
    public void startLoader() {
        Log.i(LOG_TAG, "....startLoader....");
        activeNetwork = connectivityManager.getActiveNetworkInfo();
        if (activeNetwork != null && activeNetwork.isConnected()) {
            loaderManager = getLoaderManager();
            loaderManager.initLoader(AppData.ASSETS_LOADER_ID, null, this);
            Log.i(LOG_TAG, "initLoader");
        } else {
            logout();
        }
    }

    public void getZones(){
        Log.i(LOG_TAG, "....getZones....");
        activeNetwork = connectivityManager.getActiveNetworkInfo();
        if (activeNetwork != null && activeNetwork.isConnected()) {
            loaderManager = getLoaderManager();
            loaderManager.initLoader(AppData.ZONES_LOADER_ID, null, this);

            Log.i(LOG_TAG, "initLoader");
        } else {
            infoMessage.setVisibility(View.VISIBLE);
            infoMessage.setText(R.string.no_connection);
        }
    }

    //For periodic data loading
    public synchronized void repeatLoader() {
        activeNetwork = connectivityManager.getActiveNetworkInfo();
        if (activeNetwork != null && activeNetwork.isConnected()) {
            loaderManager.destroyLoader(AppData.ASSETS_LOADER_ID);
            loaderManager.initLoader(AppData.ASSETS_LOADER_ID, null, this);
            Log.i(LOG_TAG, "repeatLoader");
        } else {
            infoMessage.setVisibility(View.VISIBLE);
            infoMessage.setText(R.string.no_connection);
            //todo set red bar status
            Log.i(LOG_TAG, "No connection");
        }
    }

    @Override
    public Loader<LoadedData> onCreateLoader(int loaderId, Bundle bundle) {
        switch (loaderId) {
            case ASSETS_LOADER_ID:
                // Create a new loader for the given URL
                Log.i(LOG_TAG, "onCreateLoader");
                return new DataLoader(this, AppData.ASSETS_REQUEST_URL, ASSETS_LOADER_ID);
            case ZONES_LOADER_ID:
                return new DataLoader(this, AppData.ZONES_LIST_URL, ZONES_LOADER_ID);
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<LoadedData> loader, LoadedData loadedData) {
        if (loadedData.getPolygonsMap() != null && !loadedData.getPolygonsMap().isEmpty()) {
            AppData.mPolygonsMap = loadedData.getPolygonsMap();
            loaderManager.destroyLoader(AppData.ZONES_LOADER_ID);
            Log.i(LOG_TAG, "Polygons loaded" + "\n" + AppData.mPolygonsMap.toString());
            return;
        }

        //whether it can be authorized. The token has not expired
        if (AppData.zonesUrlResponseCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
            loaderManager.destroyLoader(AppData.ZONES_LOADER_ID);
            AppData.needToken = true;
            //todo something later
            getZones();
        }

        //whether it can be authorized. The token has not expired
        if (AppData.assetsUrlResponseCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
            AppData.needToken = true;
            getZones();
        }


        //whether it has some problem
        if (AppData.assetsUrlResponseCode != HttpURLConnection.HTTP_OK &&
                AppData.assetsUrlResponseCode != HttpURLConnection.HTTP_UNAUTHORIZED) {
            infoMessage.setVisibility(View.VISIBLE);
            infoMessage.setText(assetsUrlResponseMessage);
            return;
        }
        infoMessage.setVisibility(View.GONE);

        if (!AppData.isRepeated) {
            AppData.isFinished = true;
            if (loadedData.getAssetMap() == null || loadedData.getAssetMap().isEmpty()) {
                if (AppData.mAssetMap == null || AppData.mAssetMap.isEmpty()) {
                    mEmptyStateTextView.setText(R.string.no_assets);
                    return;
                }
                Log.i(LOG_TAG, "the first load has finished with old data");

            } else {
                AppData.mAssetMap = loadedData.getAssetMap();
                Log.i(LOG_TAG, "the first load has finished without mistakes");

            }
            mEmptyStateTextView.setText("");
            Log.i(LOG_TAG, "the first load has finished");

            AppData.viewPager = (ViewPager) findViewById(R.id.viewpager);
            adapter = new AppTabAdapter(this, getSupportFragmentManager());
            AppData.viewPager.setAdapter(adapter);
            TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
            tabLayout.setupWithViewPager(AppData.viewPager);

            AppData.viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                }

                @Override
                public void onPageSelected(int position) {
                    if (AppData.isAssetSelectingMode && position != 0) {
                        changeModeToNormal();
                        updateListView();
                    } else if (AppData.isNotificationSelectingMode && position != 2) {
                        changeModeToNormal();
                        NotificationsFragmentTab.adapter.notifyDataSetChanged();
                    }

                    if (!AppData.isAssetSelectingMode && position == 0) {
                        AppData.mMenu.getItem(1).setVisible(true);
                    }
                    if (!AppData.isNotificationSelectingMode && position == 2) {
                        AppData.mMenu.getItem(1).setVisible(true);
                    }
                    if (position == 1) {
                        AppData.mMenu.getItem(1).setVisible(false);
                    }

                    if (MapFragmentTab.bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                        MapFragmentTab.bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                    }

                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }
            });

            AppData.isRepeated = true;

            //if we come here from the asset activity
            Bundle bundle = getIntent().getExtras();
            if (bundle != null && !bundle.isEmpty()) {
                AppData.viewPager.setCurrentItem(1);
                AppData.target = CameraPosition.builder()
                        .target(new LatLng(bundle.getDouble("latitude"), bundle.getDouble("longitude")))
                        .zoom(13)
                        .build();
                //go to map tab
                AppData.m_map.moveCamera(CameraUpdateFactory.newCameraPosition(AppData.target));
                bundle.clear();
            }

            if (progressCircle.getVisibility() != View.GONE)
                progressCircle.setVisibility(View.GONE);


        } else {
            Log.i(LOG_TAG, "the second load has finished");
            if (loadedData.getAssetMap() == null || loadedData.getAssetMap().isEmpty()) {
                return;
            }
            AppData.mAssetMap = loadedData.getAssetMap();
            updateListView();

            if (progressCircle.getVisibility() != View.GONE)
                progressCircle.setVisibility(View.GONE);
        }
    }

    //update the list of assets
    public void updateListView() {
        AssetsFragmentTab.assetsDataAdapter.clear();
        //AssetsFragmentTab.assetsDataAdapter.notifyDataSetChanged();
        Log.i(LOG_TAG, "order by list..........." + getString(R.string.settings_order_by_kit_id_value));
        if (orderBy.equals(getString(R.string.settings_order_by_kit_id_value))) {
            AssetsFragmentTab.assetsDataAdapter.addAll(new ArrayList<>(mAssetMap.values()));
        } else if (orderBy.equals(getString(R.string.settings_order_by_signal_time_value))) {
            ArrayList<AssetsData> ads = new ArrayList<>(mAssetMap.values());
            Collections.sort(ads, AssetsData.COMPARE_BY_LAST_SIGNAL_TIME);
            AssetsFragmentTab.assetsDataAdapter.addAll(ads);
        }
    }

    //we need to interrupt the loading thread
    @Override
    protected void onDestroy() {
        AppData.isFinished = false;
        AppData.isRepeated = false;
        super.onDestroy();
    }

    @Override
    public void onLoaderReset(Loader<LoadedData> loader) {
        //AppData.mAssetMap.clear();
        Log.i(LOG_TAG, "onLoadReset");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        AppData.mMenu = menu;
        //MenuItem item = (MenuItem) findViewById(R.id.main_menu_back);
//        item.setVisible(true);
        //in normal mode this item should not be shown
        if (!AppData.isAssetSelectingMode)
            AppData.mMenu.getItem(3).setVisible(false);
        if (!AppData.isNotificationSelectingMode)
            AppData.mMenu.getItem(4).setVisible(false);
        if (AppData.isAssetSelectingMode || AppData.isNotificationSelectingMode)
            AppData.mMenu.getItem(1).setVisible(false);
        try {
            if (AppData.viewPager.getCurrentItem() == 1)
                AppData.mMenu.getItem(1).setVisible(false);
        } catch (NullPointerException e) {
        }
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            case R.id.main_menu_logout:
                logout();
                return true;
            case R.id.main_menu_item_selection:
                //todo settings here
                if (!AppData.isAssetSelectingMode && !AppData.isNotificationSelectingMode) {
                    if (AppData.viewPager.getCurrentItem() == 0) {
                        AppData.isAssetSelectingMode = true;
                        AppData.mainActivity.setUpButton();
                        AppData.mMenu.getItem(3).setVisible(true);
                        AppData.mMenu.getItem(1).setVisible(false);
                        AppData.mainActivity.setTitle("Выбрано: " + String.valueOf(AppData.selectedAssetCounter));
                        AppData.mainActivity.updateListView();
                    } else if (AppData.viewPager.getCurrentItem() == 2) {
                        AppData.isNotificationSelectingMode = true;
                        Log.i(LOG_TAG, "the mode......... has changed");
                        AppData.mainActivity.setUpButton();
                        AppData.mMenu.getItem(4).setVisible(true);
                        AppData.mMenu.getItem(1).setVisible(false);
                        AppData.mainActivity.setTitle("Выбрано: " + String.valueOf(AppData.selectedNotificationCounter));
                        NotificationsFragmentTab.adapter.notifyDataSetChanged();
                    }
                }
                return true;
            case R.id.main_menu_settings:
                Intent settingsIntent = new Intent(this, SettingsActivity.class);
                settingsIntent.putExtra("currentPage", AppData.viewPager.getCurrentItem());
                startActivity(settingsIntent);
                return true;
            case R.id.main_menu_subscriptions:
                Intent subscriptionsIntent = new Intent(this, SubscriptionsActivity.class);
                subscriptionsIntent.putExtra("currentPage", AppData.viewPager.getCurrentItem());
                startActivity(subscriptionsIntent);
                return true;
            case R.id.main_menu_delete:
                //Let it be here for a while
                NotificationsFragmentTab.notificationsFragmentTab.showDeleteConfirmationDialog();
                NotificationsFragmentTab.adapter.notifyDataSetChanged();
                return true;
            case R.id.main_menu_zoom_out:
                if (!AppData.selectedAsset.isEmpty() && AppData.selectedAsset != null) {
                    //first calculate the bounds of all the markers like so:
                    LatLngBounds.Builder builder = new LatLngBounds.Builder();
                    for (Integer id : AppData.selectedAsset) {
                        AssetsData as = AppData.mAssetMap.get(id);
                        builder.include(new LatLng(as.getLatitude(), as.getLongitude()));
                    }
                    LatLngBounds bounds = builder.build();

                    //Then obtain a movement description object by using the factory: CameraUpdateFactory:
                    int padding = 5; // offset from edges of the map in pixels
                    CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
                    changeModeToNormal();
                    updateListView();
                    AppData.viewPager.setCurrentItem(1);
                    AppData.m_map.moveCamera(cu);
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void logout() {
        Intent intent = new Intent(this, AuthActivity.class);
        AppData.isFinished = false;
        AppData.isRepeated = false;
        NotificationsFragmentTab.loaderSwitch = 0;
        if (AppData.isAssetSelectingMode || AppData.isNotificationSelectingMode)
            changeModeToNormal();
        startActivity(intent);
//        AppData.viewPager.setCurrentItem(0);
    }

    //to change mode from selecting to normal
    public void changeModeToNormal() {
        AppData.isAssetSelectingMode = false;
        AppData.selectedAsset.clear();
        AppData.selectedAssetCounter = 0;
        AppData.isNotificationSelectingMode = false;
        AppData.selectedNotification.clear();
        AppData.selectedNotificationUri.clear();
        AppData.selectedNotificationCounter = 0;
        setTitle(R.string.app_name);
        AppData.mMenu.getItem(3).setVisible(false);
        AppData.mMenu.getItem(4).setVisible(false);
        AppData.mMenu.getItem(1).setVisible(true);
        removeUpButton();

    }

    @Override
    public boolean onSupportNavigateUp() {
        if (AppData.isAssetSelectingMode) {
            changeModeToNormal();
            updateListView();
        } else if (AppData.isNotificationSelectingMode) {
            changeModeToNormal();
            NotificationsFragmentTab.adapter.notifyDataSetChanged();
        }
        return true;
    }

    /**
     * This method is called when the back button is pressed.
     */
    @Override
    public void onBackPressed() {
        // If the pet hasn't changed, continue with handling back button press
        //This button is upper to the left arrow
        if (AppData.isAssetSelectingMode) {
            changeModeToNormal();
            updateListView();
        } else if (AppData.isNotificationSelectingMode) {
            changeModeToNormal();
            NotificationsFragmentTab.adapter.notifyDataSetChanged();
        } else if (MapFragmentTab.bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            MapFragmentTab.bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        } else if (MapFragmentTab.bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
            MapFragmentTab.bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        } else
            logout();
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
