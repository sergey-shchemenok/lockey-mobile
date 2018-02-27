package ru.tradition.lockeymobile.tabs.assetstab;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

import ru.tradition.lockeymobile.AppData;
import ru.tradition.lockeymobile.AssetActivity;
import ru.tradition.lockeymobile.R;

import static ru.tradition.lockeymobile.AppData.mAssetData;

/**
 * Created by Caelestis on 25.01.2018.
 */

public class AssetsFragmentTab extends Fragment {

    private OnFragmentInteractionListener mListener;

    // Required empty public constructor
    public AssetsFragmentTab() {
    }

    public static final String LOG_TAG = AssetsFragmentTab.class.getName();
    public static AssetsDataAdapter assetsDataAdapter;
    private ListView assetsListView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.tab_fragment_asset_list, container, false);

        assetsListView = (ListView) rootView.findViewById(R.id.assets_list);

        assetsDataAdapter = new AssetsDataAdapter(getActivity(), new ArrayList<AssetsData>());
        assetsListView.setAdapter(assetsDataAdapter);


        //to prevent crash in some killing process situations
        try {
            assetsDataAdapter.addAll(new ArrayList<>(mAssetData.values()));
        } catch (NullPointerException e) {
            AppData.mainActivity.logout();
            Log.i(LOG_TAG, "onAssetsFragmentCreateView..........NullPointerException");

        }

        //selecting mode has the other title
        if (AppData.isSelectingMode) {
            AppData.mainActivity.setTitle(String.valueOf(AppData.selectedAssetCounter));
            AppData.mainActivity.setUpButton();
        }

        assetsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View viewItem, int position, long itemId) {
                //if it is normal mode go to asset activity
                if (!AppData.isSelectingMode) {
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
                        AppData.mainActivity.updateListView();
                    } else {
                        AppData.selectedAsset.remove(id);
                        AppData.mainActivity.setTitle("Выбрано: " + String.valueOf(--AppData.selectedAssetCounter));
                        AppData.mainActivity.updateListView();
                    }
                }
            }
        });


        //Long click changes mode into selecting
        //in selecting mode go to map
        assetsListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View viewItem, int position, long itemId) {
//                Toast.makeText(getContext(), String.valueOf(itemId) + "   " + String.valueOf(position),
//                        Toast.LENGTH_SHORT).show();
                // MainActivity.mainActivity.viewPager.setCurrentItem(1);
                if (!AppData.isSelectingMode) {
//                    AppData.isSelectingMode = true;
//
//                    AppData.mainActivity.setUpButton();
//
//                    AppData.mMenu.getItem(3).setVisible(true);
//                    AssetsData as = (AssetsData) adapterView.getItemAtPosition(position);
//                    int id = as.getId();
//                    AppData.selectedAsset.add(id);
//
//                    //todo change tab and toolbar color in selecting mode
//                    AppData.mainActivity.setTitle(String.valueOf(++AppData.selectedAssetCounter));
//                    AppData.mainActivity.updateListView();
                    AppData.mainActivity.updateListView();
                    AssetsData as = (AssetsData) adapterView.getItemAtPosition(position);
                    AppData.target = CameraPosition.builder()
                            .target(new LatLng(as.getLatitude(), as.getLongitude()))
                            .zoom(13)
                            .build();
                    //go to map tab
                    AppData.viewPager.setCurrentItem(1);
                    AppData.m_map.moveCamera(CameraUpdateFactory.newCameraPosition(AppData.target));

                }else {
                    AppData.mainActivity.changeModeToNormal();
                    AppData.mainActivity.updateListView();
                    AssetsData as = (AssetsData) adapterView.getItemAtPosition(position);
                    AppData.target = CameraPosition.builder()
                            .target(new LatLng(as.getLatitude(), as.getLongitude()))
                            .zoom(13)
                            .build();
                    //go to map tab
                    AppData.viewPager.setCurrentItem(1);
                    AppData.m_map.moveCamera(CameraUpdateFactory.newCameraPosition(AppData.target));
                }
                return true;
            }
        });
        return rootView;
    }

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
