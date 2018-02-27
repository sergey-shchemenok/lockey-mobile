package ru.tradition.lockeymobile;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import ru.tradition.lockeymobile.tabs.assetstab.AssetsData;
import ru.tradition.lockeymobile.tabs.notifications.NotificationsData;
import ru.tradition.lockeymobile.tabs.notifications.database.NotificationContract;

public class NotificationActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private final String LOG_TAG = NotificationActivity.class.getName();
    private static final int CURSOR_LOADER_ID = 1;
    /**
     * Content URI for the existing notification
     */
    private Uri mCurrentNotificationUri;

    private TextView notificationTitle;
    private TextView notificationBody;
    private TextView notificationSendingTime;

    private NotificationsData nd;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        //go to auth activity
        if (AppData.usr.equals("")||AppData.pwd.equals("")||AppData.isAuthorized == false) {
            Intent intent = new Intent(this, AuthActivity.class);
            intent.putExtra("hasCredentials", false);
            startActivity(intent);
            Log.i(LOG_TAG, ".............no credentials");
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


        //get data from the incoming intent
        Intent intent = getIntent();
        mCurrentNotificationUri = intent.getData();

        if (mCurrentNotificationUri == null) {
            nd = (NotificationsData) getIntent().getSerializableExtra("NotificationData");
        } else
            getLoaderManager().initLoader(CURSOR_LOADER_ID, null, this);

        notificationTitle = (TextView) findViewById(R.id.notification_activity_title);
        notificationBody = (TextView) findViewById(R.id.notification_activity_body);
        notificationSendingTime = (TextView) findViewById(R.id.notification_activity_sending_time);

        if (nd != null){
            notificationTitle.setText(nd.getTitle());
            notificationBody.setText(nd.getBody());
            notificationSendingTime.setText(nd.getSending_time());
        }
    }

    //Loader methods
    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        //Write the code for the loader callback method onCreateLoader; make sure it is using a URI for one notification.
        String[] projection = {
                //id column is always needed for the cursor
                NotificationContract.NotificationEntry._ID,
                NotificationContract.NotificationEntry.COLUMN_NOTIFICATION_ASSET_ID,
                NotificationContract.NotificationEntry.COLUMN_NOTIFICATION_TITLE,
                NotificationContract.NotificationEntry.COLUMN_NOTIFICATION_BODY,
                NotificationContract.NotificationEntry.COLUMN_NOTIFICATION_SENDING_TIME,
                NotificationContract.NotificationEntry.COLUMN_NOTIFICATION_LATITUDE,
                NotificationContract.NotificationEntry.COLUMN_NOTIFICATION_LONGITUDE
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
            // Find the columns of pet attributes that we're interested in
            int assetIDColumnIndex = cursor.getColumnIndex(NotificationContract.NotificationEntry.COLUMN_NOTIFICATION_ASSET_ID);
            int titleColumnIndex = cursor.getColumnIndex(NotificationContract.NotificationEntry.COLUMN_NOTIFICATION_TITLE);
            int bodyColumnIndex = cursor.getColumnIndex(NotificationContract.NotificationEntry.COLUMN_NOTIFICATION_BODY);
            int sendingTimeColumnIndex = cursor.getColumnIndex(NotificationContract.NotificationEntry.COLUMN_NOTIFICATION_SENDING_TIME);

            // Extract out the value from the Cursor for the given column index
            int assetID = cursor.getInt(assetIDColumnIndex);

            String title = cursor.getString(titleColumnIndex);
            String body = cursor.getString(bodyColumnIndex);
            String sendingTime = cursor.getString(sendingTimeColumnIndex);

            // Update the views on the screen with the values from the database
            notificationTitle.setText(title);
            notificationBody.setText(body);
            notificationSendingTime.setText(sendingTime);

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
                //todo settings here
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void logout() {
        Intent intent = new Intent(this, AuthActivity.class);
        AppData.isFinished = false;
        AppData.isRepeated = false;
        startActivity(intent);
    }

    @Override
    public boolean onSupportNavigateUp() {
        //todo good navigation from previous - back from main - up
        super.onBackPressed();
        //return super.onSupportNavigateUp();
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
