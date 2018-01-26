package ru.tradition.lockeymobile;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.gms.maps.MapFragment;


import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MapFragmentActivity.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MapFragmentActivity#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MapFragmentActivity extends Fragment implements OnMapReadyCallback {


    GoogleMap m_map;
    boolean mapReady = false;

    MarkerOptions renton;

    LatLng rentonP = new LatLng(47.489805, -122.120502);
    LatLng kirkland=new LatLng(47.7301986, -122.1768858);
    LatLng everett=new LatLng(47.978748,-122.202001);
    LatLng lynnwood=new LatLng(47.819533,-122.32288);
    LatLng montlake=new LatLng(47.7973733,-122.3281771);
    LatLng kent=new LatLng(47.385938,-122.258212);
    LatLng showare=new LatLng(47.38702,-122.23986);

    static final CameraPosition NEWYORK = CameraPosition.builder()
            .target(new LatLng(40.784, -73.9857))
            .zoom(21)
            .bearing(0)
            .tilt(45)
            .build();

    static final CameraPosition SEATTLE = CameraPosition.builder()
            .target(new LatLng(47.6204, -122.3491))
            .zoom(17)
            .bearing(0)
            .tilt(45)
            .build();

    static final CameraPosition DUBLIN = CameraPosition.builder()
            .target(new LatLng(53.3478, -6.2597))
            .zoom(17)
            .bearing(90)
            .tilt(45)
            .build();


    static final CameraPosition TOKYO = CameraPosition.builder()
            .target(new LatLng(35.6895, 139.6917))
            .zoom(17)
            .bearing(90)
            .tilt(45)
            .build();






    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public MapFragmentActivity() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MapFragmentActivity.
     */
    // TODO: Rename and change types and number of parameters
    public static MapFragmentActivity newInstance(String param1, String param2) {
        MapFragmentActivity fragment = new MapFragmentActivity();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_map, container, false);


        renton = new MarkerOptions()
                .position(new LatLng(47.489805, -122.120502))
                .title("Renton");
        //.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_launcher)); //to add icon here

        Button btnMap = (Button) rootView.findViewById(R.id.btnMap);
        btnMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mapReady)
                    m_map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            }
        });

        Button btnSatellite = (Button) rootView.findViewById(R.id.btnSatellite);
        btnSatellite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mapReady)
                    m_map.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
            }
        });

        Button btnHybrid = (Button) rootView.findViewById(R.id.btnHybrid);
        btnHybrid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mapReady)
                    m_map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
            }
        });

        //City listeners
        Button btnSeattle = (Button) rootView.findViewById(R.id.btnSea);
        btnSeattle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mapReady)
                    flyTo(SEATTLE);
            }
        });

        Button btnDublin = (Button) rootView.findViewById(R.id.btnDublin);
        btnDublin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mapReady)
                    flyTo(DUBLIN);
            }
        });

        Button btnTokyo = (Button) rootView.findViewById(R.id.btnTokyo);
        btnTokyo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mapReady)
                    flyTo(TOKYO);
            }
        });

        //Find map fragment
        MapFragment mapFragment = (MapFragment) getActivity().getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);





        // Inflate the layout for this fragment
        return rootView;
    }




    @Override
    public void onMapReady(GoogleMap map) {
        mapReady = true;
        m_map = map;
        LatLng newYork = new LatLng(40.7484, -73.9857);
        CameraPosition target = CameraPosition.builder()
                .target(newYork)
                .bearing(45)
                .tilt(45)
                .zoom(17)
                .build();
        m_map.moveCamera(CameraUpdateFactory.newCameraPosition(target));
        m_map.addMarker(renton);
        PolylineOptions pl = new PolylineOptions().geodesic(true)
                .add(rentonP)
                .add(kirkland)
                .add(everett)
                .add(lynnwood)
                .add(montlake)
                .add(kent)
                .add(showare);
        m_map.addPolyline(pl);

        //Полигон - это замкнутая ломаная

//        m_map.addPolygon(new PolygonOptions().geodesic(true)
//                .add(rentonP)
//                .add(kirkland)
//                .add(everett)
//                .add(lynnwood)
//                .add(montlake)
//                .add(kent)
//                .add(showare)
//        );

        map.addCircle(new CircleOptions()
                .center(rentonP)
                .radius(5000)
                .strokeColor(Color.GREEN)
                .fillColor(Color.argb(64,0,255,0)));

    }

    private void flyTo(CameraPosition target) {
        m_map.animateCamera(CameraUpdateFactory.newCameraPosition(target), 10000, null);
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
