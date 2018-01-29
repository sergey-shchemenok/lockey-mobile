package ru.tradition.lockeymobile.obtainingassets;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
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

import ru.tradition.lockeymobile.AuthActivity;
import ru.tradition.lockeymobile.MainActivity;
import ru.tradition.lockeymobile.R;

import static ru.tradition.lockeymobile.UserData.mAssetData;
import static ru.tradition.lockeymobile.obtainingassets.AssetsQueryUtils.assetsUrlResponseCode;

/**
 * Created by Caelestis on 25.01.2018.
 */

public class AssetsFragment extends Fragment {

    private OnFragmentInteractionListener mListener;

    public AssetsFragment() {
        // Required empty public constructor
    }


    /**
     * TextView that is displayed when the list is empty
     */
    public static final String LOG_TAG = AssetsFragment.class.getName();

    private AssetsDataAdapter assetsDataAdapter;

    private ListView assetsListView;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.asset_list, container, false);

        assetsListView = (ListView) rootView.findViewById(R.id.assets_list);

        assetsDataAdapter = new AssetsDataAdapter(getActivity(), new ArrayList<AssetsData>());
        assetsListView.setAdapter(assetsDataAdapter);

        assetsDataAdapter.addAll(mAssetData);

        //todo this will be made in the near future, i hope
        assetsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                //AssetsData assetsData = assetsDataAdapter.getItem(position);
            }
        });

        return rootView;

    }

    //when activity is closed it don't need to update data
    @Override
    public void onStart() {
        MainActivity.sleepTime = 10000;
        super.onStart();
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
