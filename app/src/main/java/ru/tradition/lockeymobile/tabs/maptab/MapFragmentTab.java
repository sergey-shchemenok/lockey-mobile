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
    private LinearLayout geoFenceBottomSheet;
    public static BottomSheetBehavior bottomSheetBehavior;

    private LinearLayoutManager layoutManager;


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
        geoFenceBottomSheet = (LinearLayout) rootView.findViewById(R.id.bottom_sheet);
        // init the bottom sheet behavior
        bottomSheetBehavior = BottomSheetBehavior.from(geoFenceBottomSheet);
        // set the peek height
//        bottomSheetBehavior.setPeekHeight(340);
        // set hideable or not
        bottomSheetBehavior.setHideable(true);
//        bottomSheetBehavior.setSkipCollapsed(true);
        // set callback for changes
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
//                if (BottomSheetBehavior.STATE_DRAGGING == newState) {
//                    fabBottomDrawer.animate().scaleX(0).scaleY(0).setDuration(300).start();
//                } else if (BottomSheetBehavior.STATE_COLLAPSED == newState || BottomSheetBehavior.STATE_HIDDEN == newState) {
//                    fabBottomDrawer.animate().scaleX(1).scaleY(1).setDuration(300).start();
//                }
                if (BottomSheetBehavior.STATE_HIDDEN == newState) {
                    fabBottomDrawer.animate().scaleX(1).scaleY(1).setDuration(100).start();
                    clearPolygonSet();
                    mAdapter.notifyDataSetChanged();
                }

                if (BottomSheetBehavior.STATE_EXPANDED == newState) {
                    LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
                    mPolygonsList.setLayoutManager(layoutManager);
                    mPolygonsList.setHasFixedSize(true);
                    mAdapter = new GeofencePolygonAdapter(AppData.polygonsList, MapFragmentTab.this);
                    mPolygonsList.setAdapter(mAdapter);
                    fabLayers.hide();
                }
                if (BottomSheetBehavior.STATE_COLLAPSED == newState) {
                    LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
                    mPolygonsList.setLayoutManager(layoutManager);
                    mPolygonsList.setHasFixedSize(true);
                    mAdapter = new GeofencePolygonAdapter(AppData.polygonsList, MapFragmentTab.this);
                    mPolygonsList.setAdapter(mAdapter);
                    fabLayers.show();
                }

                if (BottomSheetBehavior.STATE_HIDDEN == newState) {
                    fabLayers.show();
                }


            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                bottomSheetBehavior.getPeekHeight();
//                if (slideOffset >= 0) {
//                    fabBottomDrawer.animate().scaleX(1 - slideOffset).scaleY(1 - slideOffset).setDuration(0).start();
//                }
                //fabBottomDrawer.animate().scaleX(1 - Math.abs(slideOffset)).scaleY(1 - Math.abs(slideOffset)).setDuration(0).start();

            }
        });

        fabBottomDrawer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
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

        mAdapter = new GeofencePolygonAdapter(AppData.polygonsList, this);
        //Log.i(LOG_TAG, "Polygons set" + AppData.polygonsList.toString());
        mPolygonsList.setAdapter(mAdapter);

        //to remove zones after rotation
        clearPolygonSet();
        return rootView;
    }

    //helper method to clear polygon set
    private void clearPolygonSet() {
        if (!polygons.isEmpty()) {
            for (Map.Entry<Integer, Polygon> pair : polygons.entrySet())
                pair.getValue().remove();
            polygons.clear();
        }
        polygonNamesNumber = -1;
    }

    @Override
    public void onListItemClick(int clickedItemIndex) {
        if (AppData.m_map != null) {
            if (!polygons.isEmpty()) {
                for (Map.Entry<Integer, Polygon> pair : polygons.entrySet())
                    pair.getValue().remove();
                polygons.clear();
            }
            if (polygonNamesNumber != clickedItemIndex) {
                GeofencePolygon geof = AppData.polygonsList.get(clickedItemIndex);
                LatLng[] latLngArray = geof.getPolygon();

                PolygonOptions popt = new PolygonOptions().geodesic(true);

                for (LatLng point : latLngArray) {
                    popt.add(point);
                }
                Polygon geozone = AppData.m_map.addPolygon(popt);
                polygons.put(geof.getGeofence_id(), geozone);
                geozone.setFillColor(Color.parseColor("#6421a30d"));
                geozone.setStrokeColor(Color.parseColor("#FF21A30D"));
                int lastPosition = polygonNamesNumber;
                polygonNamesNumber = clickedItemIndex;
                if (lastPosition >= 0) {
//                    GeofencePolygonAdapter.PolygonNameViewHolder vh = (GeofencePolygonAdapter.PolygonNameViewHolder) mPolygonsList.findViewHolderForAdapterPosition(lastPosition);
//                    mAdapter.onBindViewHolder(vh, lastPosition);
                    mAdapter.notifyDataSetChanged();
                }

            } else {
                polygonNamesNumber = -1;
            }
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
        startMarkerUpdating();
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
        stopMarkerUpdating();
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

    Runnable mMarkerPositionUpdater = new Runnable() {
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
                mHandler.postDelayed(mMarkerPositionUpdater, mInterval);
            }
        }
    };

    void startMarkerUpdating() {
        mMarkerPositionUpdater.run();
    }

    void stopMarkerUpdating() {
        mHandler.removeCallbacks(mMarkerPositionUpdater);
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
