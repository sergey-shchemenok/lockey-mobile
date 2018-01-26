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

import java.net.HttpURLConnection;
import java.util.List;

import ru.tradition.lockeymobile.obtainingassets.AssetsData;
import ru.tradition.lockeymobile.obtainingassets.AssetsFragment;
import ru.tradition.lockeymobile.obtainingassets.AssetsLoader;

import static ru.tradition.lockeymobile.obtainingassets.AssetsQueryUtils.assetsUrlResponseCode;

public class MainActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<List<AssetsData>>,
        MapFragmentActivity.OnFragmentInteractionListener,
        OtherFragment.OnFragmentInteractionListener,
        AssetsFragment.OnFragmentInteractionListener {

    //Here is the data from Loader about cars and other assets
    public static List<AssetsData> mAssetData;

    //todo Here is the data from Loader about cars and other assets for API lesser 26
    public static List<AssetsData> mAssetDataAPIBefore26;

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
        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        if (activeNetwork != null && activeNetwork.isConnected()) {
            // Get a reference to the LoaderManager, in order to interact with loaders.
            loaderManager = getLoaderManager();
            // Initialize the loader. Pass in the int ID constant defined above and pass in null for
            // the bundle. Pass in this activity for the LoaderCallbacks parameter (which is valid
            // because this activity implements the LoaderCallbacks interface).
            loaderManager.initLoader(ASSETS_LOADER_ID, null, this);
            Log.v(LOG_TAG, "initLoader");
        } else {
            progressCircle.setVisibility(View.GONE);
            mEmptyStateTextView.setText(R.string.no_connection);
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

        if (assetsUrlResponseCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
            Intent intent = new Intent(this, AuthActivity.class);
            startActivity(intent);
            return;
        }

        //assetsDataAdapter.clear();


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

        mAssetData = assetData;

        //mAssetDataAPIBefore26 = assetData;

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

    }

    @Override
    public void onLoaderReset(Loader<List<AssetsData>> loader) {
        mAssetData.clear();

        Log.v(LOG_TAG, "onLoadReset");

    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
