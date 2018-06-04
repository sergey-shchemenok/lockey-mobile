package ru.tradition.lockeymobile.tabs.maptab;

import android.support.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;

import java.util.Comparator;

import ru.tradition.lockeymobile.tabs.assetstab.AssetsData;

/**
 * Created by Caelestis on 16.03.2018.
 */

public class GeofencePolygon implements Comparable<GeofencePolygon> {
    private int geofence_id;
    private String polygonName;
    private boolean isPrivate;
    private LatLng[] polygon;

    public GeofencePolygon(int geofence_id, String polygonName, boolean isPrivate, LatLng... polygon) {
        this.geofence_id = geofence_id;
        this.polygonName = polygonName;
        this.isPrivate = isPrivate;
        this.polygon = polygon;
    }

    public int getGeofence_id() {
        return geofence_id;
    }

    public String getPolygonName() {
        return polygonName;
    }

    public boolean isPrivate() {
        return isPrivate;
    }

    public LatLng[] getPolygon() {
        return polygon;
    }



    @Override
    public int compareTo(@NonNull GeofencePolygon o) {
        return polygonName.compareTo(o.getPolygonName());
    }
}
