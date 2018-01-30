package ru.tradition.lockeymobile;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import ru.tradition.lockeymobile.obtainingassets.AssetsData;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MapFragmentTab.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MapFragmentTab#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MapFragmentTab extends Fragment implements OnMapReadyCallback {

    private final String LOG_TAG = MapFragmentTab.class.getSimpleName();

    private static View rootView;

    GoogleMap m_map;
    boolean mapReady = false;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public MapFragmentTab() {
        // Required empty public constructor
    }

    MapFragmentTab fragment;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MapFragmentTab.
     */
    // TODO: Rename and change types and number of parameters
    public static MapFragmentTab newInstance(String param1, String param2) {
        MapFragmentTab fragment = new MapFragmentTab();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    SupportMapFragment mapFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*
        Note: You cannot inflate a layout into a fragment when that layout includes a <fragment>.
        Nested fragments are only supported when added to a fragment dynamically.
        This is perhaps the best solution to the problem
         */
        FragmentManager fm = getChildFragmentManager();
        mapFragment = (SupportMapFragment) fm.findFragmentByTag("mapFragment");
        if (mapFragment == null) {
            mapFragment = new SupportMapFragment();
            FragmentTransaction ft = fm.beginTransaction();
            ft.add(R.id.mapFragmentContainer, mapFragment, "mapFragment");
            ft.commit();
            fm.executePendingTransactions();
        }
        mapFragment.getMapAsync(this);


        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        /*
        Without this, there will be a big problem if we want to restart the app
        The solution was found here https://stackoverflow.com/questions/14083950/duplicate-id-tag-null-or-parent-id-with-another-fragment-for-com-google-androi/14695397#14695397
        In fact, the best solution is found, but this code can still come in handy
*/

//        if (rootView != null) {
//            ViewGroup parent = (ViewGroup) rootView.getParent();
//            if (parent != null)
//                parent.removeView(rootView);
//        }
//        try {
//            rootView = inflater.inflate(R.layout.tab_fragment_map, container, false);
//        } catch (InflateException e) {
//        /* map is already there, just return view as it is */
//        }

        rootView = inflater.inflate(R.layout.tab_fragment_map, container, false);

        // Inflate the layout for this fragment
        return rootView;
    }

    //Handler for map updating here
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mHandler = new Handler();
    }

    @Override
    public void onStart() {
        super.onStart();
        startRepeatingTask();
    }

    //Get data from mAssetData list and make marker from it
    @Override
    public void onMapReady(GoogleMap map) {
        mapReady = true;
        m_map = map;
        m_map.moveCamera(CameraUpdateFactory.newCameraPosition(UserData.target));

        try {
            for (AssetsData asset : UserData.mAssetData) {
                MarkerOptions marker = new MarkerOptions()
                        .position(new LatLng(asset.getLatitude(), asset.getLongitude()))
                        .title(String.valueOf(asset.getId()));
                if (asset.getLastSignalTime() < 15 && asset.getLastSignalTime() >= 0) {
                    marker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                }
                m_map.addMarker(marker);
            }
        } catch (NullPointerException e) {
            MainActivity.mainActivity.logout();
        }
    }


    //Save camera position
    @Override
    public void onStop() {
        try {
            UserData.target = m_map.getCameraPosition();
        } catch (NullPointerException e) {
            MainActivity.mainActivity.logout();
        }
        stopRepeatingTask();
        super.onStop();

    }

//    @Override
//    public void onDestroyView() {
//        MainActivity.isFinished = false;
//        MainActivity.isRepeated = false;
//        super.onDestroyView();
//    }

    //The code for map updating
    private int mInterval = 1000 * 10; // 5 seconds by default, can be changed later
    private Handler mHandler;

    Runnable mStatusChecker = new Runnable() {
        @Override
        public void run() {
            try {
                if (MainActivity.isFinished) {
                    MainActivity.mainActivity.repeatLoader();
                    MainActivity.isRepeated = true;
                    Log.i(LOG_TAG, "Repeating loading assets");
                }
                if (m_map != null) {
                    UserData.target = m_map.getCameraPosition();
                    m_map.clear();
                    m_map.moveCamera(CameraUpdateFactory.newCameraPosition(UserData.target));

                    for (AssetsData asset : UserData.mAssetData) {
                        MarkerOptions marker = new MarkerOptions()
                                .position(new LatLng(asset.getLatitude(), asset.getLongitude()))
                                .title(String.valueOf(asset.getId()));
                        if (asset.getLastSignalTime() < 15 && asset.getLastSignalTime() >= 0) {
                            marker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                        }
                        m_map.addMarker(marker);
                    }
                    Log.i(LOG_TAG, "The position of markers was updated");
                }
            } finally {
                // 100% guarantee that this always happens, even if
                // your update method throws an exception
                mHandler.postDelayed(mStatusChecker, mInterval);
            }
        }
    };

    void startRepeatingTask() {
        mStatusChecker.run();
    }

    void stopRepeatingTask() {
        mHandler.removeCallbacks(mStatusChecker);
    }


    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
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
