package ru.tradition.lockeymobile.tabs.maptab;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
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

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import ru.tradition.lockeymobile.AppData;
import ru.tradition.lockeymobile.AuthActivity;
import ru.tradition.lockeymobile.R;
import ru.tradition.lockeymobile.tabs.assetstab.AssetsData;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MapFragmentTabOSM.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MapFragmentTabOSM#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MapFragmentTabOSM extends Fragment implements
        GeofencePolygonAdapter.ListItemClickListener,
        GeofencePolygonAdapter.ListItemLongClickListener {

    //Open Street Map
    public static MapView osm_map;
    public static IMapController mapController;
    public static TreeMap<Integer, org.osmdroid.views.overlay.Marker> osm_markers = new TreeMap<>();
    //**

    public static GeofencePolygonAdapter mAdapter;
    private RecyclerView mPolygonsList;

    private final String LOG_TAG = MapFragmentTabOSM.class.getSimpleName();
    private static View rootView;


    //add fabLayers button
    private FloatingActionButton fabLayers;

    //add fab and other elements for bottom drawer
    private FloatingActionButton fabBottomDrawer;
    private LinearLayout geoFenceBottomSheet;
    public static BottomSheetBehavior bottomSheetBehavior;

    private LinearLayoutManager layoutManager;
    private LinearLayout googleMapFragmentContainer;
    private int recyclerViewScrollState = 0;


    private static TreeMap<Integer, Polygon> polygons = new TreeMap<>();
    public static int polygonNamesNumber = -1;

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1;
    private String mParam2;
    private OnFragmentInteractionListener mListener;

    public MapFragmentTabOSM() {
    }

    public static MapFragmentTabOSM newInstance(String param1, String param2) {
        MapFragmentTabOSM fragment = new MapFragmentTabOSM();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

//    SupportMapFragment osmMapFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            Context ctx = AppData.mainActivity.getApplicationContext();
            Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        } catch (NullPointerException e) {
            startActivity(new Intent(getActivity(), AuthActivity.class));

        }

        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.tab_fragment_map_drawer, container, false);

        googleMapFragmentContainer = (LinearLayout) rootView.findViewById(R.id.google_mapFragmentContainer);
        googleMapFragmentContainer.setVisibility(View.GONE);

        osm_map = (MapView) rootView.findViewById(R.id.osm_map);
        osm_map.setTileSource(TileSourceFactory.MAPNIK);

        //Then we add default zoom buttons, and ability to zoom with 2 fingers (multi-touch)
        osm_map.setBuiltInZoomControls(true);
        osm_map.setMultiTouchControls(true);
        //todo process it later

        //We can move the map on a default view point. For this, we need access to the map controller:
        mapController = osm_map.getController();
        mapController.setZoom(AppData.osmCameraZoom);
        mapController.setCenter(AppData.osmStartPoint);

//        Log.i(LOG_TAG, "osm start coordinates  " + osm_map.getMapCenter().getLatitude() + " "
//                + osm_map.getMapCenter().getLongitude());

        //******


        // Inflate the layout for this fragment
        fabLayers = (FloatingActionButton) rootView.findViewById(R.id.fab_layers);
        fabBottomDrawer = (FloatingActionButton) rootView.findViewById(R.id.fab_bottom_drawer);
        geoFenceBottomSheet = (LinearLayout) rootView.findViewById(R.id.bottom_sheet);
        bottomSheetBehavior = BottomSheetBehavior.from(geoFenceBottomSheet);
        bottomSheetBehavior.setHideable(true);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (BottomSheetBehavior.STATE_HIDDEN == newState) {
                    fabLayers.show();
                    fabBottomDrawer.show();
                    clearPolygonSet();
                    mAdapter.notifyDataSetChanged();
                    osm_map.setBuiltInZoomControls(true);
                }

                if (BottomSheetBehavior.STATE_EXPANDED == newState) {
                    recyclerViewScrollState = layoutManager.findFirstCompletelyVisibleItemPosition();
                    layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                    mAdapter = new GeofencePolygonAdapter(new ArrayList<>(AppData.mPolygonsMap.values()), MapFragmentTabOSM.this, MapFragmentTabOSM.this);
                    mPolygonsList.setAdapter(mAdapter);
                    mPolygonsList.scrollToPosition(recyclerViewScrollState);
                    fabLayers.hide();
                    fabBottomDrawer.hide();
                    osm_map.setBuiltInZoomControls(false);
                }
                if (BottomSheetBehavior.STATE_COLLAPSED == newState) {
                    recyclerViewScrollState = layoutManager.findFirstCompletelyVisibleItemPosition();
                    layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
                    mAdapter = new GeofencePolygonAdapter(new ArrayList<>(AppData.mPolygonsMap.values()), MapFragmentTabOSM.this, MapFragmentTabOSM.this);
                    mPolygonsList.setAdapter(mAdapter);
                    mPolygonsList.scrollToPosition(recyclerViewScrollState);
                    fabLayers.show();
                    fabBottomDrawer.hide();
                    osm_map.setBuiltInZoomControls(false);
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                bottomSheetBehavior.getPeekHeight();
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
                //todo change map mode

            }
        });

        fabLayers.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                //todo centre the markers

                return true;
            }
        });


        mPolygonsList = (RecyclerView) rootView.findViewById(R.id.geofence_polygons);
        layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        mPolygonsList.setLayoutManager(layoutManager);
        mPolygonsList.setHasFixedSize(true);
        if (AppData.mPolygonsMap != null && !AppData.mPolygonsMap.isEmpty()) {
            mAdapter = new GeofencePolygonAdapter(new ArrayList<>(AppData.mPolygonsMap.values()), this, this);
            mPolygonsList.setAdapter(mAdapter);
        }
        //to remove zones after rotation
        clearPolygonSet();

        //for osm map
        osmGetDataAndShowMarkers();
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
        //todo emphasize polygon

    }

    @Override
    public void onListItemLongClick(int clickedItemIndex) {
        if (osm_map != null) {
            if (polygonNamesNumber != clickedItemIndex) {
                this.onListItemClick(clickedItemIndex);

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

    /*
    for osm map
    */
    public void osmGetDataAndShowMarkers() {
        try {
            for (Map.Entry<Integer, AssetsData> pair : AppData.mAssetMap.entrySet()) {
                org.osmdroid.views.overlay.Marker osmMarker = new org.osmdroid.views.overlay.Marker(osm_map);
                GeoPoint point = new GeoPoint(pair.getValue().getLatitude(), pair.getValue().getLongitude());
                osmMarker.setPosition(point);
                osmMarker.setAnchor(org.osmdroid.views.overlay.Marker.ANCHOR_CENTER, org.osmdroid.views.overlay.Marker.ANCHOR_BOTTOM);
                osm_markers.put(pair.getValue().getId(), osmMarker);
            }
        } catch (NullPointerException e) {
            startActivity(new Intent(getActivity(), AuthActivity.class));
            Log.i(LOG_TAG, "onMapReady..........NullPointerException");
        }

        for (Map.Entry<Integer, org.osmdroid.views.overlay.Marker> pair : osm_markers.entrySet()) {
            osm_map.getOverlays().add(pair.getValue());
        }

    }
    //***


    //Save camera position
    @Override
    public void onStop() {
        try {
            AppData.osmStartPoint = osm_map.getMapCenter();
            AppData.osmCameraZoom = osm_map.getZoomLevelDouble();

        } catch (NullPointerException e) {
            startActivity(new Intent(getActivity(), AuthActivity.class));
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
