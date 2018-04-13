package ru.tradition.lockeymobile;

import android.net.Uri;
import android.support.v4.view.ViewPager;
import android.view.Menu;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import ru.tradition.lockeymobile.tabs.assetstab.AssetsData;
import ru.tradition.lockeymobile.tabs.maptab.GeofencePolygon;

/**
 * Created by Caelestis on 28.01.2018.
 *
 * Stores variables for the entire application
 */

public final class AppData {

    public static MainActivity mainActivity;

    //Stores the response code for the request
    public static int assetsUrlResponseCode;
    public static int zonesUrlResponseCode;
    public static int subscriptionsUrlResponseCode;

    //Here is the data from Loader about cars and other assets
    public static Map<Integer, AssetsData> mAssetMap;
    //To save camera position in case of restarting app
    public static CameraPosition target = CameraPosition.builder()
            .target(new LatLng(55.7522200, 37.6155600))
            .zoom(10)
            .build();

    //Here is the data from Loader about geozones
    public static Map<Integer, GeofencePolygon> mPolygonsMap;

    //Here is the data from Loader about subscriptions
    public static Map<Integer, SubscriptionData> mSubscriptionsMap;

    //This sets determines which kit is selected
    public static Set<Integer> selectedAsset = new TreeSet<>();
    public static Set<String> selectedNotification = new TreeSet<>();
    public static Set<String> selectedNotificationLong = new TreeSet<>();
    public static Set<Uri> selectedNotificationUri = new TreeSet<>();

    //for selectingMode touches the notification and asset tabs
    public static boolean isAssetSelectingMode = false;
    public static int selectedAssetCounter = 0;
    public static boolean isNotificationSelectingMode = false;
    public static int selectedNotificationCounter = 0;

    //store main menu
    public static Menu mMenu;
    public static ViewPager viewPager;

    public static GoogleMap m_map;

    //Flags for managing the updating thread
    public static boolean isRepeated = false;
    public static boolean isFinished = false;

    //user's login and password
    public static boolean isAuthorized = false;
    public static String pwd = "";
    public static String usr = "";


    /**
     * Constant value for the assets loader ID. We can choose any integer.
     * This comes into play if you're using multiple loaders.
     */
    public static final int AUTH_LOADER_ID = 1;
    public static final int ASSETS_LOADER_ID = 2;
    public static final int ZONES_LOADER_ID = 3;
    public static final int SUBSCRIPTIONS_LOADER_ID = 4;


    /**
     * URL for assets data from server
     */
    public static final String ASSETS_REQUEST_URL = "http://my.lockey.ru/LockeyREST/api/Cars";
    public static final String AUTH_REQUEST_URL = "http://my.lockey.ru/LockeyREST/api/Auth";
    public static final String ZONES_LIST_URL = "http://my.lockey.ru/LockeyREST/api/Zone";
    public static final String SUBSCRIPTIONS_LIST_URL = "http://my.lockey.ru/LockeyREST/api/ZoneSubscription?";

}
