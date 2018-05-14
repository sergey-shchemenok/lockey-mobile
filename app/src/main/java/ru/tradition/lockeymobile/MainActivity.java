package ru.tradition.lockeymobile;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.Context;
import android.content.DialogInterface;
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
import android.util.AttributeSet;
import android.util.Log;
import android.view.DragEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.messaging.FirebaseMessaging;

import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import ru.tradition.lockeymobile.tabs.AppTabAdapter;
import ru.tradition.lockeymobile.tabs.assetstab.AssetsData;
import ru.tradition.lockeymobile.tabs.assetstab.AssetsFragmentTab;
import ru.tradition.lockeymobile.tabs.assetstab.AssetsQueryUtils;
import ru.tradition.lockeymobile.tabs.maptab.GeofenceQueryUtils;
import ru.tradition.lockeymobile.tabs.maptab.MapFragmentTab;
import ru.tradition.lockeymobile.tabs.maptab.MapFragmentTabOSM;
import ru.tradition.lockeymobile.tabs.notifications.NotificationsFragmentTab;

import static ru.tradition.lockeymobile.AppData.ASSETS_LOADER_ID;
import static ru.tradition.lockeymobile.AppData.ZONES_LOADER_ID;
import static ru.tradition.lockeymobile.AppData.mAssetMap;
import static ru.tradition.lockeymobile.tabs.assetstab.AssetsQueryUtils.assetsUrlResponseMessage;

public class MainActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<LoadedData>,
        MapFragmentTab.OnFragmentInteractionListener,
        MapFragmentTabOSM.OnFragmentInteractionListener,
        NotificationsFragmentTab.OnFragmentInteractionListener,
        AssetsFragmentTab.OnFragmentInteractionListener {

    private static final int UPDATING_INTERVAL = 5000;

    private AppTabAdapter adapter;

    private Toolbar toolbar;

    private ConnectivityManager connectivityManager;
    private NetworkInfo activeNetwork;
    private LoaderManager loaderManager;
    private TextView infoMessage;


    //preferences
    public static boolean allowNotification;
    public static String useMap;

    public static final String LOG_TAG = MainActivity.class.getName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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

        infoMessage = (TextView) findViewById(R.id.main_info_message);
        infoMessage.setVisibility(View.GONE);

        //todo later
        //FirebaseMessaging.getInstance().subscribeToTopic("NEWS");

        if (MapFragmentTab.markers != null || !MapFragmentTab.markers.isEmpty())
            MapFragmentTab.markers.clear();
        if (MapFragmentTabOSM.osm_markers != null || !MapFragmentTabOSM.osm_markers.isEmpty())
            MapFragmentTabOSM.osm_markers.clear();
        if (MapFragmentTabOSM.osm_map != null)
            MapFragmentTabOSM.osm_map.getOverlays().clear();


        //todo when server part be ready
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        useMap = sharedPrefs.getString(
                getString(R.string.settings_use_map_key),
                getString(R.string.settings_default_map)
        );
        Log.i(LOG_TAG, "useMap.........." + useMap);

        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        AppData.viewPager = (CustomViewPager) findViewById(R.id.viewpager);
        adapter = new AppTabAdapter(this, getSupportFragmentManager(), useMap);
        AppData.viewPager.setAdapter(adapter);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(AppData.viewPager);

        AppData.viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (AppData.mMenu != null) {
                    if (AppData.isAssetSelectingMode && position != 0) {
                        changeModeToNormal();
                        AssetsFragmentTab.aft.updateListView();
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
                        AppData.viewPager.setPagingEnabled(false);
                    } else
                        AppData.viewPager.setPagingEnabled(true);

                    //do not delete!!!
//                if (MapFragmentTab.bottomSheetBehavior != null) {
//                    if (MapFragmentTab.bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED ||
//                            MapFragmentTab.bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
//                        MapFragmentTab.bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
//                    }
//                }
//                if (MapFragmentTabOSM.bottomSheetBehavior != null) {
//                    if (MapFragmentTabOSM.bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED ||
//                            MapFragmentTabOSM.bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
//                        MapFragmentTabOSM.bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
//                    }
//                }

                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        //get data from asset and notification activity to open map
        Bundle bundle = getIntent().getExtras();
        if (bundle != null && !bundle.isEmpty()) {
            if (bundle.containsKey("latitude")) {
                AppData.viewPager.setCurrentItem(1);
                //go to map tab
                if (MapFragmentTab.google_map != null && useMap.equals(getString(R.string.settings_google_map_value))) {
                    AppData.target = CameraPosition.builder()
                            .target(new LatLng(bundle.getDouble("latitude"), bundle.getDouble("longitude")))
                            .zoom(14)
                            .build();
                    MapFragmentTab.google_map.moveCamera(CameraUpdateFactory.newCameraPosition(AppData.target));
                } else if (MapFragmentTabOSM.mapController != null && useMap.equals(getString(R.string.settings_osm_value))) {
                    AppData.osmCameraZoom = 16.0;
                    AppData.osmStartPoint = new GeoPoint(bundle.getDouble("latitude"), bundle.getDouble("longitude"));
                    MapFragmentTabOSM.mapController.setZoom(AppData.osmCameraZoom);
                    MapFragmentTabOSM.mapController.setCenter(AppData.osmStartPoint);
                }
                bundle.clear();
            } else if (bundle.containsKey("page")) {
                AppData.viewPager.setCurrentItem(bundle.getInt("page"));
            }
        }

        Log.i(LOG_TAG, "Before starting loaders");

        //launch loading data from server
        getAssetsData();
        getZones();
        startMainDataUpdater();

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
    public void getAssetsData() {
        Log.i(LOG_TAG, "....getting assets....");
        activeNetwork = connectivityManager.getActiveNetworkInfo();
        if (activeNetwork != null && activeNetwork.isConnected()) {
            loaderManager = getLoaderManager();
            loaderManager.initLoader(AppData.ASSETS_LOADER_ID, null, this);
            Log.i(LOG_TAG, "starting loader");
        } else {
            infoMessage.setVisibility(View.VISIBLE);
            infoMessage.setText(R.string.no_connection);
        }
    }

    public void getZones() {
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

        loaderManager.destroyLoader(AppData.ASSETS_LOADER_ID);
        loaderManager.destroyLoader(AppData.ZONES_LOADER_ID);

        if (loadedData.getPolygonsMap() != null && !loadedData.getPolygonsMap().isEmpty()) {
            AppData.mPolygonsMap = loadedData.getPolygonsMap();
            Log.i(LOG_TAG, "Polygons loaded" + "\n" + AppData.mPolygonsMap.toString());
            return;
        }

        //whether it can be authorized. The token has not expired
        if (GeofenceQueryUtils.zonesUrlResponseCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
            AppData.needToken = true;
            //todo something later
            getZones();
            return;
        }

        //whether it can be authorized. The token has not expired
        if (AssetsQueryUtils.assetsUrlResponseCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
            AppData.needToken = true;
            getAssetsData();
            return;
        }


        //whether it has some problem
        if (AssetsQueryUtils.assetsUrlResponseCode != HttpURLConnection.HTTP_OK &&
                AssetsQueryUtils.assetsUrlResponseCode != HttpURLConnection.HTTP_UNAUTHORIZED) {
            infoMessage.setVisibility(View.VISIBLE);
            infoMessage.setText(assetsUrlResponseMessage);
            return;
        }
        infoMessage.setVisibility(View.GONE);

        if (loadedData.getAssetMap() == null || loadedData.getAssetMap().isEmpty()) {
            if (AppData.mAssetMap == null || AppData.mAssetMap.isEmpty()) {
                AssetsFragmentTab.aft.mEmptyStateTextView.setText(R.string.no_assets);
                return;
            }
            Log.i(LOG_TAG, "the loading has finished with old data");
        } else {
            AppData.mAssetMap = loadedData.getAssetMap();
            Log.i(LOG_TAG, "the loading has finished without mistakes");
        }

        if (AssetsFragmentTab.aft == null)
            return;
        AssetsFragmentTab.aft.mEmptyStateTextView.setText("");
        AssetsFragmentTab.aft.progressCircle.setVisibility(View.GONE);

        AssetsFragmentTab.aft.updateListView();
    }

    //we need to interrupt the loading thread
    @Override
    protected void onDestroy() {

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
                        AssetsFragmentTab.aft.updateListView();
                    } else if (AppData.viewPager.getCurrentItem() == 2 && NotificationsFragmentTab.notificationListView.getCount() != 0) {
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
                    if (MapFragmentTab.google_map != null && useMap.equals(getString(R.string.settings_google_map_value))) {
                        //first calculate the bounds of all the markers like so:
                        LatLngBounds.Builder builder = new LatLngBounds.Builder();
                        for (Integer id : AppData.selectedAsset) {
                            AssetsData as = AppData.mAssetMap.get(id);
                            builder.include(new LatLng(as.getLatitude(), as.getLongitude()));
                        }
                        LatLngBounds bounds = builder.build();
                        //Then obtain a movement description object by using the factory: CameraUpdateFactory:
                        int padding = 10; // offset from edges of the map in pixels
                        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
//                        changeModeToNormal();
//                        updateListView();
                        MapFragmentTab.google_map.moveCamera(cu);
                    } else if (MapFragmentTabOSM.mapController != null && useMap.equals(getString(R.string.settings_osm_value))) {
                        if (AppData.selectedAsset.size() > 1) {
                            int minLat = Integer.MAX_VALUE;
                            int maxLat = Integer.MIN_VALUE;
                            int minLong = Integer.MAX_VALUE;
                            int maxLong = Integer.MIN_VALUE;
                            for (Integer id : AppData.selectedAsset) {
                                AssetsData as = AppData.mAssetMap.get(id);
                                GeoPoint point = new GeoPoint(as.getLatitude(), as.getLongitude());
                                if (Math.round(point.getLatitude() * 10000000) < minLat)
                                    minLat = (int) Math.round(point.getLatitude() * 10000000);
                                if (Math.round(point.getLatitude() * 10000000) > maxLat)
                                    maxLat = (int) Math.round(point.getLatitude() * 10000000);
                                if (Math.round(point.getLongitude() * 10000000) < minLong)
                                    minLong = (int) Math.round(point.getLongitude() * 10000000);
                                if (Math.round(point.getLongitude() * 10000000) > maxLong)
                                    maxLong = (int) Math.round(point.getLongitude() * 10000000);
                            }
                            //todo something more
                            BoundingBox boundingBox = new BoundingBox((double) maxLat / 10000000,
                                    (double) maxLong / 10000000,
                                    (double) minLat / 10000000,
                                    (double) minLong / 10000000);
                            MapFragmentTabOSM.osm_map.zoomToBoundingBox(boundingBox, true, 10);
                        } else {
                            AppData.osmCameraZoom = 22.0;
                            AppData.osmStartPoint = new GeoPoint(
                                    AppData.mAssetMap.get(AppData.selectedAsset.toArray()[0]).getLatitude(),
                                    AppData.mAssetMap.get(AppData.selectedAsset.toArray()[0]).getLongitude());
                            MapFragmentTabOSM.mapController.setZoom(AppData.osmCameraZoom);
                            MapFragmentTabOSM.mapController.setCenter(AppData.osmStartPoint);
                        }
                    }
                    AppData.viewPager.setCurrentItem(1);
                }
                return true;

            case R.id.main_menu_feedback:
                String[] addresses = new String[3];
                addresses[0] = "alex.zador@gmail.com";
                addresses[1] = "shemenok@tradition.ru";
                addresses[2] = "panfilovai@tradition.ru";
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:")); // only email apps should handle this
                intent.putExtra(Intent.EXTRA_EMAIL, addresses);
                intent.putExtra(Intent.EXTRA_SUBJECT, "Сообщение от пользователя " + AppData.usr);
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }
                return true;

            case R.id.main_menu_about_program:
                showAboutTheProgram(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void logout() {
        Intent intent = new Intent(this, AuthActivity.class);
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
            AssetsFragmentTab.aft.updateListView();
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
            AssetsFragmentTab.aft.updateListView();
        } else if (AppData.isNotificationSelectingMode) {
            changeModeToNormal();
            NotificationsFragmentTab.adapter.notifyDataSetChanged();
        } else if (MapFragmentTab.bottomSheetBehavior != null &&
                MapFragmentTab.bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            MapFragmentTab.bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        } else if (MapFragmentTab.bottomSheetBehavior != null &&
                MapFragmentTab.bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
            MapFragmentTab.bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        } else if (MapFragmentTabOSM.bottomSheetBehavior != null &&
                MapFragmentTabOSM.bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            MapFragmentTabOSM.bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        } else if (MapFragmentTabOSM.bottomSheetBehavior != null &&
                MapFragmentTabOSM.bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
            MapFragmentTabOSM.bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        } else
            logout();
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    //let's check up the Timer
    private Timer mainDataUpdaterTimer;
    private MainActivity.MainDataUpdater mainDataUpdater;

    private void startMainDataUpdater() {
        if (mainDataUpdaterTimer != null) {
            mainDataUpdaterTimer.cancel();
        }
        // re-schedule timer here otherwise, IllegalStateException of "TimerTask is scheduled already" will be thrown
        mainDataUpdaterTimer = new Timer();
        mainDataUpdater = new MainActivity.MainDataUpdater();
        mainDataUpdaterTimer.schedule(mainDataUpdater, UPDATING_INTERVAL, UPDATING_INTERVAL);
    }

    class MainDataUpdater extends TimerTask {

        @Override
        public void run() {
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    //first we need to update data
                    getAssetsData();
                    Log.i(LOG_TAG, "Repeating loading assets");
                    if (MapFragmentTab.mft != null)
                        MapFragmentTab.mft.updateMarkers();
                    if (MapFragmentTabOSM.mftosm != null)
                        MapFragmentTabOSM.mftosm.updateMarkers();
                }
            });
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mainDataUpdaterTimer != null) {
            mainDataUpdaterTimer.cancel();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        startMainDataUpdater();
    }

    public static void showAboutTheProgram(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("О программе")
                .setMessage("Lockey Mobile\nВерсия: " + AppData.VERSION + "\nНПГ Традиция ©")
                .setCancelable(false)
                .setNegativeButton("Закрыть",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
        AlertDialog alert = builder.create();
        alert.show();
    }

}
