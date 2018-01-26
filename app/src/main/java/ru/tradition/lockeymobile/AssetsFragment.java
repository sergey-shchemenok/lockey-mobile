package ru.tradition.lockeymobile;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

import ru.tradition.lockeymobile.obtainingassets.AssetsData;
import ru.tradition.lockeymobile.obtainingassets.AssetsDataAdapter;
import ru.tradition.lockeymobile.obtainingassets.AssetsLoader;

import static ru.tradition.lockeymobile.obtainingassets.AssetsQueryUtils.assetsUrlResponseCode;

/**
 * Created by Caelestis on 25.01.2018.
 */

public class AssetsFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<List<AssetsData>> {

    private OnFragmentInteractionListener mListener;

    //Here is the data from Loader about cars and other assets
    private List<AssetsData> mAssetData;

    //Here is the data from Loader about cars and other assets for API lesser 26
    private static List<AssetsData> mAssetDataAPIBefore26;

    public AssetsFragment() {
        // Required empty public constructor
    }


    /**
     * TextView that is displayed when the list is empty
     */
    private TextView mEmptyStateTextView;
    private ProgressBar progressCircle;
    private ConnectivityManager connectivityManager;
    private LoaderManager loaderManager;

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.asset_list, container, false);


        progressCircle = (ProgressBar) rootView.findViewById(R.id.loading_spinner);
        mEmptyStateTextView = (TextView) rootView.findViewById(R.id.empty_view);


        if (assetsUrlResponseCode == 0) {
            Intent intent = new Intent(getActivity(), AuthActivity.class);
            startActivity(intent);
        }

        ListView assetsListView = (ListView) rootView.findViewById(R.id.assets_list);
        assetsDataAdapter = new AssetsDataAdapter(getActivity(), new ArrayList<AssetsData>());
        assetsListView.setAdapter(assetsDataAdapter);

        assetsListView.setEmptyView(mEmptyStateTextView);

        //Checking the connection using connectivityManager
        connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        if (activeNetwork != null && activeNetwork.isConnected()) {
            // Get a reference to the LoaderManager, in order to interact with loaders.
            loaderManager = getActivity().getLoaderManager();
            // Initialize the loader. Pass in the int ID constant defined above and pass in null for
            // the bundle. Pass in this activity for the LoaderCallbacks parameter (which is valid
            // because this activity implements the LoaderCallbacks interface).
            loaderManager.initLoader(ASSETS_LOADER_ID, null, this);
            Log.v(LOG_TAG, "initLoader");
        } else {
            progressCircle.setVisibility(View.GONE);
            mEmptyStateTextView.setText(R.string.no_connection);
        }

        //this will be made in the near future, i hope
        assetsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                //AssetsData assetsData = assetsDataAdapter.getItem(position);
            }
        });


        return rootView;

    }

    @Override
    public Loader<List<AssetsData>> onCreateLoader(int i, Bundle bundle) {
        // Create a new loader for the given URL
        Log.v(LOG_TAG, "onCreateLoader");
        return new AssetsLoader(getActivity(), ASSETS_REQUEST_URL);
    }

    @Override
    public void onLoadFinished(Loader<List<AssetsData>> loader, List<AssetsData> assetData) {
        // Set empty state text to display "No assets found."
        progressCircle.setVisibility(View.GONE);

        assetsDataAdapter.clear();
        if (assetsUrlResponseCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
            Intent intent = new Intent(getActivity(), AuthActivity.class);
            startActivity(intent);
            return;
        }

        if (assetData == null || assetData.isEmpty()) {
            connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
            if (activeNetwork != null && activeNetwork.isConnected()) {
            } else {
                mEmptyStateTextView.setText(R.string.no_connection);
                return;
            }
            if (progressCircle.getVisibility() == View.GONE) {
                mEmptyStateTextView.setText(R.string.no_assets);
                return;
            }
        }
        Log.v(LOG_TAG, "onLoadFinished");

        mAssetData = assetData;
        //mAssetDataAPIBefore26 = assetData;

        assetsDataAdapter.addAll(mAssetData);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mAssetData == null || mAssetData.isEmpty()) {
            connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
            if (activeNetwork != null && activeNetwork.isConnected()) {
                mEmptyStateTextView.setText(R.string.no_assets);
            } else {
                mEmptyStateTextView.setText(R.string.no_connection);
            }

        } else
            assetsDataAdapter.addAll(mAssetData);
    }

//    @Override
//    public void onConfigurationChanged(Configuration newConfig) {
//        super.onConfigurationChanged(newConfig);
//        // Checks the orientation of the screen
//        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.O) {
//            if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
//                mAssetData = mAssetDataAPIBefore26;
//                progressCircle.setVisibility(View.GONE);
//            } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
//                mAssetData = mAssetDataAPIBefore26;
//                progressCircle.setVisibility(View.GONE);
//            }
//        }
//    }

    //    if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.O) {
//
//    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        progressCircle.setVisibility(View.VISIBLE);


    }

    @Override
    public void onLoaderReset(Loader<List<AssetsData>> loader) {
        assetsDataAdapter.clear();
        Log.v(LOG_TAG, "onLoadReset");

    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }


}
