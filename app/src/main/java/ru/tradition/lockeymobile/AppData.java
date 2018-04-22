package ru.tradition.lockeymobile;

import android.net.Uri;
import android.support.v4.view.ViewPager;
import android.view.Menu;

import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.util.GeoPoint;

import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CopyOnWriteArraySet;

import ru.tradition.lockeymobile.subscriptions.SubscriptionData;
import ru.tradition.lockeymobile.tabs.assetstab.AssetsData;
import ru.tradition.lockeymobile.tabs.maptab.GeofencePolygon;

/**
 * Created by Caelestis on 28.01.2018.
 *
 * Stores variables for the entire application
 */

public final class AppData {

    public static MainActivity mainActivity;

    //whether it need new token to download data
    public static volatile boolean needToken = false;

    //Here is the data from Loader about cars and other assets
    public static Map<Integer, AssetsData> mAssetMap;
    //To save camera position in case of restarting app
    public static CameraPosition target = CameraPosition.builder()
            .target(new LatLng(55.7522200, 37.6155600))
            .zoom(10)
            .build();

    //to save osm map camera position
    public static IGeoPoint osmStartPoint = new GeoPoint(55.7522200, 37.6155600);
    public static double osmCameraZoom = 12.0;


    //Here is the data from Loader about geozones
    public static Map<Integer, GeofencePolygon> mPolygonsMap;

    //Here is the data from Loader about subscriptions
    public static Map<Integer, SubscriptionData> mSubscriptionsMap;

    //This sets determines which kit is selected
    public static Set<Integer> selectedAsset = new TreeSet<>();

    public static Set<String> selectedNotification = new TreeSet<>();
    public static Set<String> selectedNotificationLong = new TreeSet<>();
    public static Set<Uri> selectedNotificationUri = new TreeSet<>();

    public static volatile Set<Integer> selectedSubscription = new CopyOnWriteArraySet<>();
    public static volatile Set<Integer> activatingSubscription = new CopyOnWriteArraySet<>();
    public static volatile Set<Integer> deactivatingSubscription = new CopyOnWriteArraySet<>();

    //for selectingMode touches the notification and asset tabs
    public static boolean isAssetSelectingMode = false;
    public static int selectedAssetCounter = 0;
    public static boolean isNotificationSelectingMode = false;
    public static int selectedNotificationCounter = 0;

    //store main menu
    public static Menu mMenu;
    public static CustomViewPager viewPager;

    //Flags for managing the updating thread
    public static boolean isRepeated = false;
    public static boolean isFinished = false;

    //user's login and password
    public static boolean isAuthorized = false;
    public static String pwd = "";
    public static String usr = "";


    /**
     * Constant value for the loader ID.
     */
    public static final int AUTH_LOADER_ID = 1;
    public static final int ASSETS_LOADER_ID = 2;
    public static final int ZONES_LOADER_ID = 3;
    public static final int SUBSCRIPTIONS_LOADER_ID = 4;
    public static final int ACTIVATE_SUBSCRIPTION_LOADER_ID = 5;
    public static final int DEACTIVATE_SUBSCRIPTION_LOADER_ID = 6;


    /**
     * URL for data from server
     */
    public static final String ASSETS_REQUEST_URL = "http://my.lockey.ru/LockeyREST/api/Cars";
    public static final String AUTH_REQUEST_URL = "http://my.lockey.ru/LockeyREST/api/Auth";
    public static final String ZONES_LIST_URL = "http://my.lockey.ru/LockeyREST/api/Zone";
    public static final String SUBSCRIPTIONS_LIST_URL = "http://my.lockey.ru/LockeyREST/api/ZoneSubscription?";
    public static final String ACTIVATE_SUBSCRIPTION_REQUEST_URL = "http://my.lockey.ru/LockeyREST/api/ZoneSubscription";
    public static final String DEACTIVATE_SUBSCRIPTION_REQUEST_URL = "http://my.lockey.ru/LockeyREST/api/ZoneSubscription?";

}
