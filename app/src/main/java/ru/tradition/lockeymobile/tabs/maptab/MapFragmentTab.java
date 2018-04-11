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
                    clearPolygonSet();
                    mAdapter.notifyDataSetChanged();
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
        fakeList.add(new GeofencePolygon(21, "МКАД",true,
                new LatLng(55.57562233895321, 37.59931400583514),
                new LatLng(55.5965780907678, 37.51367056907543),
                new LatLng(55.63613065941571, 37.460112219466055),
                new LatLng(55.69190167482748, 37.413420324934805),
                new LatLng(55.71124802627249, 37.38732779563793),
                new LatLng(55.75145776796834, 37.36810172141918),
                new LatLng(55.786993409326605, 37.372221594466055),
                new LatLng(55.81092307423466, 37.39007437766918),
                new LatLng(55.841007252919766, 37.396940832747305),
                new LatLng(55.85950903121015, 37.399687414778555),
                new LatLng(55.881083308630785, 37.45049918235668),
                new LatLng(55.88570480889632, 37.48895133079418),
                new LatLng(55.90572495239638, 37.528776770247305),
                new LatLng(55.90880405762271, 37.583708410872305),
                new LatLng(55.89725615242213, 37.64275992454418),
                new LatLng(55.89186595335763, 37.70593131126293),
                new LatLng(55.82481097085516, 37.83776724876293),
                new LatLng(55.71666328863865, 37.841887121809805),
                new LatLng(55.69654567259541, 37.833647375716055),
                new LatLng(55.65318023530563, 37.84326041282543),
                new LatLng(55.572516831562154, 37.69219840110668),
                new LatLng(55.56941107857544, 37.671599035872305)
        ));
        fakeList.add(new GeofencePolygon(25, "Нижний Новгород",true,
                new LatLng(56.30411772619058, 43.69103165174192),
                new LatLng(56.380229864152, 43.82923465052909),
                new LatLng(56.41139196413391, 44.01600222865409),
                new LatLng(56.3513251385113, 44.23572879115409),
                new LatLng(56.158321176743996, 44.23435550013846),
                new LatLng(56.06413731314753, 44.11762576381034),
                new LatLng(56.134605503455326, 43.74546389857596)

                ));
        fakeList.add(new GeofencePolygon(28, "Офис",true,
                new LatLng(55.588120214087816, 37.65351380514278),
                new LatLng(55.588768979013835, 37.65327777074947),
                new LatLng(55.588835673865624, 37.653889314404864),
                new LatLng(55.58818691004207, 37.654114619962115)
        ));
        fakeList.add(new GeofencePolygon(31, "Центр",true,
                new LatLng(55.760591030877904, 31.340907338621832),
                new LatLng(56.90548719520857, 30.813563588621832),
                new LatLng(58.17889441594721, 33.67000890112183),
                new LatLng(58.86725629527061, 36.96590733862183),
                new LatLng(58.54770155778007, 41.00887608862183),
                new LatLng(56.22755986468746, 42.76668858862183),
                new LatLng(53.88759714051711, 42.37118077612183),
                new LatLng(53.02412807774639, 37.71297765112183),
                new LatLng(54.19724425694539, 33.58211827612183)
        ));
        fakeList.add(new GeofencePolygon(35, "Стройка 12",true,
                new LatLng(55.61587297858232, 37.46164675179648),
                new LatLng(55.62086526490709, 37.46387834969687),
                new LatLng(55.62081679916385, 37.47383470956015),
                new LatLng(55.61514589356092, 37.475980476772065)
        ));
        fakeList.add(new GeofencePolygon(38, "Московская область",true,
                new LatLng(55.27574067614793, 35.36139002637526),
                new LatLng(56.39846583288372, 35.62506190137526),
                new LatLng(56.92975473224751, 37.64654627637526),
                new LatLng(56.71331433143086, 38.30572596387526),
                new LatLng(55.908952332380316, 38.70123377637526),
                new LatLng(55.81030003082483, 39.93170252637526),
                new LatLng(55.26322247267056, 40.28326502637526),
                new LatLng(54.30024741518173, 38.78912440137526),
                new LatLng(54.835251662420234, 37.95416346387526),
                new LatLng(54.75925202850601, 37.38287440137526),
                new LatLng(55.26322247267056, 36.67974940137526),
                new LatLng(55.16293474516506, 35.36139002637526)
        ));
        fakeList.add(new GeofencePolygon(41, "Национальный парк", true,
                new LatLng(63.043406922092856, 36.23426996166063),
                new LatLng(63.29629615315988, 36.24525628978563),
                new LatLng(63.54698574915778, 36.51991449291063),
                new LatLng(63.537196046466725, 36.80555902416063),
                new LatLng(63.22709522026023, 36.99232660228563),
                new LatLng(63.02846227771947, 37.49769769603563),
                new LatLng(62.67252899731627, 37.31093011791063),
                new LatLng(62.26117310623878, 37.17909418041063),
                new LatLng(62.11765459299115, 36.75062738353563)
        ));

        mAdapter = new GeofencePolygonAdapter(fakeList, this);
        mPolygonsList.setAdapter(mAdapter);

        //to remove zones after rotation
        clearPolygonSet();
        return rootView;
    }

    private ArrayList<GeofencePolygon> fakeList;

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
                GeofencePolygon geof = fakeList.get(clickedItemIndex);
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
