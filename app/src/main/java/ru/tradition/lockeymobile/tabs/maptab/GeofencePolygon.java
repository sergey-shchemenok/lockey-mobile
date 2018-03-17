package ru.tradition.lockeymobile.tabs.maptab;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Caelestis on 16.03.2018.
 */

public class GeofencePolygon {
    private int geofence_id;
    private String polygonName;
    private LatLng[] polygon;

    public GeofencePolygon(int geofence_id, String polygonName, LatLng... polygon) {
        this.geofence_id = geofence_id;
        this.polygonName = polygonName;
        this.polygon = polygon;
    }

    public int getGeofence_id() {
        return geofence_id;
    }

    public String getPolygonName() {
        return polygonName;
    }

    public LatLng[] getPolygon() {
        return polygon;
    }
}
