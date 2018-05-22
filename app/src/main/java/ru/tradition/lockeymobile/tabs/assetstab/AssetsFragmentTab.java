package ru.tradition.lockeymobile.tabs.assetstab;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import java.util.Collections;

import ru.tradition.lockeymobile.AppData;
import ru.tradition.lockeymobile.AssetActivity;
import ru.tradition.lockeymobile.MainActivity;
import ru.tradition.lockeymobile.R;
import ru.tradition.lockeymobile.tabs.maptab.MapFragmentTab;
import ru.tradition.lockeymobile.tabs.maptab.MapFragmentTabOSM;

import static ru.tradition.lockeymobile.AppData.mAssetMap;

/**
 * Created by Caelestis on 25.01.2018.
 */

public class AssetsFragmentTab extends Fragment {

    private OnFragmentInteractionListener mListener;

    // Required empty public constructor
    public AssetsFragmentTab() {
    }

    public static AssetsFragmentTab aft;

    public static final String LOG_TAG = AssetsFragmentTab.class.getName();
    public static AssetsDataAdapter assetsDataAdapter;
    private ListView assetsListView;

    public TextView mEmptyStateTextView;
    public ProgressBar progressCircle;

    private static String orderBy;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        aft = this;
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        orderBy = sharedPrefs.getString(
                getString(R.string.settings_order_by_key),
                getString(R.string.settings_order_by_default)
        );
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.tab_fragment_asset_list, container, false);

        assetsListView = (ListView) rootView.findViewById(R.id.assets_list);

        progressCircle = (ProgressBar) rootView.findViewById(R.id.asset_tab_loading_spinner);
        mEmptyStateTextView = (TextView) rootView.findViewById(R.id.asset_tab_empty_view);
        progressCircle.setVisibility(View.VISIBLE);

        assetsDataAdapter = new AssetsDataAdapter(getActivity(), new ArrayList<AssetsData>());
        assetsListView.setAdapter(assetsDataAdapter);


        try {
            updateListView();
        } catch (NullPointerException e) {
        }

        //selecting mode has the other title
        if (AppData.isAssetSelectingMode) {
            AppData.mainActivity.setTitle("Выбрано: " + String.valueOf(AppData.selectedAssetCounter));
            AppData.mainActivity.setUpButton();
            AppData.mMenu.getItem(1).setVisible(false);
        }

        assetsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View viewItem, int position, long itemId) {
                //if it is normal mode go to asset activity
                if (!AppData.isAssetSelectingMode) {
                    Intent intent = new Intent(getActivity(), AssetActivity.class);
                    //put data to intent
                    AssetsData as = (AssetsData) adapterView.getItemAtPosition(position);
                    intent.putExtra("AssetData", as);
                    startActivity(intent);
                }
                //if it is selecting mode. Select or deselect asset
                else {
                    AssetsData as = (AssetsData) adapterView.getItemAtPosition(position);
                    int id = as.getId();
                    if (!AppData.selectedAsset.contains(id)) {
                        AppData.selectedAsset.add(id);
                        AppData.mainActivity.setTitle("Выбрано: " + String.valueOf(++AppData.selectedAssetCounter));
                        assetsDataAdapter.notifyDataSetChanged();
                    } else {
                        AppData.selectedAsset.remove(id);
                        AppData.mainActivity.setTitle("Выбрано: " + String.valueOf(--AppData.selectedAssetCounter));
                        assetsDataAdapter.notifyDataSetChanged();
                    }
                }
//                AppData.mainActivity.getZones();

            }
        });


        //Long click changes mode into selecting
        //in selecting mode go to map
        assetsListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View viewItem, int position, long itemId) {
                if (AppData.isAssetSelectingMode) {
                    AppData.mainActivity.changeModeToNormal();
                }
                assetsDataAdapter.notifyDataSetChanged();
                AssetsData as = (AssetsData) adapterView.getItemAtPosition(position);
                AppData.viewPager.setCurrentItem(1);

                if (MapFragmentTab.google_map != null && MainActivity.useMap.equals(getString(R.string.settings_google_map_value))) {
                    AppData.target = CameraPosition.builder()
                            .target(new LatLng(as.getLatitude(), as.getLongitude()))
                            .zoom(14)
                            .build();
                    MapFragmentTab.google_map.moveCamera(CameraUpdateFactory.newCameraPosition(AppData.target));
                } else if (MapFragmentTabOSM.mapController != null && MainActivity.useMap.equals(getString(R.string.settings_osm_value))) {
                    AppData.osmCameraZoom = 16.0;
                    AppData.osmStartPoint = new GeoPoint(as.getLatitude(), as.getLongitude());
                    MapFragmentTabOSM.mapController.setZoom(AppData.osmCameraZoom);
                    MapFragmentTabOSM.mapController.setCenter(AppData.osmStartPoint);
                }
                return true;
            }
        });

        if (AppData.mAssetMap != null && !AppData.mAssetMap.isEmpty())
            progressCircle.setVisibility(View.GONE);


        return rootView;
    }

    //update the list of assets
    public void updateListView() {
        AssetsFragmentTab.assetsDataAdapter.clear();
        //AssetsFragmentTab.assetsDataAdapter.notifyDataSetChanged();
        //this fragment can be detached
        if (isAdded()) {
            Log.i(LOG_TAG, "order by list..........." + getString(R.string.settings_order_by_kit_id_value));
            // etc ...
            if (orderBy.equals(getString(R.string.settings_order_by_kit_id_value))) {
                AssetsFragmentTab.assetsDataAdapter.addAll(new ArrayList<>(mAssetMap.values()));
            } else if (orderBy.equals(getString(R.string.settings_order_by_signal_time_value))) {
                ArrayList<AssetsData> ads = new ArrayList<>(mAssetMap.values());
                Collections.sort(ads, AssetsData.COMPARE_BY_LAST_SIGNAL_TIME);
                AssetsFragmentTab.assetsDataAdapter.addAll(ads);
            }
        }
    }

//    @Override
//    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//        super.onCreateOptionsMenu(menu, inflater);
//        menu.add("add")
//                .setIcon(R.drawable.ic_delete_forever)
//                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
//
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        String itemTitle = item.getTitle().toString();
//
//        switch (itemTitle) {
//
//            case "add":
//
//                AppData.mainActivity.logout();
//                // Do Activity menu item stuff here
//                return true;
//
//            default:
//                break;
//        }
//
//        return super.onOptionsItemSelected(item);
//
//    }

    //    @Override
//    public void onStop() {
//        MainActivity.mainActivity.setTitle(R.string.app_name);
//        super.onStop();
//    }

//    @Override
//    public void onDestroyView() {
//        MainActivity.mainActivity.setTitle(R.string.app_name);
//        super.onDestroyView();
//    }

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
