package ru.tradition.lockeymobile.tabs.assetstab;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;

import ru.tradition.lockeymobile.AssetActivity;
import ru.tradition.lockeymobile.MainActivity;
import ru.tradition.lockeymobile.R;
import ru.tradition.lockeymobile.UserData;

import static ru.tradition.lockeymobile.UserData.mAssetData;

/**
 * Created by Caelestis on 25.01.2018.
 */

public class AssetsFragment extends Fragment {

    private OnFragmentInteractionListener mListener;

    private int selectedAssetCounter = 0;

    private boolean isSelectedMode = false;

    // Required empty public constructor
    public AssetsFragment() {
    }

    public static final String LOG_TAG = AssetsFragment.class.getName();
    public static AssetsDataAdapter assetsDataAdapter;
    private ListView assetsListView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.asset_list, container, false);

        assetsListView = (ListView) rootView.findViewById(R.id.assets_list);

        assetsDataAdapter = new AssetsDataAdapter(getActivity(), new ArrayList<AssetsData>());
        assetsListView.setAdapter(assetsDataAdapter);


        //todo update
        try {
            assetsDataAdapter.addAll(new ArrayList<>(mAssetData.values()));
        } catch (NullPointerException e) {
            MainActivity.mainActivity.logout();
        }

        assetsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View viewItem, int position, long itemId) {
                if (!isSelectedMode) {
                    Intent intent = new Intent(getActivity(), AssetActivity.class);
                    //put data to intent
                    AssetsData as = (AssetsData) adapterView.getItemAtPosition(position);
                    intent.putExtra("AssetData", as);
                    startActivity(intent);
                } else {
                    AssetsData as = (AssetsData) adapterView.getItemAtPosition(position);
                    int id = as.getId();
                    if (!UserData.selectedAsset.contains(id)) {
                        UserData.selectedAsset.add(id);
                        MainActivity.mainActivity.setTitle(String.valueOf(++selectedAssetCounter));
                        MainActivity.mainActivity.updateListView();
                    } else {
                        UserData.selectedAsset.remove(id);
                        MainActivity.mainActivity.setTitle(String.valueOf(--selectedAssetCounter));
                        MainActivity.mainActivity.updateListView();
                    }
                }
            }
        });


        //todo we can use this feature
        assetsListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View viewItem, int position, long itemId) {
//                Toast.makeText(getContext(), String.valueOf(itemId) + "   " + String.valueOf(position),
//                        Toast.LENGTH_SHORT).show();
                // MainActivity.mainActivity.viewPager.setCurrentItem(1);
                isSelectedMode = true;
                AssetsData as = (AssetsData) adapterView.getItemAtPosition(position);
                int id = as.getId();
                UserData.selectedAsset.add(id);

                MainActivity.mainActivity.setTitle(String.valueOf(++selectedAssetCounter));
                MainActivity.mainActivity.updateListView();
                //todo let's continue tomorrow
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
