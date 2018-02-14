package ru.tradition.lockeymobile;

import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import ru.tradition.lockeymobile.tabs.assetstab.AssetsData;

/**
 * Created by Caelestis on 28.01.2018.
 */

public final class UserData {

    //for getting token without starting MainActivity
    public static boolean hasToken = false;

    //Here is the data from Loader about cars and other assets
    public static Map<Integer, AssetsData> mAssetData;
    //To save camera position in case of restarting app
    public static CameraPosition target = CameraPosition.builder()
            .target(new LatLng(55.7522200, 37.6155600))
            .zoom(10)
            .build();

    //This set determines which kit is selected
    public static Set<Integer> selectedAsset = new TreeSet<>();

    //todo Here is the data from Loader about cars and other assets for API lesser 26
    public static List<AssetsData> mAssetDataAPIBefore26;

    //user's login and password
    public static String pwd = "";
    public static String usr = "";


    /**
     * Constant value for the assets loader ID. We can choose any integer.
     * This comes into play if you're using multiple loaders.
     */
    public static final int ASSETS_LOADER_ID = 1;

    /**
     * URL for assets data from server
     */
    public static final String ASSETS_REQUEST_URL = "http://my.lockey.ru/LockeyREST/api/Cars";

    public static final String AUTH_REQUEST_URL = "http://my.lockey.ru/LockeyREST/api/Auth";

    public static final int AUTH_LOADER_ID = 2;


}
