package ru.tradition.lockeymobile.tabs.notifications.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by Caelestis on 04.12.2017.
 */

public class NotificationDbHelper extends SQLiteOpenHelper {

    public static final String LOG_TAG = NotificationDbHelper.class.getSimpleName();

    /**
     * Name of the database file
     */
    private static final String DATABASE_NAME = "lockeydata.db";

    /**
     * Database version. If you change the database schema, you must increment the database version.
     */
    private static final int DATABASE_VERSION = 1;

    /**
     * notifications database CREATE method
     */
    private static final String SQL_CREATE_NOTIFICATIONS_TABLE = "CREATE TABLE " + NotificationContract.NotificationEntry.TABLE_NAME + "("
            + NotificationContract.NotificationEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + NotificationContract.NotificationEntry.COLUMN_NOTIFICATION_ASSET_ID + " INTEGER NOT NULL, "
            + NotificationContract.NotificationEntry.COLUMN_NOTIFICATION_TITLE + " TEXT NOT NULL, "
            + NotificationContract.NotificationEntry.COLUMN_NOTIFICATION_BODY + " TEXT NOT NULL, "
            + NotificationContract.NotificationEntry.COLUMN_NOTIFICATION_SENDING_TIME + " TEXT NOT NULL, "
            + NotificationContract.NotificationEntry.COLUMN_NOTIFICATION_LATITUDE + " REAL, "
            + NotificationContract.NotificationEntry.COLUMN_NOTIFICATION_LONGITUDE + " REAL, "
            + NotificationContract.NotificationEntry.COLUMN_NOTIFICATION_READ + " INTEGER NOT NULL DEFAULT 0);";

    /*
     * Constructs a new instance of {@link NotificationDbHelper}.
     *
     * @param context of the app
     */
    public NotificationDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * This is called when the database is created for the first time.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_NOTIFICATIONS_TABLE);
        Log.i(LOG_TAG, "Creating new database... " + SQL_CREATE_NOTIFICATIONS_TABLE);

    }

    /**
     * This is called when the database needs to be upgraded.
     */
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
