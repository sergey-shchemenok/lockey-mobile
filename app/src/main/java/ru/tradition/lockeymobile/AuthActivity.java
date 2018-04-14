package ru.tradition.lockeymobile;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.iid.FirebaseInstanceId;

import java.net.HttpURLConnection;

import ru.tradition.lockeymobile.auth.AuthQueryUtils;
import ru.tradition.lockeymobile.auth.TokenLoader;
import ru.tradition.lockeymobile.tabs.notifications.NotificationsData;
import ru.tradition.lockeymobile.tabs.notifications.database.NotificationContract;


public class AuthActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<String> {

//    private TextView result;

    private EditText loginView;
    private EditText passwordView;
    private Button loginButton;

    public static final String LOG_TAG = AuthActivity.class.getName();

    private ConnectivityManager connectivityManager;
    private LoaderManager loaderManager;
    private NetworkInfo activeNetwork;

    private TextView infoMessage;
    private TextView connectionStatusMessage;

    //token of device
    String fcmToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //firebase notification
//        if(AppData.noticeReceived == true){
////            AppData.pwd = passwordView.getText().toString();
////            AppData.usr = loginView.getText().toString();
//            //get token. If it is correct start main activity
//            getToken();
//        }

        setContentView(R.layout.activity_auth);

        //token for firebase cloud messaging
        fcmToken = FirebaseInstanceId.getInstance().getToken();
        Log.i(LOG_TAG, "the token is: " + fcmToken);

        //in case we get this activity after clicking the notification from background
        NotificationsData notificationsData = getNotificationData();
        if (notificationsData != null) {
            insertNotification(notificationsData);
        }

        try {
            AppData.mainActivity.changeModeToNormal();
        } catch (NullPointerException e) {
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        loginView = (EditText) findViewById(R.id.edit_login);
        //loginView.setBackgroundColor(Color.LTGRAY);
        passwordView = (EditText) findViewById(R.id.edit_password);
        loginButton = (Button) findViewById(R.id.login_button);

        infoMessage = (TextView) findViewById(R.id.main_info_message);
        infoMessage.setVisibility(View.INVISIBLE);

        connectionStatusMessage = (TextView) findViewById(R.id.main_connection_message);
        connectionStatusMessage.setVisibility(View.GONE);

        //to prevent authorization from notification activity
        if (getIntent().getExtras() != null) {
            boolean hasCredentials = true;
            if (getIntent().getExtras().containsKey("hasCredentials"))
                hasCredentials = getIntent().getExtras().getBoolean("hasCredentials");
            if (hasCredentials == false) {
                infoMessage.setVisibility(View.VISIBLE);
                infoMessage.setText("Требуется авторизация");
            }
        }

        if (!AppData.usr.isEmpty())
            loginView.setText(AppData.usr);
        if (!AppData.pwd.isEmpty())
            passwordView.setText(AppData.pwd);

        mHandler = new Handler();

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AppData.pwd = passwordView.getText().toString();
                AppData.usr = loginView.getText().toString();
                //get token. If it is correct start main activity
                getToken();
            }
        });

        //just for test version
        loginButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                if (fcmToken != null) {
                    String[] addresses = new String[2];
                    addresses[1] = "alex.zador@gmail.com";
                    addresses[0] = "shemenok@tradition.ru";
                    Intent intent = new Intent(Intent.ACTION_SENDTO);
                    intent.setData(Uri.parse("mailto:")); // only email apps should handle this
                    intent.putExtra(Intent.EXTRA_EMAIL, addresses);
                    intent.putExtra(Intent.EXTRA_SUBJECT, "Токен тестового устройства");
                    intent.putExtra(Intent.EXTRA_TEXT, fcmToken);
                    if (intent.resolveActivity(getPackageManager()) != null) {
                        startActivity(intent);
                    }
                }

                return true;
            }
        });

    }

    private NotificationsData getNotificationData() {
        int id = 0;
        String title = null;
        String body = null;
        String sending_time = null;
        double latitude = 0.0;
        double longitude = 0.0;

        if (getIntent().getExtras() != null) {
            for (String key : getIntent().getExtras().keySet()) {
                Object value = getIntent().getExtras().get(key);
                Log.i("MainActivity: ", "Key: " + key + " Value: " + value);
                if (key.equals("id")) {
                    try {
                        id = Integer.parseInt(String.valueOf(value));
                    } catch (NumberFormatException e) {
                    }
                }
                if (key.equals("title")) {
                    title = String.valueOf(value);
                }
                if (key.equals("body")) {
                    body = String.valueOf(value);
                }
                if (key.equals("date")) {
                    sending_time = String.valueOf(value);
                }
                if (key.equals("latitude")) {
                    try {
                        latitude = Double.parseDouble(String.valueOf(value));
                    } catch (NumberFormatException e) {
                    }

                }
                if (key.equals("longitude")) {
                    try {
                        longitude = Double.parseDouble(String.valueOf(value));
                    } catch (NumberFormatException e) {
                    }

                }
            }
        }
        if (id != 0 && title != null
                && body != null
                && sending_time != null
                && latitude != 0.0
                && longitude != 0.0) {
            return new NotificationsData(id, title, body, sending_time, latitude, longitude);
        } else if (id != 0 && title != null
                && body != null
                && sending_time != null) {
            return new NotificationsData(id, title, body, sending_time);
        }
        //todo add other datas (longitude and latitude)
        else return null;
    }

    private void insertNotification(NotificationsData nd) {
        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(NotificationContract.NotificationEntry.COLUMN_NOTIFICATION_ASSET_ID, nd.getId());
        values.put(NotificationContract.NotificationEntry.COLUMN_NOTIFICATION_TITLE, nd.getTitle());
        values.put(NotificationContract.NotificationEntry.COLUMN_NOTIFICATION_BODY, nd.getBody());
        values.put(NotificationContract.NotificationEntry.COLUMN_NOTIFICATION_SENDING_TIME, nd.getSending_time());
        values.put(NotificationContract.NotificationEntry.COLUMN_NOTIFICATION_LATITUDE, nd.getLatitude());
        values.put(NotificationContract.NotificationEntry.COLUMN_NOTIFICATION_LONGITUDE, nd.getLongitude());

        // Insert a new row for Toto in the database, returning the ID of that new row.
        // The first argument for db.insert() is the pets table name.
        // The second argument provides the name of a column in which the framework
        // can insert NULL in the event that the ContentValues is empty (if
        // this is set to "null", then the framework will not insert a row when
        // there are no values).
        // The third argument is the ContentValues object containing the info for Toto.
        //long newRowId = db.insert(PetContract.PetEntry.TABLE_NAME, null, values);
        Uri newUri = getContentResolver().insert(NotificationContract.NotificationEntry.CONTENT_URI, values);
    }

    public void getToken() {
        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        activeNetwork = connectivityManager.getActiveNetworkInfo();
        if (activeNetwork != null && activeNetwork.isConnected()) {
            loaderManager = getLoaderManager();
            loaderManager.initLoader(AppData.AUTH_LOADER_ID, null, this);
            Log.v(LOG_TAG, "initLoader");
        } else {
            connectionStatusMessage.setVisibility(View.VISIBLE);
            connectionStatusMessage.setText(R.string.no_connection);
        }
    }

    @Override
    public Loader<String> onCreateLoader(int i, Bundle bundle) {
        // Create a new loader for the given URL
        Log.v(LOG_TAG, "onCreateLoader");
        return new TokenLoader(this, AppData.AUTH_REQUEST_URL, AppData.pwd, AppData.usr);
    }

    @Override
    public void onLoadFinished(Loader<String> loader, String message) {
        loaderManager.destroyLoader(AppData.AUTH_LOADER_ID);
        if (!message.equals("OK") && AuthQueryUtils.authUrlResponseCode != HttpURLConnection.HTTP_OK) {
            infoMessage.setVisibility(View.VISIBLE);
            infoMessage.setText(AuthQueryUtils.authUrlResponseMessage);
            return;
        }
        if (!message.equals("OK")) {
            infoMessage.setVisibility(View.VISIBLE);
            infoMessage.setText(R.string.no_credentials);
            return;
        }
        AppData.isFinished = false;
        AppData.isRepeated = false;
        if (AppData.viewPager != null)
            AppData.viewPager.setCurrentItem(0);
        infoMessage.setVisibility(View.INVISIBLE);
        Log.v(LOG_TAG, "onLoadFinished");
        Intent intent = new Intent(AuthActivity.this, MainActivity.class);
        AppData.isAuthorized = true;
        startActivity(intent);
    }

    @Override
    public void onLoaderReset(Loader<String> loader) {
        Log.v(LOG_TAG, "onLoadReset");

    }

    @Override
    protected void onStart() {
        Log.i(LOG_TAG, "Activity has started..............................");
        super.onStart();
        AppData.isAuthorized = false;
        startRepeatingTask();
    }

    @Override
    protected void onStop() {
        Log.i(LOG_TAG, "Activity has stopped..............................");
        super.onStop();
        stopRepeatingTask();
    }

    //The code for checking internet connection
    private int mInterval = 1000 * 1; // 1 seconds by default, can be changed later
    private Handler mHandler;

    Runnable mStatusOfNetworkChecker = new Runnable() {
        @Override
        public void run() {
            try {
                Log.i(LOG_TAG, "checking network");
                connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                activeNetwork = connectivityManager.getActiveNetworkInfo();
                if (activeNetwork != null && activeNetwork.isConnected()) {
                    connectionStatusMessage.setVisibility(View.GONE);
                    connectionStatusMessage.setText("");
                } else {
                    connectionStatusMessage.setVisibility(View.VISIBLE);
                    connectionStatusMessage.setText(R.string.no_connection);
                }
            } finally {
                mHandler.postDelayed(mStatusOfNetworkChecker, mInterval);
            }
        }
    };

    void startRepeatingTask() {
        mStatusOfNetworkChecker.run();
    }

    void stopRepeatingTask() {
        mHandler.removeCallbacks(mStatusOfNetworkChecker);
    }
    //end here

    //we don't need to move to previous activity from here
    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }
}
