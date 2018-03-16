package ru.tradition.lockeymobile.tabs.maptab;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Caelestis on 16.03.2018.
 */

public class GeofencePolygon {
    String polygonName;
    LatLng[] polygon;

    public GeofencePolygon(String polygonName, LatLng... polygon) {
        this.polygonName = polygonName;
        this.polygon = polygon;
    }

    public String getPolygonName() {
        return polygonName;
    }

    public LatLng[] getPolygon() {
        return polygon;
    }
}
