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
import org.osmdroid.tileprovider.tilesource.MapBoxTileSource;
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.OverlayItem;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import ru.tradition.lockeymobile.AppData;
import ru.tradition.lockeymobile.AuthActivity;
import ru.tradition.lockeymobile.MainActivity;
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

    public static MapFragmentTabOSM mftosm;

    public static MapView osm_map;
    public static IMapController mapController;
    public static TreeMap<Integer, org.osmdroid.views.overlay.Marker> osm_markers = new TreeMap<>();

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

        mftosm = this;

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
                if (AppData.mPolygonsMap != null && !AppData.mPolygonsMap.isEmpty())
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                else {
                    Toast.makeText(getContext(), "Получение списка зон", Toast.LENGTH_LONG).show();
                    AppData.mainActivity.getZones();
                }


            }
        });

        fabLayers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (osm_map != null) {
                    osm_map.setTileSource(TileSourceFactory.CLOUDMADESMALLTILES);


                }

            }
        });

        fabLayers.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (osm_markers != null && !osm_markers.isEmpty()) {
                    int minLat = Integer.MAX_VALUE;
                    int maxLat = Integer.MIN_VALUE;
                    int minLong = Integer.MAX_VALUE;
                    int maxLong = Integer.MIN_VALUE;
                    for (Map.Entry<Integer, org.osmdroid.views.overlay.Marker> pair : osm_markers.entrySet()) {
                        GeoPoint point = pair.getValue().getPosition();
                        if (Math.round(point.getLatitude() * 10000000) < minLat)
                            minLat = (int) Math.round(point.getLatitude() * 10000000);
                        if (Math.round(point.getLatitude() * 10000000) > maxLat)
                            maxLat = (int) Math.round(point.getLatitude() * 10000000);
                        if (Math.round(point.getLongitude() * 10000000) < minLong)
                            minLong = (int) Math.round(point.getLongitude() * 10000000);
                        if (Math.round(point.getLongitude() * 10000000) > maxLong)
                            maxLong = (int) Math.round(point.getLongitude() * 10000000);
                    }
                    BoundingBox boundingBox = new BoundingBox((double) maxLat / 10000000,
                            (double) maxLong / 10000000,
                            (double) minLat / 10000000,
                            (double) minLong / 10000000);
                    osm_map.zoomToBoundingBox(boundingBox, true, 10);
                } else {
                    osmGetDataAndShowMarkers();
                    Toast.makeText(getContext(), "Получение координат объектов", Toast.LENGTH_LONG).show();

                }

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
        //mHandler = new Handler();
    }

    @Override
    public void onStart() {
        super.onStart();
        //startMarkerUpdating();
    }

    public void osmGetDataAndShowMarkers() {
        try {
            osm_markers.clear();
            osm_map.getOverlays().clear();
            for (Map.Entry<Integer, AssetsData> pair : AppData.mAssetMap.entrySet()) {
                org.osmdroid.views.overlay.Marker osmMarker = new org.osmdroid.views.overlay.Marker(osm_map);
                GeoPoint point = new GeoPoint(pair.getValue().getLatitude(), pair.getValue().getLongitude());
                osmMarker.setPosition(point);
                osmMarker.setAnchor(org.osmdroid.views.overlay.Marker.ANCHOR_CENTER, org.osmdroid.views.overlay.Marker.ANCHOR_BOTTOM);
                osm_markers.put(pair.getValue().getId(), osmMarker);
            }
        } catch (NullPointerException e) {
            //startActivity(new Intent(getActivity(), AuthActivity.class));
            Log.i(LOG_TAG, "onMapReady..........NullPointerException");
        }

        for (Map.Entry<Integer, org.osmdroid.views.overlay.Marker> pair : osm_markers.entrySet()) {
            osm_map.getOverlays().add(pair.getValue());
        }
        osm_map.invalidate();
    }

    public void updateMarkers() {
//        if (osm_markers == null || osm_markers.isEmpty()) {
//            osmGetDataAndShowMarkers();
//            return;
//        }
//
//        if (osm_map != null && AppData.mAssetMap != null && !AppData.mAssetMap.isEmpty()) {
//            //updating marker position
//            for (Map.Entry<Integer, AssetsData> pair : AppData.mAssetMap.entrySet()) {
//                int id = pair.getKey();
//                org.osmdroid.views.overlay.Marker savedMarker = osm_markers.get(id);
//                GeoPoint savedPosition;
//                //need this check in case killing process
//                if (savedMarker != null) {
//                    savedPosition = savedMarker.getPosition();
//                    //compare saved and new position
//                    GeoPoint newPosition = new GeoPoint(pair.getValue().getLatitude(), pair.getValue().getLongitude());
//                    if (!savedPosition.equals(newPosition)) {
//                        org.osmdroid.views.overlay.Marker newMarker = new org.osmdroid.views.overlay.Marker(osm_map);
//                        newMarker.setPosition(newPosition);
//                        newMarker.setAnchor(org.osmdroid.views.overlay.Marker.ANCHOR_CENTER, org.osmdroid.views.overlay.Marker.ANCHOR_BOTTOM);
//                        osm_map.getOverlays().remove(savedMarker);
//                        osm_markers.remove(id);
//                        osm_map.getOverlays().add(newMarker);
//                        osm_markers.put(id, newMarker);
//                        //osm_map.getOverlays().remove()
//                        Log.i(LOG_TAG, "The markers have moved");
//                    } else {
//
//                    }
//                }
//            }
//            Log.i(LOG_TAG, "The position of markers was updated");
//
//        } else
//            Log.i(LOG_TAG, "the map................. is null");
        osmGetDataAndShowMarkers();
    }


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
        //stopMarkerUpdating();
        super.onStop();

    }

//    @Override
//    public void onDestroyView() {
//        MainActivity.isFinished = false;
//        MainActivity.isRepeated = false;
//        super.onDestroyView();
//    }

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
