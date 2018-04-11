package ru.tradition.lockeymobile;

import java.util.List;
import java.util.Map;

import ru.tradition.lockeymobile.tabs.assetstab.AssetsData;
import ru.tradition.lockeymobile.tabs.maptab.GeofencePolygon;

/**
 * Created by Caelestis on 11.04.2018.
 */

public class LoadedData {
    private Map<Integer, AssetsData> assetData;
    private List<GeofencePolygon> polygonsList;

    public LoadedData(Map<Integer, AssetsData> assetData) {
        this.assetData = assetData;
    }

    public LoadedData(List<GeofencePolygon> polygonsList) {
        this.polygonsList = polygonsList;
    }

    public Map<Integer, AssetsData> getAssetData() {
        return assetData;
    }

    public List<GeofencePolygon> getPolygonsList() {
        return polygonsList;
    }
}
