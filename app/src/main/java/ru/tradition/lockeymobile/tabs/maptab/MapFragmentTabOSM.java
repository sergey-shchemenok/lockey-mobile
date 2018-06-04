package ru.tradition.lockeymobile.tabs.maptab;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polygon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import ru.tradition.lockeymobile.AppData;
import ru.tradition.lockeymobile.AuthActivity;
import ru.tradition.lockeymobile.R;
import ru.tradition.lockeymobile.tabs.assetstab.AssetsData;

import static ru.tradition.lockeymobile.AppData.mPolygonsMap;


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
    private LinearLayout zooms;
    private int recyclerViewScrollState = 0;


    private static TreeMap<Integer, Polygon> polygons = new TreeMap<>();
    public static int polygonNamesNumber = -1;

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1;
    private String mParam2;
    private OnFragmentInteractionListener mListener;

    private Button zoomIn;
    private Button zoomOut;

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
        osm_map.setBuiltInZoomControls(false);
        osm_map.setMultiTouchControls(true);

//        RotationGestureOverlay mRotationGestureOverlay = new RotationGestureOverlay(osm_map);
//        mRotationGestureOverlay.setEnabled(true);
//        osm_map.getOverlays().add(mRotationGestureOverlay);

        osm_map.setMinZoomLevel(3.5);

        //We can move the map on a default view point. For this, we need access to the map controller:
        mapController = osm_map.getController();
        mapController.setZoom(AppData.osmCameraZoom);
        mapController.setCenter(AppData.osmStartPoint);

        zoomIn = (Button)rootView.findViewById(R.id.map_tab_zoom_in);
        zoomOut = (Button)rootView.findViewById(R.id.map_tab_zoom_out);

        zoomIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mapController != null)
                    mapController.zoomIn();
            }
        });
        zoomOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mapController != null)
                    mapController.zoomOut();
            }
        });


//        Log.i(LOG_TAG, "osm start coordinates  " + osm_map.getMapCenter().getLatitude() + " "
//                + osm_map.getMapCenter().getLongitude());

        //******


        // Inflate the layout for this fragment
        fabLayers = (FloatingActionButton) rootView.findViewById(R.id.fab_layers);
        fabBottomDrawer = (FloatingActionButton) rootView.findViewById(R.id.fab_bottom_drawer);
        geoFenceBottomSheet = (LinearLayout) rootView.findViewById(R.id.bottom_sheet);
        zooms = rootView.findViewById(R.id.map_tab_zooms);

        bottomSheetBehavior = BottomSheetBehavior.from(geoFenceBottomSheet);
        bottomSheetBehavior.setHideable(true);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (BottomSheetBehavior.STATE_HIDDEN == newState) {
                    fabLayers.show();
                    fabBottomDrawer.show();
                    zooms.setVisibility(View.VISIBLE);
                    clearPolygonSet();
                    mAdapter.notifyDataSetChanged();
//                    osm_map.setBuiltInZoomControls(true);
                }

                if (BottomSheetBehavior.STATE_EXPANDED == newState) {
                    recyclerViewScrollState = layoutManager.findFirstCompletelyVisibleItemPosition();
                    layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                    ArrayList<GeofencePolygon> geoPoly = new ArrayList<>(mPolygonsMap.values());
                    Collections.sort(geoPoly);
                    mAdapter = new GeofencePolygonAdapter(geoPoly, MapFragmentTabOSM.this, MapFragmentTabOSM.this);                    mPolygonsList.setAdapter(mAdapter);
                    mPolygonsList.scrollToPosition(recyclerViewScrollState);
                    fabLayers.hide();
                    fabBottomDrawer.hide();
                    zooms.setVisibility(View.GONE);
//                    osm_map.setBuiltInZoomControls(false);
                }
                if (BottomSheetBehavior.STATE_COLLAPSED == newState) {
                    recyclerViewScrollState = layoutManager.findFirstCompletelyVisibleItemPosition();
                    layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
                    ArrayList<GeofencePolygon> geoPoly = new ArrayList<>(mPolygonsMap.values());
                    Collections.sort(geoPoly);
                    mAdapter = new GeofencePolygonAdapter(geoPoly, MapFragmentTabOSM.this, MapFragmentTabOSM.this);                    mPolygonsList.setAdapter(mAdapter);
                    mPolygonsList.scrollToPosition(recyclerViewScrollState);
                    fabLayers.show();
                    fabBottomDrawer.hide();
                    zooms.setVisibility(View.VISIBLE);
//                    osm_map.setBuiltInZoomControls(false);
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                bottomSheetBehavior.getPeekHeight();
                if (slideOffset <= 0) {
                    int padding_in_dp = 24;  // 6 dps
                    final float scale = getResources().getDisplayMetrics().density;
                    int padding_in_px = (int) (padding_in_dp * scale * (2 + slideOffset) + 0.5f);
                    zooms.setPadding(0,0,0,padding_in_px);
                } else {
                    fabLayers.hide();
                    fabBottomDrawer.hide();
                    zooms.setVisibility(View.GONE);
                }
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
                    //CloudmadeUtil.retrieveCloudmadeKey(getContext());
                    if (osm_map.getTileProvider().getTileSource() == TileSourceFactory.MAPNIK)
                        osm_map.setTileSource(TileSourceFactory.OpenTopo);
                    else
                        osm_map.setTileSource(TileSourceFactory.MAPNIK);
                }
//                Toast.makeText(getContext(), "Функция переключения режимов отображения карт OpenStreetMap недоступна в текущей версии приложения", Toast.LENGTH_LONG).show();
            }
        });

        fabLayers.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (osm_markers != null && !osm_markers.isEmpty()) {
                    if (osm_markers.size() > 1) {
                        int minLat = Integer.MAX_VALUE;
                        int maxLat = Integer.MIN_VALUE;
                        int minLong = Integer.MAX_VALUE;
                        int maxLong = Integer.MIN_VALUE;
                        for (Map.Entry<Integer, Marker> pair : osm_markers.entrySet()) {
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
                    }
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
            ArrayList<GeofencePolygon> geoPoly = new ArrayList<>(mPolygonsMap.values());
            Collections.sort(geoPoly);
            mAdapter = new GeofencePolygonAdapter(geoPoly, MapFragmentTabOSM.this, MapFragmentTabOSM.this);
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
                osm_map.getOverlayManager().remove(pair.getValue());
            polygons.clear();
        }
        polygonNamesNumber = -1;
        osm_map.invalidate();
    }

    @Override
    public void onListItemClick(int clickedItemIndex) {
        if (osm_map != null) {
            if (!polygons.isEmpty()) {
                for (Map.Entry<Integer, Polygon> pair : polygons.entrySet())
                    osm_map.getOverlayManager().remove(pair.getValue());
                polygons.clear();
            }
            if (polygonNamesNumber != clickedItemIndex) {
                ArrayList<GeofencePolygon> polygonList = new ArrayList<>(AppData.mPolygonsMap.values());
                Collections.sort(polygonList);

                GeofencePolygon geof = polygonList.get(clickedItemIndex);
                LatLng[] latLngArray = geof.getPolygon();
                List<GeoPoint> geoPoints = new ArrayList<>();
                for (int i = 0; i < latLngArray.length; i++) {
                    geoPoints.add(new GeoPoint(latLngArray[i].latitude, latLngArray[i].longitude));
                }

                Polygon polygon = new Polygon();    //see note below
                polygon.setFillColor(Color.parseColor("#6421a30d"));
                polygon.setStrokeColor(Color.parseColor("#FF21A30D"));
                polygon.setPoints(geoPoints);
                osm_map.getOverlayManager().add(polygon);
                polygons.put(geof.getGeofence_id(), polygon);
                osm_map.invalidate();

                int lastPosition = polygonNamesNumber;
                polygonNamesNumber = clickedItemIndex;
                if (lastPosition >= 0) {
                    mAdapter.notifyDataSetChanged();
                }

            } else {
                polygonNamesNumber = -1;
                osm_map.invalidate();
            }
        }

    }

    @Override
    public void onListItemLongClick(int clickedItemIndex) {
        if (osm_map != null) {
            if (polygonNamesNumber != clickedItemIndex) {
                this.onListItemClick(clickedItemIndex);

            }
        }

        ArrayList<GeofencePolygon> polygonList = new ArrayList<>(AppData.mPolygonsMap.values());
        Collections.sort(polygonList);

        GeofencePolygon geof = polygonList.get(clickedItemIndex);
        LatLng[] latLngArray = geof.getPolygon();
        List<GeoPoint> geoPoints = new ArrayList<>();
        for (int i = 0; i < latLngArray.length; i++) {
            geoPoints.add(new GeoPoint(latLngArray[i].latitude, latLngArray[i].longitude));
        }
        Log.i(LOG_TAG, "long click.....");

        int minLat = Integer.MAX_VALUE;
        int maxLat = Integer.MIN_VALUE;
        int minLong = Integer.MAX_VALUE;
        int maxLong = Integer.MIN_VALUE;
        for (GeoPoint point : geoPoints) {
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
                osmMarker.setTitle(String.valueOf(pair.getValue().getId()));
                osm_markers.put(pair.getValue().getId(), osmMarker);
            }
        } catch (NullPointerException e) {
            //startActivity(new Intent(getActivity(), AuthActivity.class));
            Log.i(LOG_TAG, "onMapReady..........NullPointerException");
        }

        for (Map.Entry<Integer, org.osmdroid.views.overlay.Marker> pair : osm_markers.entrySet()) {
            osm_map.getOverlays().add(pair.getValue());
        }

        if (!polygons.isEmpty()) {
            for (Map.Entry<Integer, Polygon> pair : polygons.entrySet())
                osm_map.getOverlayManager().add(pair.getValue());
        }

        osm_map.invalidate();
    }

    public void updateMarkers() {
        osmGetDataAndShowMarkers();
    }


    //Save camera position
    @Override
    public void onStop() {
        if (osm_map != null) {
            AppData.osmStartPoint = osm_map.getMapCenter();
            AppData.osmCameraZoom = osm_map.getZoomLevelDouble();
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
