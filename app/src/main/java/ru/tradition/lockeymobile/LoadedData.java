package ru.tradition.lockeymobile;

import java.util.List;
import java.util.Map;

import ru.tradition.lockeymobile.tabs.assetstab.AssetsData;
import ru.tradition.lockeymobile.tabs.maptab.GeofencePolygon;

import static ru.tradition.lockeymobile.AppData.ASSETS_LOADER_ID;
import static ru.tradition.lockeymobile.AppData.SUBSCRIPTIONS_LOADER_ID;
import static ru.tradition.lockeymobile.AppData.ZONES_LOADER_ID;

/**
 * Created by Caelestis on 11.04.2018.
 */

public class LoadedData {
    private Map<Integer, AssetsData> assetMap;
    private Map<Integer, GeofencePolygon> polygonsMap;
    private Map<Integer, SubscriptionData> subscriptionMap;
    private String responseMessage;


    public LoadedData(Map<Integer, AssetsData> assetMap, Map<Integer, GeofencePolygon> polygonsMap, Map<Integer, SubscriptionData> subscriptionMap) {
        this.assetMap = assetMap;
        this.polygonsMap = polygonsMap;
        this.subscriptionMap = subscriptionMap;
    }

    public LoadedData(String responseMessage) {
        this.responseMessage = responseMessage;
    }

    public Map<Integer, AssetsData> getAssetMap() {
        return assetMap;
    }

    public Map<Integer, GeofencePolygon> getPolygonsMap() {
        return polygonsMap;
    }

    public Map<Integer, SubscriptionData> getSubscriptionMap() {
        return subscriptionMap;
    }

    public String getResponseMessage() {
        return responseMessage;
    }
}
