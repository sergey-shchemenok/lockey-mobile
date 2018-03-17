package ru.tradition.lockeymobile.tabs.maptab;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;


import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import ru.tradition.lockeymobile.AppData;
import ru.tradition.lockeymobile.R;
import ru.tradition.lockeymobile.tabs.assetstab.AssetsData;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MapFragmentTab.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MapFragmentTab#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MapFragmentTab extends Fragment implements OnMapReadyCallback,
        GeofencePolygonAdapter.ListItemClickListener {

    public static GeofencePolygonAdapter mAdapter;
    private RecyclerView mPolygonsList;

    private Toast mToast;

    private final String LOG_TAG = MapFragmentTab.class.getSimpleName();
    private static View rootView;

    boolean mapReady = false;

    //add fabLayers button
    private FloatingActionButton fabLayers;

    //add fab and other elements for bottom drawer
    private FloatingActionButton fabBottomDrawer;
    private LinearLayout llBottomSheet;
    public static BottomSheetBehavior bottomSheetBehavior;


    private int mapType = GoogleMap.MAP_TYPE_NORMAL;

    private static TreeMap<Integer, Marker> markers = new TreeMap<>();
    private static TreeMap<Integer, Polygon> polygons = new TreeMap<>();
    public static int polygonNamesNumber = -1;

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1;
    private String mParam2;
    private OnFragmentInteractionListener mListener;

    public MapFragmentTab() {
        // Required empty public constructor
    }

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

        rootView = inflater.inflate(R.layout.tab_fragment_map_drawer, container, false);
        // Inflate the layout for this fragment

        fabLayers = (FloatingActionButton) rootView.findViewById(R.id.fab_layers);

        fabBottomDrawer = (FloatingActionButton) rootView.findViewById(R.id.fab_bottom_drawer);


        llBottomSheet = (LinearLayout) rootView.findViewById(R.id.bottom_sheet);

        // init the bottom sheet behavior
        bottomSheetBehavior = BottomSheetBehavior.from(llBottomSheet);

        // set the peek height
//        bottomSheetBehavior.setPeekHeight(340);

        // set hideable or not
        bottomSheetBehavior.setHideable(true);

//        bottomSheetBehavior.setSkipCollapsed(true);

        // set callback for changes
        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
//                if (BottomSheetBehavior.STATE_DRAGGING == newState) {
//                    fabBottomDrawer.animate().scaleX(0).scaleY(0).setDuration(300).start();
//                } else if (BottomSheetBehavior.STATE_COLLAPSED == newState || BottomSheetBehavior.STATE_HIDDEN == newState) {
//                    fabBottomDrawer.animate().scaleX(1).scaleY(1).setDuration(300).start();
//                }
                if (BottomSheetBehavior.STATE_COLLAPSED == newState || BottomSheetBehavior.STATE_HIDDEN == newState) {
                    fabBottomDrawer.animate().scaleX(1).scaleY(1).setDuration(100).start();
                    if (!polygons.isEmpty()) {
                        for (Map.Entry<Integer, Polygon> pair : polygons.entrySet())
                            pair.getValue().remove();
                        polygons.clear();
                    }
                    polygonNamesNumber = -1;
                }


//                if (BottomSheetBehavior.STATE_EXPANDED == newState) {
//                    if (AppData.m_map != null) {
//                        for (Map.Entry<Integer, AssetsData> pair : AppData.mAssetData.entrySet()) {
//                            int id = pair.getKey();
//                            Marker savedMarker = markers.get(id);
//                            //need this check in case killing process
//                            if (savedMarker != null) {
//                                savedMarker.setAlpha(0.5f);
//                            }
//                        }
//                        Log.i(LOG_TAG, "The markers changed colors");
//
//                    } else
//                        Log.i(LOG_TAG, "the map................. is null");
//                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                bottomSheetBehavior.getPeekHeight();
                if (slideOffset >= 0)
                    fabBottomDrawer.animate().scaleX(1 - slideOffset).scaleY(1 - slideOffset).setDuration(0).start();
                //fabBottomDrawer.animate().scaleX(1 - Math.abs(slideOffset)).scaleY(1 - Math.abs(slideOffset)).setDuration(0).start();

            }
        });

        fabBottomDrawer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        });


        fabLayers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mapReady && AppData.m_map != null) {
                    if (mapType == GoogleMap.MAP_TYPE_NORMAL) {
                        AppData.m_map.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                        mapType = GoogleMap.MAP_TYPE_SATELLITE;
                    } else if (mapType == GoogleMap.MAP_TYPE_SATELLITE) {
                        AppData.m_map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                        mapType = GoogleMap.MAP_TYPE_HYBRID;
                    } else {
                        AppData.m_map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                        mapType = GoogleMap.MAP_TYPE_NORMAL;
                    }
                }
            }
        });

        fabLayers.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                for (Map.Entry<Integer, Marker> pair : markers.entrySet()) {
                    Marker marker = pair.getValue();
                    builder.include(marker.getPosition());
                }
                LatLngBounds bounds = builder.build();

                int padding = 5; // offset from edges of the map in pixels
                CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);

                AppData.m_map.animateCamera(cu);
                return true;
            }
        });


        mPolygonsList = (RecyclerView) rootView.findViewById(R.id.geofence_polygons);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        mPolygonsList.setLayoutManager(layoutManager);

        mPolygonsList.setHasFixedSize(true);

        fakeList = new ArrayList<>();
        fakeList.add(new GeofencePolygon(21, "МКАД1",
                new LatLng(55.572560364514594, 37.60004794177246),
                new LatLng(55.59429373522563, 37.50666415270996),
                new LatLng(55.63539896560563, 37.45997225817871),
                new LatLng(55.72985487342488, 37.373454924194334),
                new LatLng(55.8618643459282, 37.400920744506834),
                new LatLng(55.910386676275145, 37.56983553942871),
                new LatLng(55.88959881965586, 37.70991122302246),
                new LatLng(55.819453970015424, 37.83900057849121),
                new LatLng(55.647024698634446, 37.83625399645996)
        ));
        fakeList.add(new GeofencePolygon(25, "Нижний Новгород",
                new LatLng(55.572560364514594, 37.60004794177246),
                new LatLng(55.59429373522563, 37.50666415270996),
                new LatLng(55.72985487342488, 37.373454924194334),
                new LatLng(55.8618643459282, 37.400920744506834),
                new LatLng(55.88959881965586, 37.70991122302246),
                new LatLng(55.819453970015424, 37.83900057849121)
        ));
        fakeList.add(new GeofencePolygon(28, "Офис",
                new LatLng(55.59429373522563, 37.50666415270996),
                new LatLng(55.63539896560563, 37.45997225817871),
                new LatLng(55.8618643459282, 37.400920744506834),
                new LatLng(55.910386676275145, 37.56983553942871),
                new LatLng(55.819453970015424, 37.83900057849121)
        ));
        fakeList.add(new GeofencePolygon(31, "МКАД",
                new LatLng(55.572560364514594, 37.60004794177246),
                new LatLng(55.59429373522563, 37.50666415270996),
                new LatLng(55.63539896560563, 37.45997225817871),
                new LatLng(55.72985487342488, 37.373454924194334),
                new LatLng(55.8618643459282, 37.400920744506834),
                new LatLng(55.910386676275145, 37.56983553942871),
                new LatLng(55.88959881965586, 37.70991122302246),
                new LatLng(55.819453970015424, 37.83900057849121),
                new LatLng(55.647024698634446, 37.83625399645996)
        ));
        fakeList.add(new GeofencePolygon(35, "Стройка 12",
                new LatLng(55.572560364514594, 37.60004794177246),
                new LatLng(55.59429373522563, 37.50666415270996),
                new LatLng(55.72985487342488, 37.373454924194334),
                new LatLng(55.8618643459282, 37.400920744506834),
                new LatLng(55.88959881965586, 37.70991122302246),
                new LatLng(55.819453970015424, 37.83900057849121)
        ));
        fakeList.add(new GeofencePolygon(38, "Московская область",
                new LatLng(55.59429373522563, 37.50666415270996),
                new LatLng(55.63539896560563, 37.45997225817871),
                new LatLng(55.8618643459282, 37.400920744506834),
                new LatLng(55.910386676275145, 37.56983553942871),
                new LatLng(55.819453970015424, 37.83900057849121)
        ));
        fakeList.add(new GeofencePolygon(41, "МКАД7",
                new LatLng(55.572560364514594, 37.60004794177246),
                new LatLng(55.59429373522563, 37.50666415270996),
                new LatLng(55.63539896560563, 37.45997225817871),
                new LatLng(55.72985487342488, 37.373454924194334),
                new LatLng(55.8618643459282, 37.400920744506834),
                new LatLng(55.910386676275145, 37.56983553942871),
                new LatLng(55.88959881965586, 37.70991122302246),
                new LatLng(55.819453970015424, 37.83900057849121),
                new LatLng(55.647024698634446, 37.83625399645996)
        ));
        fakeList.add(new GeofencePolygon(45, "МКАД8",
                new LatLng(55.572560364514594, 37.60004794177246),
                new LatLng(55.59429373522563, 37.50666415270996),
                new LatLng(55.72985487342488, 37.373454924194334),
                new LatLng(55.8618643459282, 37.400920744506834),
                new LatLng(55.88959881965586, 37.70991122302246),
                new LatLng(55.819453970015424, 37.83900057849121)
        ));
        fakeList.add(new GeofencePolygon(48, "МКАД9",
                new LatLng(55.59429373522563, 37.50666415270996),
                new LatLng(55.63539896560563, 37.45997225817871),
                new LatLng(55.8618643459282, 37.400920744506834),
                new LatLng(55.910386676275145, 37.56983553942871),
                new LatLng(55.819453970015424, 37.83900057849121)
        ));
        fakeList.add(new GeofencePolygon(51, "МКАД10",
                new LatLng(55.572560364514594, 37.60004794177246),
                new LatLng(55.59429373522563, 37.50666415270996),
                new LatLng(55.63539896560563, 37.45997225817871),
                new LatLng(55.72985487342488, 37.373454924194334),
                new LatLng(55.8618643459282, 37.400920744506834),
                new LatLng(55.910386676275145, 37.56983553942871),
                new LatLng(55.88959881965586, 37.70991122302246),
                new LatLng(55.819453970015424, 37.83900057849121),
                new LatLng(55.647024698634446, 37.83625399645996)
        ));
        fakeList.add(new GeofencePolygon(55, "МКАД11",
                new LatLng(55.572560364514594, 37.60004794177246),
                new LatLng(55.59429373522563, 37.50666415270996),
                new LatLng(55.72985487342488, 37.373454924194334),
                new LatLng(55.8618643459282, 37.400920744506834),
                new LatLng(55.88959881965586, 37.70991122302246),
                new LatLng(55.819453970015424, 37.83900057849121)
        ));
        fakeList.add(new GeofencePolygon(58, "МКАД12",
                new LatLng(55.59429373522563, 37.50666415270996),
                new LatLng(55.63539896560563, 37.45997225817871),
                new LatLng(55.8618643459282, 37.400920744506834),
                new LatLng(55.910386676275145, 37.56983553942871),
                new LatLng(55.819453970015424, 37.83900057849121)
        ));

        mAdapter = new GeofencePolygonAdapter(fakeList, this);
        mPolygonsList.setAdapter(mAdapter);

        //to remove zones after rotation
        if (!polygons.isEmpty()) {
            for (Map.Entry<Integer, Polygon> pair : polygons.entrySet())
                pair.getValue().remove();
            polygons.clear();
        }
        polygonNamesNumber = -1;

        return rootView;
    }

    private ArrayList<GeofencePolygon> fakeList;


    @Override
    public void onListItemClick(int clickedItemIndex) {
        if (mToast != null) {
            mToast.cancel();
        }
        String toastMessage = "Item #" + clickedItemIndex + " clicked.";
        mToast = Toast.makeText(getActivity(), toastMessage, Toast.LENGTH_LONG);

        mToast.show();

        if (AppData.m_map != null) {
            if (!polygons.isEmpty()) {
                for (Map.Entry<Integer, Polygon> pair : polygons.entrySet())
                    pair.getValue().remove();
                polygons.clear();
            }
            if (polygonNamesNumber != clickedItemIndex) {
                GeofencePolygon geof = fakeList.get(clickedItemIndex);
                LatLng[] latLng = geof.getPolygon();

                PolygonOptions popt = new PolygonOptions().geodesic(true);

                for (LatLng ll : latLng) {
                    popt.add(ll);
                }
                Polygon p = AppData.m_map.addPolygon(popt);
                polygons.put(geof.getGeofence_id(), p);
                p.setFillColor(Color.parseColor("#6421a30d"));
                p.setStrokeColor(Color.parseColor("#FF21A30D"));
                int lastPosition = polygonNamesNumber;
                polygonNamesNumber = clickedItemIndex;
                if (lastPosition >= 0) {
                    GeofencePolygonAdapter.PolygonNameViewHolder vh = (GeofencePolygonAdapter.PolygonNameViewHolder) mPolygonsList.findViewHolderForAdapterPosition(lastPosition);
                    mAdapter.onBindViewHolder(vh, lastPosition);
                }

            } else {
                polygonNamesNumber = -1;
            }

//            mAdapter = new GeofencePolygonAdapter(fakeList, this);
//            mPolygonsList.setAdapter(mAdapter);
//            GeofencePolygonAdapter.PolygonNameViewHolder vh = (GeofencePolygonAdapter.PolygonNameViewHolder) mPolygonsList.findViewHolderForAdapterPosition(clickedItemIndex);
//            mAdapter.onBindViewHolder(vh, clickedItemIndex);


        }
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
        AppData.m_map = map;
        AppData.m_map.moveCamera(CameraUpdateFactory.newCameraPosition(AppData.target));

        //clicking on marker
        AppData.m_map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                Toast.makeText(getContext(), marker.getTitle() + " " + marker.getPosition(), Toast.LENGTH_LONG).show();
                return true;
            }
        });

        try {
            markers.clear();
            for (Map.Entry<Integer, AssetsData> pair : AppData.mAssetData.entrySet()) {
                MarkerOptions marker = new MarkerOptions()
                        .position(new LatLng(pair.getValue().getLatitude(), pair.getValue().getLongitude()))
                        .title(String.valueOf(pair.getValue().getId()));
                if (pair.getValue().getLastSignalTime() < 15 && pair.getValue().getLastSignalTime() >= 0) {
                    marker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                }
                markers.put(pair.getKey(), AppData.m_map.addMarker(marker));
            }
        } catch (NullPointerException e) {
            AppData.mainActivity.logout();
            Log.i(LOG_TAG, "onMapReady..........NullPointerException");
        }
    }


    //Save camera position
    @Override
    public void onStop() {
        try {
            AppData.target = AppData.m_map.getCameraPosition();
        } catch (NullPointerException e) {
            AppData.mainActivity.logout();
            Log.i(LOG_TAG, "onMapStop..........NullPointerException");
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
    private int mInterval = 1000 * 5; // 5 seconds by default, can be changed later
    private Handler mHandler;

    Runnable mStatusChecker = new Runnable() {
        @Override
        public void run() {
            try {
                //first we need to update data
                if (AppData.isFinished) {
                    AppData.mainActivity.repeatLoader();
                    Log.i(LOG_TAG, "Repeating loading assets");
                }

                if (AppData.m_map != null) {
                    AppData.target = AppData.m_map.getCameraPosition();
                    //m_map.clear();
                    AppData.m_map.moveCamera(CameraUpdateFactory.newCameraPosition(AppData.target));

                    //updating marker position
                    for (Map.Entry<Integer, AssetsData> pair : AppData.mAssetData.entrySet()) {
                        int id = pair.getKey();
                        Marker savedMarker = markers.get(id);
                        LatLng savedPosition;
                        //need this check in case killing process
                        if (savedMarker != null) {
                            savedPosition = savedMarker.getPosition();
                            //compare saved and new position
                            LatLng newPosition = new LatLng(pair.getValue().getLatitude(), pair.getValue().getLongitude());
                            if (!savedPosition.equals(newPosition)) {
                                MarkerOptions marker = new MarkerOptions()
                                        .position(newPosition)
                                        .title(String.valueOf(pair.getValue().getId()));
                                if (pair.getValue().getLastSignalTime() < 15 && pair.getValue().getLastSignalTime() >= 0) {
                                    marker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                                }
                                savedMarker.remove();
                                markers.remove(id);
                                markers.put(id, AppData.m_map.addMarker(marker));
                                Log.i(LOG_TAG, "The markers have moved");
                            } else {
                                if (pair.getValue().getLastSignalTime() < 15 && pair.getValue().getLastSignalTime() >= 0) {
                                    savedMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                                } else
                                    savedMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                            }
                        }
                    }
                    Log.i(LOG_TAG, String.valueOf(AppData.isFinished));
                    Log.i(LOG_TAG, String.valueOf(AppData.isRepeated));
                    Log.i(LOG_TAG, "The position of markers was updated");

                } else
                    Log.i(LOG_TAG, "the map................. is null");

            } finally {
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
