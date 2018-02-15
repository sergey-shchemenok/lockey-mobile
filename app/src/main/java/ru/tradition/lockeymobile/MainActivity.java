package ru.tradition.lockeymobile;

import android.app.LoaderManager;
import android.content.ClipData;
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

    public static MainActivity mainActivity;
    public ViewPager viewPager;
    public AppTabAdapter adapter;

    private Toolbar toolbar;

    //Flags for managing the updating thread
    public static boolean isRepeated = false;
    private static boolean isInterrupted = false;
    public static boolean isFinished = false;

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

        if (assetsUrlResponseCode == 0) {
            Intent intent = new Intent(this, AuthActivity.class);
            startActivity(intent);
        }

        setContentView(R.layout.activity_main);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //toolbar.setTitle("title");

        progressCircle = (ProgressBar) findViewById(R.id.loading_spinner);
        mEmptyStateTextView = (TextView) findViewById(R.id.empty_view);
        infoMessage = (TextView) findViewById(R.id.main_info_message);
        infoMessage.setVisibility(View.GONE);
        progressCircle.setVisibility(View.VISIBLE);

        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        //launch loading data from server
        startLoader();

        mainActivity = this;
    }

    //The method adds "up" button to toolbar
    public void setUpButton() {
        if (toolbar != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }
    }

    //The method adds "up" button to toolbar
    public void removeUpButton() {
        if (toolbar != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setHomeButtonEnabled(false);
        }
    }

    //For initial data loading
    private void startLoader() {
        activeNetwork = connectivityManager.getActiveNetworkInfo();
        if (activeNetwork != null && activeNetwork.isConnected()) {
            loaderManager = getLoaderManager();
            loaderManager.initLoader(UserData.ASSETS_LOADER_ID, null, this);
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
            loaderManager.destroyLoader(UserData.ASSETS_LOADER_ID);
            loaderManager.initLoader(UserData.ASSETS_LOADER_ID, null, this);
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
        return new AssetsLoader(this, UserData.ASSETS_REQUEST_URL);
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

        if (!isRepeated) {
            isFinished = true;
            if (assetData == null || assetData.isEmpty()) {
                mEmptyStateTextView.setText(R.string.no_assets);
                return;
            }
            mEmptyStateTextView.setText("");
            Log.i(LOG_TAG, "onLoadFinished");

            UserData.mAssetData = assetData;

            // Find the view pager that will allow the user to swipe between fragments
            viewPager = (ViewPager) findViewById(R.id.viewpager);
            // Create an adapter that knows which fragment should be shown on each page
            adapter = new AppTabAdapter(this, getSupportFragmentManager());
            // Set the adapter onto the view pager
            viewPager.setAdapter(adapter);
            //assetsListView.setEmptyView(mEmptyStateTextView);
            TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
            // Connect the tab layout with the view pager.
            tabLayout.setupWithViewPager(viewPager);

            viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                    //changeMode();
                }

                @Override
                public void onPageSelected(int position) {
                    changeMode();
                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }
            });


            isRepeated = true;

        } else {
            if (assetData == null || assetData.isEmpty()) {
                return;
            }
            UserData.mAssetData = assetData;
            //here is not good for speed
            updateListView();

        }
    }

    public void updateListView() {
        AssetsFragment.assetsDataAdapter.clear();
        AssetsFragment.assetsDataAdapter.addAll(new ArrayList<>(UserData.mAssetData.values()));
    }

    @Override
    protected void onDestroy() {
        //we need to interrupt this thread
        isFinished = false;
        isRepeated = false;
        super.onDestroy();
    }

    @Override
    public void onLoaderReset(Loader<Map<Integer, AssetsData>> loader) {
        //UserData.mAssetData.clear();
        Log.i(LOG_TAG, "onLoadReset");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        UserData.mMenu = menu;
        //MenuItem item = (MenuItem) findViewById(R.id.main_menu_back);
//        item.setVisible(true);
        if (!UserData.isSelectedMode)
            UserData.mMenu.getItem(2).setVisible(false);
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
        isFinished = false;
        isRepeated = false;
        if (UserData.isSelectedMode)
            changeMode();
        startActivity(intent);

    }

    public void changeMode() {
        UserData.isSelectedMode = false;
        UserData.selectedAsset.clear();
        UserData.selectedAssetCounter = 0;
        setTitle(R.string.app_name);
        UserData.mMenu.getItem(2).setVisible(false);
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
        if (UserData.isSelectedMode) {
            changeMode();
            updateListView();
        } else
            logout();
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
