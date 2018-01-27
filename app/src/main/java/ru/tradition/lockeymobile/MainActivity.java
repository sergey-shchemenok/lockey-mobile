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
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

import java.net.HttpURLConnection;
import java.util.List;

import ru.tradition.lockeymobile.obtainingassets.AssetsData;
import ru.tradition.lockeymobile.obtainingassets.AssetsFragment;
import ru.tradition.lockeymobile.obtainingassets.AssetsLoader;

import static ru.tradition.lockeymobile.obtainingassets.AssetsQueryUtils.assetsUrlResponseCode;

public class MainActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<List<AssetsData>>,
        MapFragmentTab.OnFragmentInteractionListener,
        OtherFragment.OnFragmentInteractionListener,
        AssetsFragment.OnFragmentInteractionListener {



    public static boolean isRepeated = false;
    public static boolean isInterrupted = false;
    public static boolean isFinished = false;




    private TextView mEmptyStateTextView;
    private ProgressBar progressCircle;
    private ConnectivityManager connectivityManager;
    private LoaderManager loaderManager;

    /**
     * Constant value for the assets loader ID. We can choose any integer.
     * This comes into play if you're using multiple loaders.
     */
    private static final int ASSETS_LOADER_ID = 1;

    public static final String LOG_TAG = MainActivity.class.getName();
    /**
     * URL for assets data from the Lockey Server
     */
    private static final String ASSETS_REQUEST_URL = "http://my.lockey.ru/LockeyREST/api/Cars";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progressCircle = (ProgressBar) findViewById(R.id.loading_spinner);
        mEmptyStateTextView = (TextView) findViewById(R.id.empty_view);


        if (assetsUrlResponseCode == 0) {
            Intent intent = new Intent(this, AuthActivity.class);
            startActivity(intent);
        }
        //Checking the connection using connectivityManager
        startLoader();

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (!isInterrupted) {

                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (isFinished) {
                        repeatLoader();
                        isRepeated = true;
                        Log.i(LOG_TAG, "Repeating loading assets");
                    }
                }
            }
        }).start();
    }

    private void startLoader() {
        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        if (activeNetwork != null && activeNetwork.isConnected()) {
            loaderManager = getLoaderManager();
            loaderManager.initLoader(ASSETS_LOADER_ID, null, this);
            Log.v(LOG_TAG, "initLoader");
        } else {
            progressCircle.setVisibility(View.GONE);
            mEmptyStateTextView.setText(R.string.no_connection);
        }
    }

    private void repeatLoader() {
        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        if (activeNetwork != null && activeNetwork.isConnected()) {
            loaderManager.restartLoader(ASSETS_LOADER_ID, null, this);
            Log.v(LOG_TAG, "initLoader");
        } else {

        }
    }


    @Override
    public Loader<List<AssetsData>> onCreateLoader(int i, Bundle bundle) {
        // Create a new loader for the given URL
        Log.v(LOG_TAG, "onCreateLoader");
        return new AssetsLoader(this, ASSETS_REQUEST_URL);
    }

    @Override
    public void onLoadFinished(Loader<List<AssetsData>> loader, List<AssetsData> assetData) {
        // Set empty state text to display "No assets found."
        progressCircle.setVisibility(View.GONE);


        //whether it can be authorized. The token has not expired
        if (assetsUrlResponseCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
            Intent intent = new Intent(this, AuthActivity.class);
            isFinished = false;
            isRepeated = false;
            startActivity(intent);
            return;
        }

        if (!isRepeated) {
            if (assetData == null || assetData.isEmpty()) {
                connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
                if (activeNetwork != null && activeNetwork.isConnected()) {
                } else {
                    mEmptyStateTextView.setText(R.string.no_connection);
                    return;
                }
                mEmptyStateTextView.setText(R.string.no_assets);
                return;
            }
            Log.v(LOG_TAG, "onLoadFinished");

            UserData.mAssetData = assetData;

            // Find the view pager that will allow the user to swipe between fragments
            ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);

            // Create an adapter that knows which fragment should be shown on each page
            AppTabAdapter adapter = new AppTabAdapter(this, getSupportFragmentManager());

            // Set the adapter onto the view pager
            viewPager.setAdapter(adapter);

            //assetsListView.setEmptyView(mEmptyStateTextView);

            TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);

            // Connect the tab layout with the view pager. This will
            //   1. Update the tab layout when the view pager is swiped
            //   2. Update the view pager when a tab is selected
            //   3. Set the tab layout's tab names with the view pager's adapter's titles
            //      by calling onPageTitle()
            tabLayout.setupWithViewPager(viewPager);

            isFinished = true;
        } else {
            UserData.mAssetData = assetData;
        }

    }

    @Override
    protected void onDestroy() {
        //we need to interrupt this thread
        isFinished = false;
        isRepeated = false;
        super.onDestroy();
    }

    @Override
    public void onLoaderReset(Loader<List<AssetsData>> loader) {
        UserData.mAssetData.clear();

        Log.v(LOG_TAG, "onLoadReset");

    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
