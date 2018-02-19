package ru.tradition.lockeymobile;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
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

import com.google.firebase.messaging.FirebaseMessaging;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Map;

import ru.tradition.lockeymobile.tabs.AppTabAdapter;
import ru.tradition.lockeymobile.tabs.assetstab.AssetsData;
import ru.tradition.lockeymobile.tabs.assetstab.AssetsFragment;
import ru.tradition.lockeymobile.tabs.assetstab.AssetsLoader;
import ru.tradition.lockeymobile.tabs.assetstab.AssetsQueryUtils;
import ru.tradition.lockeymobile.tabs.maptab.MapFragmentTab;
import ru.tradition.lockeymobile.tabs.noticetab.NoticeFragment;

import static ru.tradition.lockeymobile.tabs.assetstab.AssetsQueryUtils.assetsUrlResponseCode;
import static ru.tradition.lockeymobile.tabs.assetstab.AssetsQueryUtils.assetsUrlResponseMessage;

public class MainActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Map<Integer, AssetsData>>,
        MapFragmentTab.OnFragmentInteractionListener,
        NoticeFragment.OnFragmentInteractionListener,
        AssetsFragment.OnFragmentInteractionListener {

    private AppTabAdapter adapter;

    private Toolbar toolbar;

    private TextView mEmptyStateTextView;
    private ProgressBar progressCircle;
    private ConnectivityManager connectivityManager;
    private NetworkInfo activeNetwork;
    private LoaderManager loaderManager;
    private TextView infoMessage;

    public static final String LOG_TAG = MainActivity.class.getName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //go to auth activity
        if (assetsUrlResponseCode == 0) {
            Intent intent = new Intent(this, AuthActivity.class);
            startActivity(intent);
            Log.i(LOG_TAG, ".............assetsUrlResponseCode == 0");
        }
        AppData.mainActivity = this;

        setContentView(R.layout.activity_main);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FirebaseMessaging.getInstance().subscribeToTopic("NEWS");

        //toolbar.setTitle("title");

        progressCircle = (ProgressBar) findViewById(R.id.loading_spinner);
        mEmptyStateTextView = (TextView) findViewById(R.id.empty_view);
        infoMessage = (TextView) findViewById(R.id.main_info_message);
        infoMessage.setVisibility(View.GONE);
        progressCircle.setVisibility(View.VISIBLE);

        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        //launch loading data from server
        startLoader();
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
        activeNetwork = connectivityManager.getActiveNetworkInfo();
        if (activeNetwork != null && activeNetwork.isConnected()) {
            loaderManager = getLoaderManager();
            loaderManager.initLoader(AppData.ASSETS_LOADER_ID, null, this);
            Log.i(LOG_TAG, "initLoader");
        } else {
            progressCircle.setVisibility(View.GONE);
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
    public Loader<Map<Integer, AssetsData>> onCreateLoader(int i, Bundle bundle) {
        // Create a new loader for the given URL
        Log.i(LOG_TAG, "onCreateLoader");
        return new AssetsLoader(this, AppData.ASSETS_REQUEST_URL);
    }

    @Override
    public void onLoadFinished(Loader<Map<Integer, AssetsData>> loader, Map<Integer, AssetsData> assetData) {
        // Set empty state text to display "No assets found."
        progressCircle.setVisibility(View.GONE);

        //whether it can be authorized. The token has not expired
        if (assetsUrlResponseCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
            AssetsQueryUtils.needToken = true;
        }

        //whether it has some problem
        if (assetsUrlResponseCode != HttpURLConnection.HTTP_OK &&
                assetsUrlResponseCode != HttpURLConnection.HTTP_UNAUTHORIZED) {
            infoMessage.setVisibility(View.VISIBLE);
            infoMessage.setText(assetsUrlResponseMessage);
            return;
        }
        infoMessage.setVisibility(View.GONE);

        if (!AppData.isRepeated) {
            AppData.isFinished = true;
            if (assetData == null || assetData.isEmpty()) {
                mEmptyStateTextView.setText(R.string.no_assets);
                return;
            }
            mEmptyStateTextView.setText("");
            Log.i(LOG_TAG, "the first load has finished");

            AppData.mAssetData = assetData;

            // Find the view pager that will allow the user to swipe between fragments
            AppData.viewPager = (ViewPager) findViewById(R.id.viewpager);
            // Create an adapter that knows which fragment should be shown on each page
            adapter = new AppTabAdapter(this, getSupportFragmentManager());
            // Set the adapter onto the view pager
            AppData.viewPager.setAdapter(adapter);
            //assetsListView.setEmptyView(mEmptyStateTextView);
            TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
            // Connect the tab layout with the view pager.
            tabLayout.setupWithViewPager(AppData.viewPager);

            //listen to tab changes
            AppData.viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                }

                @Override
                public void onPageSelected(int position) {
                    changeMode();
                    updateListView();
                }

                @Override
                public void onPageScrollStateChanged(int state) {
                }
            });

//            Intent intent = getIntent();
//            Bundle bundle = intent.getExtras();
//            if (bundle != null && bundle.containsKey("TabNumber")) {
//                int tabNumber = bundle.getInt("TabNumber");
//                if (tabNumber == 2)
//                    AppData.viewPager.setCurrentItem(2);
//            }

            AppData.isRepeated = true;

            //this for notices
            if (AppData.noticeReceived == true) {
                //restart activity
                //recreate();
                AppData.viewPager.setCurrentItem(2);
            }

        } else {
            Log.i(LOG_TAG, "the second load has finished");
            if (assetData == null || assetData.isEmpty()) {
                return;
            }
            AppData.mAssetData = assetData;
            //here is not good for speed
            updateListView();

            //this for notices
            if (AppData.noticeReceived == true) {
                //restart activity
                //recreate();
                //AppData.viewPager.setCurrentItem(2);
            }


        }
    }

    //update the list of assets
    public void updateListView() {
        AssetsFragment.assetsDataAdapter.clear();
        AssetsFragment.assetsDataAdapter.addAll(new ArrayList<>(AppData.mAssetData.values()));
    }

    //we need to interrupt the loading thread
    @Override
    protected void onDestroy() {
        AppData.isFinished = false;
        AppData.isRepeated = false;
        super.onDestroy();
    }

    @Override
    public void onLoaderReset(Loader<Map<Integer, AssetsData>> loader) {
        //AppData.mAssetData.clear();
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
        if (!AppData.isSelectingMode)
            AppData.mMenu.getItem(2).setVisible(false);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            case R.id.main_menu_logout:
                logout();
                return true;
            case R.id.main_menu_settings:
                //todo settings here
                return true;
            case R.id.main_menu_back:
                changeMode();
                updateListView();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void logout() {
        Intent intent = new Intent(this, AuthActivity.class);
        AppData.isFinished = false;
        AppData.isRepeated = false;
        if (AppData.isSelectingMode)
            changeMode();
        startActivity(intent);

    }

    //to change mode from selecting to normal
    public void changeMode() {
        AppData.isSelectingMode = false;
        AppData.selectedAsset.clear();
        AppData.selectedAssetCounter = 0;
        setTitle(R.string.app_name);
        AppData.mMenu.getItem(2).setVisible(false);
        removeUpButton();
    }

    @Override
    public boolean onSupportNavigateUp() {
        changeMode();
        updateListView();
        return true;
    }

    /**
     * This method is called when the back button is pressed.
     */
    @Override
    public void onBackPressed() {
        // If the pet hasn't changed, continue with handling back button press
        //This button is upper to the left arrow
        if (AppData.isSelectingMode) {
            changeMode();
            updateListView();
        } else
            logout();
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
