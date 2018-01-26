package ru.tradition.lockeymobile;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.AdapterView;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

import ru.tradition.lockeymobile.obtainingassets.AssetsData;
import ru.tradition.lockeymobile.obtainingassets.AssetsDataAdapter;
import ru.tradition.lockeymobile.obtainingassets.AssetsLoader;

import static ru.tradition.lockeymobile.obtainingassets.AssetsQueryUtils.assetsUrlResponseCode;


public class AssetsActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<List<AssetsData>>{


    /**
     * TextView that is displayed when the list is empty
     */
    private TextView mEmptyStateTextView;
    private ProgressBar progressCircle;
    private ConnectivityManager connectivityManager;

    /**
     * Constant value for the assets loader ID. We can choose any integer.
     * This comes into play if you're using multiple loaders.
     */
    private static final int ASSETS_LOADER_ID = 1;

    public static final String LOG_TAG = AssetsActivity.class.getName();
    /**
     * URL for assets data from the Lockey Server
     */
    private static final String ASSETS_REQUEST_URL = "http://my.lockey.ru/LockeyREST/api/Cars";

    private AssetsDataAdapter assetsDataAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.asset_list);

        progressCircle = (ProgressBar) findViewById(R.id.loading_spinner);
        mEmptyStateTextView = (TextView) findViewById(R.id.empty_view);


        if (assetsUrlResponseCode == 0){
            Intent intent = new Intent(this, AuthActivity.class);
            startActivity(intent);
        }

        //Checking the connection using connectivityManager
        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        if (activeNetwork != null && activeNetwork.isConnected()) {
            // Get a reference to the LoaderManager, in order to interact with loaders.
            LoaderManager loaderManager = getLoaderManager();
            // Initialize the loader. Pass in the int ID constant defined above and pass in null for
            // the bundle. Pass in this activity for the LoaderCallbacks parameter (which is valid
            // because this activity implements the LoaderCallbacks interface).
            loaderManager.initLoader(ASSETS_LOADER_ID, null, this);
            Log.v(LOG_TAG, "initLoader");
        } else {
            progressCircle.setVisibility(View.GONE);
            mEmptyStateTextView.setText(R.string.no_connection);
        }

        ListView assetsListView = (ListView) findViewById(R.id.assets_list);
        assetsDataAdapter = new AssetsDataAdapter(this, new ArrayList<AssetsData>());
        assetsListView.setAdapter(assetsDataAdapter);

        assetsListView.setEmptyView(mEmptyStateTextView);

        //this will be made in the near future, i hope
        assetsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
            //AssetsData assetsData = assetsDataAdapter.getItem(position);
            }
        });

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

        assetsDataAdapter.clear();
        if (assetsUrlResponseCode == HttpURLConnection.HTTP_UNAUTHORIZED){
            Intent intent = new Intent(this, AuthActivity.class);
            startActivity(intent);
            return;
        }

        if (assetData == null || assetData.isEmpty()) {
            connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
            if (activeNetwork != null && activeNetwork.isConnected()) {}else
            {
                mEmptyStateTextView.setText(R.string.no_connection);
                return;
            }
            mEmptyStateTextView.setText(R.string.no_assets);
            return;
        }
        Log.v(LOG_TAG, "onLoadFinished");

        assetsDataAdapter.addAll(assetData);
    }

    @Override
    public void onLoaderReset(Loader<List<AssetsData>> loader) {
        assetsDataAdapter.clear();
        Log.v(LOG_TAG, "onLoadReset");

    }

}
