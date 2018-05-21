package ru.tradition.lockeymobile;

import android.app.LoaderManager;
import android.app.NativeActivity;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.api.IMapController;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polygon;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;

import ru.tradition.lockeymobile.tabs.maptab.GeofencePolygon;
import ru.tradition.lockeymobile.tabs.maptab.GeofencePolygonAdapter;
import ru.tradition.lockeymobile.tabs.maptab.MapFragmentTabOSM;
import ru.tradition.lockeymobile.tabs.notifications.NotificationsData;
import ru.tradition.lockeymobile.tabs.notifications.database.NotificationContract;

import static android.view.MotionEvent.ACTION_DOWN;
import static ru.tradition.lockeymobile.AppData.ZONES_LOADER_ID;

public class NotificationActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private final String LOG_TAG = NotificationActivity.class.getSimpleName();
    private static final int CURSOR_LOADER_ID = 1;
    /**
     * Content URI for the existing notification
     */
    private Uri mCurrentNotificationUri;

    private TextView notificationTitle;
    private TextView notificationBody;
    private TextView notificationSendingTime;

    private static int zid = -1;

    //for map
    private static MapView osm_map;
    private static IMapController mapController;
    private static TreeMap<Integer, Marker> osm_markers = new TreeMap<>();

    private FloatingActionButton fabLayers;

    //if we open this activity not from the notificatiton tab on item clicking
    private boolean fromItem = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //to get data from background notifications' intent
//        ndBackground = getNotificationData();
//        if (ndBackground != null) {
//            insertNotification(ndBackground);
//            fromItem = false;
//        }

        setContentView(R.layout.activity_notification);

        //go to auth activity. It need to prevent seeing the internal information without authorization
        if (AppData.usr.equals("") || AppData.pwd.equals("") || AppData.isAuthorized == false) {
            Intent intent = new Intent(this, AuthActivity.class);
            intent.putExtra("hasCredentials", false);
            startActivity(intent);
            Log.i(LOG_TAG, ".............no credentials");
            return;
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //to add up button
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);

        }

        toolbar.setTitle(R.string.notification_activity_title);

        osm_map = (MapView) findViewById(R.id.notification_osm_map);
        osm_map.setTileSource(TileSourceFactory.MAPNIK);

        //Then we add default zoom buttons, and ability to zoom with 2 fingers (multi-touch)
        osm_map.setBuiltInZoomControls(true);
        osm_map.setMultiTouchControls(true);

        //get data from the incoming intent. From clicking on item at notification tab
        Intent intent = getIntent();
        mCurrentNotificationUri = intent.getData();

        //this occurs when we get here after clicking the notification banner that coming in foreground
        if (mCurrentNotificationUri == null) {
            Bundle bundle = getIntent().getExtras();
            //ndForeground = (NotificationsData) getIntent().getSerializableExtra("NotificationData");
            Uri uri = bundle.getParcelable("Uri");
            mCurrentNotificationUri = uri;
            makeReadNotification(uri);
            getLoaderManager().initLoader(CURSOR_LOADER_ID, null, this);
            fromItem = false;
        } else {
            getLoaderManager().initLoader(CURSOR_LOADER_ID, null, this);
            fromItem = true;
        }

        notificationTitle = (TextView) findViewById(R.id.notification_activity_title);
        notificationBody = (TextView) findViewById(R.id.notification_activity_body);
        notificationSendingTime = (TextView) findViewById(R.id.notification_activity_sending_time);
        fabLayers = (FloatingActionButton) findViewById(R.id.fab_layers_notification);

        fabLayers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (osm_map != null) {
                    if (osm_map.getTileProvider().getTileSource() == TileSourceFactory.MAPNIK)
                        osm_map.setTileSource(TileSourceFactory.OpenTopo);
                    else
                        osm_map.setTileSource(TileSourceFactory.MAPNIK);
                }
            }
        });

    }

    //Loader methods
    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        //Write the code for the loader callback method onCreateLoader; make sure it is using a URI for one notification.
        String[] projection = {
                //id column is always needed for the cursor
                NotificationContract.NotificationEntry._ID,
                NotificationContract.NotificationEntry.COLUMN_NOTIFICATION_ID,
                NotificationContract.NotificationEntry.COLUMN_NOTIFICATION_TITLE,
                NotificationContract.NotificationEntry.COLUMN_NOTIFICATION_BODY,
                NotificationContract.NotificationEntry.COLUMN_NOTIFICATION_SENDING_TIME,
                NotificationContract.NotificationEntry.COLUMN_NOTIFICATION_LATITUDE,
                NotificationContract.NotificationEntry.COLUMN_NOTIFICATION_LONGITUDE,
                NotificationContract.NotificationEntry.COLUMN_NOTIFICATION_TEXT
        };

        return new CursorLoader(this, mCurrentNotificationUri, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }
        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {
            // Find the columns of notification attributes that we're interested in
            int IDColumnIndex = cursor.getColumnIndex(NotificationContract.NotificationEntry.COLUMN_NOTIFICATION_ID);
            int titleColumnIndex = cursor.getColumnIndex(NotificationContract.NotificationEntry.COLUMN_NOTIFICATION_TITLE);
            int bodyColumnIndex = cursor.getColumnIndex(NotificationContract.NotificationEntry.COLUMN_NOTIFICATION_BODY);
            int sendingTimeColumnIndex = cursor.getColumnIndex(NotificationContract.NotificationEntry.COLUMN_NOTIFICATION_SENDING_TIME);
            int latitudeIndex = cursor.getColumnIndex(NotificationContract.NotificationEntry.COLUMN_NOTIFICATION_LATITUDE);
            int longitudeIndex = cursor.getColumnIndex(NotificationContract.NotificationEntry.COLUMN_NOTIFICATION_LONGITUDE);
            int textIndex = cursor.getColumnIndex(NotificationContract.NotificationEntry.COLUMN_NOTIFICATION_TEXT);

            // Extract out the value from the Cursor for the given column index
            int ID = cursor.getInt(IDColumnIndex);

            String title = cursor.getString(titleColumnIndex);
            String body = cursor.getString(bodyColumnIndex);
            String sendingTime = cursor.getString(sendingTimeColumnIndex);
            double latitude = cursor.getDouble(latitudeIndex);
            double longitude = cursor.getDouble(longitudeIndex);
            String text = cursor.getString(textIndex);

            if ((title == null || title.isEmpty())
                    && (body == null || body.isEmpty())
                    && (sendingTime == null || sendingTime.isEmpty())) {

            }

            // Update the views on the screen with the values from the database
            notificationTitle.setText(title);
            notificationBody.setText(body);
//                    + " Номер бортового комплекта - " + String.valueOf(assetID));
            notificationSendingTime.setText(getFormattedDate(sendingTime));

            //We can move the map on a default view point. For this, we need access to the map controller:
            mapController = osm_map.getController();
            mapController.setZoom(14.0);

            GeoPoint notificationPoint = new GeoPoint(latitude, longitude);
            mapController.setCenter(notificationPoint);

            Marker notificationMarker = new Marker(osm_map);
            notificationMarker.setPosition(notificationPoint);
            notificationMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            notificationMarker.setTitle(title);

            Polygon circle = new Polygon() {
                @Override
                public void showInfoWindow(GeoPoint position) {
                    //super.showInfoWindow(position);
                }
            };
            circle.setPoints(Polygon.pointsAsCircle(notificationPoint, 300.0));
            circle.setFillColor(Color.parseColor("#6421a30d"));
            circle.setStrokeColor(Color.parseColor("#FF21A30D"));
            circle.setStrokeWidth(0);

            osm_map.getOverlays().add(circle);
            osm_map.getOverlays().add(notificationMarker);

//            JSONArray rootArray = new JSONArray(jsonResponse);
//            for (int i = 0; i < rootArray.length(); i++) {

            if (text != null && !text.isEmpty()) {
                try {
                    JSONObject textObject = new JSONObject(text);
                    String event = textObject.getString("event");
                    if (event.equals("zone")) {
                        zid = textObject.getInt("ZID");
                        showZoneOnMap(zid);
                    }

                } catch (JSONException e) {
                    Log.e(LOG_TAG, "Problem parsing the JSON results", e);
                }
            }
            osm_map.invalidate();
        }
    }

    private void showZoneOnMap(int zid) {

        if (osm_map != null && AppData.mPolygonsMap != null && !AppData.mPolygonsMap.isEmpty()) {
            if (AppData.mPolygonsMap.containsKey(zid)) {
                GeofencePolygon geof = AppData.mPolygonsMap.get(zid);
                LatLng[] latLngArray = geof.getPolygon();
                List<GeoPoint> geoPoints = new ArrayList<>();
                for (int i = 0; i < latLngArray.length; i++) {
                    geoPoints.add(new GeoPoint(latLngArray[i].latitude, latLngArray[i].longitude));
                }

                Polygon polygon = new Polygon();    //see note below
                polygon.setFillColor(Color.parseColor("#6421a30d"));
                polygon.setStrokeColor(Color.parseColor("#FF21A30D"));
                polygon.setPoints(geoPoints);
                osm_map.getOverlayManager().add(polygon);
            } else {
                //todo if zone does'not exist
                Toast.makeText(this, "Указанная в уведомлении зона была либо удалена, либо принадлежит другой учетной записи", Toast.LENGTH_LONG).show();
                Log.i(LOG_TAG, "zone does'not exist");
            }
        } else if (AppData.mPolygonsMap == null || AppData.mPolygonsMap.isEmpty()) {
            startUpdater(zid);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.            notificationTitle.setText(title);
        notificationTitle.setText("");
        notificationBody.setText("");
        notificationSendingTime.setText("");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_notification, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            case R.id.notification_menu_logout:
                logout();
                return true;
            case R.id.notification_menu_settings:
                Intent settingsIntent = new Intent(this, SettingsActivity.class);
                startActivity(settingsIntent);
                return true;
            case R.id.notification_menu_subscriptions:
                Intent subscriptionsIntent = new Intent(this, SubscriptionsActivity.class);
                startActivity(subscriptionsIntent);
                return true;

            case R.id.notification_menu_about_program:
                MainActivity.showAboutTheProgram(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void logout() {
        Intent intent = new Intent(this, AuthActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onSupportNavigateUp() {
        //todo navigation can be improved
        if (fromItem)
            super.onBackPressed();
        else {
            fromItem = true;
            Intent intent = new Intent(NotificationActivity.this, MainActivity.class);
            intent.putExtra("page", 2);
            startActivity(intent);
            //return super.onSupportNavigateUp();
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        if (fromItem)
            super.onBackPressed();
        else {
            fromItem = true;
            Intent intent = new Intent(NotificationActivity.this, MainActivity.class);
            intent.putExtra("page", 2);
            startActivity(intent);
            //super.onSupportNavigateUp();
        }
    }


    private static String getFormattedDate(String sendingTime) {
        long milli = 0;
        java.text.SimpleDateFormat simpleDateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        try {
            Date date = simpleDateFormat.parse(sendingTime);
            milli = date.getTime();

        } catch (java.text.ParseException e) {
            e.printStackTrace();
        }
        if (milli == 0) {
            return sendingTime;
        }
        Date date = new Date(milli);
        //SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z"); // the format of your date
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy, HH:mm");
        //sdf.setTimeZone(TimeZone.getTimeZone("GMT-4")); // give a timezone reference for formatting (see comment at the bottom
        return sdf.format(date);
    }

    private void makeReadNotification(Uri currentNotificationUri) {
        ContentValues values = new ContentValues();
        values.put(NotificationContract.NotificationEntry.COLUMN_NOTIFICATION_READ, 1);
        int rowsAffected = getContentResolver().update(currentNotificationUri, values, null, null);
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopUpdater();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        if (AppData.mPolygonsMap == null || AppData.mPolygonsMap.isEmpty()) {
            if (zid != -1)
                startUpdater(zid);
        }
    }

    //let's check up the Timer
    private Timer updaterTimer;
    private NotificationActivity.NotificationUpdater mUpdater;
    private Context context = this;

    private void startUpdater(int zid) {
        if (updaterTimer != null) {
            updaterTimer.cancel();
        }
        // re-schedule timer here otherwise, IllegalStateException of "TimerTask is scheduled already" will be thrown
        updaterTimer = new Timer();
        mUpdater = new NotificationActivity.NotificationUpdater(zid);
        updaterTimer.schedule(mUpdater, 0, 2000);
    }

    private void stopUpdater() {
        if (updaterTimer != null) {
            updaterTimer.cancel();
        }
    }

    class NotificationUpdater extends TimerTask {
        int zid;

        public NotificationUpdater(int zid) {
            this.zid = zid;
        }

        @Override
        public void run() {
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    if (osm_map != null && AppData.mPolygonsMap != null && !AppData.mPolygonsMap.isEmpty()) {
                        showZoneOnMap(zid);
                        Log.i(LOG_TAG, "stop updater...");
                        osm_map.invalidate();
                        stopUpdater();
                        return;
                    }
                    if (AppData.mainActivity != null) {
                        AppData.mainActivity.loaderManager.destroyLoader(AppData.ZONES_LOADER_ID);
                        Log.i(LOG_TAG, "trying to get zones...");
                        AppData.mainActivity.getZones();
                        if (!AppData.mainActivity.isConnected) {
                            Toast.makeText(context, "Отсутствует подключение к сети", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        startActivity(new Intent(NotificationActivity.this, AuthActivity.class));
                    }
                }
            });
        }
    }


    //Old
    //to get notification data from intent
//    private NotificationsData ndForeground;
//    private NotificationsData ndBackground;

    //trying to show data from fore or background
//    showData(ndBackground);
//    showData(ndForeground);

//    private NotificationsData getNotificationData() {
//        int id = 0;
//        String title = null;
//        String body = null;
//        String sending_time = null;
//        double latitude = 0.0;
//        double longitude = 0.0;
//        String text = null;
//
//        if (getIntent().getExtras() != null) {
//            for (String key : getIntent().getExtras().keySet()) {
//                Object value = getIntent().getExtras().get(key);
//                Log.i("MainActivity: ", "Key: " + key + " Value: " + value);
//                if (key.equals("id")) {
//                    try {
//                        id = Integer.parseInt(String.valueOf(value));
//                    } catch (NumberFormatException e) {
//                    }
//                }
//                if (key.equals("title")) {
//                    title = String.valueOf(value);
//                }
//                if (key.equals("body")) {
//                    body = String.valueOf(value);
//                }
//                if (key.equals("date")) {
//                    sending_time = String.valueOf(value);
//                }
//                if (key.equals("text")) {
//                    sending_time = String.valueOf(value);
//                }
//                if (key.equals("latitude")) {
//                    try {
//                        latitude = Double.parseDouble(String.valueOf(value));
//                    } catch (NumberFormatException e) {
//                    }
//
//                }
//                if (key.equals("longitude")) {
//                    try {
//                        longitude = Double.parseDouble(String.valueOf(value));
//                    } catch (NumberFormatException e) {
//                    }
//
//                }
//            }
//        }
//        if (id != 0 && title != null
//                && body != null
//                && sending_time != null
//                ) {
//            return new NotificationsData(id, title, body, sending_time, latitude, longitude, text);
//        } else return null;
//    }
//
//    //to insert data into database
//    private void insertNotification(NotificationsData nd) {
//        // Create a new map of values, where column names are the keys
//        ContentValues values = new ContentValues();
//        values.put(NotificationContract.NotificationEntry.COLUMN_NOTIFICATION_ID, nd.getId());
//        values.put(NotificationContract.NotificationEntry.COLUMN_NOTIFICATION_TITLE, nd.getTitle());
//        values.put(NotificationContract.NotificationEntry.COLUMN_NOTIFICATION_BODY, nd.getBody());
//        values.put(NotificationContract.NotificationEntry.COLUMN_NOTIFICATION_SENDING_TIME, nd.getSending_time());
//        values.put(NotificationContract.NotificationEntry.COLUMN_NOTIFICATION_LATITUDE, nd.getLatitude());
//        values.put(NotificationContract.NotificationEntry.COLUMN_NOTIFICATION_LONGITUDE, nd.getLongitude());
//        Uri newUri = getContentResolver().insert(NotificationContract.NotificationEntry.CONTENT_URI, values);
//    }


//    //to show data in this activity
//    private void showData(NotificationsData nd) {
//        if (nd != null) {
//            notificationTitle.setText(nd.getTitle());
//            notificationBody.setText(nd.getBody());
////                    +  " Номер бортового комплекта - " + String.valueOf(nd.getId()));
//            notificationSendingTime.setText(getFormattedDate(nd.getSending_time()));
//        }
//    }
}
