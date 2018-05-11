package ru.tradition.lockeymobile.tabs.notifications.database;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;


/**
 * Created by Caelestis on 06.12.2017.
 */

public class NotificationProvider extends ContentProvider {
    NotificationDbHelper mDbHelper;

    /**
     * URI matcher code for the content URI for the notifications table
     */
    private static final int NOTIFICATIONS = 100;

    /**
     * URI matcher code for the content URI for a single notification in the notifications table
     */
    private static final int NOTIFICATION_ID = 101;

    /**
     * UriMatcher object to match a content URI to a corresponding code.
     * The input passed into the constructor represents the code to return for the root URI.
     * It's common to use NO_MATCH as the input for this case.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer. This is run the first time anything is called from this class.
    static {
        // The calls to addURI() go here, for all of the content URI patterns that the provider
        // should recognize. All paths added to the UriMatcher have a corresponding code to return
        // when a match is found.

    /*
     * Sets the integer value for multiple rows in table. Notice that no wildcard is used
     * in the path
     */
        sUriMatcher.addURI(NotificationContract.CONTENT_AUTHORITY, NotificationContract.PATH_NOTIFICATIONS, NOTIFICATIONS);
        sUriMatcher.addURI(NotificationContract.CONTENT_AUTHORITY, NotificationContract.PATH_NOTIFICATIONS + "/#", NOTIFICATION_ID);
    }

    /**
     * Tag for the log messages
     */
    public static final String LOG_TAG = NotificationProvider.class.getSimpleName();

    /**
     * Initialize the provider and the database helper object.
     */
    @Override
    public boolean onCreate() {
        mDbHelper = new NotificationDbHelper(getContext());
        return true;
    }

    /**
     * Perform the query for the given URI. Use the given projection, selection, selection arguments, and sort order.
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
// Get readable database
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        // This cursor will hold the result of the query
        Cursor cursor;

        // Figure out if the URI matcher can match the URI to a specific code
        int match = sUriMatcher.match(uri);
        switch (match) {
            case NOTIFICATIONS:
                // For the NOTIFICATIONS code, query the notifications table directly with the given
                // projection, selection, selection arguments, and sort order. The cursor
                // could contain multiple rows of the notifications table.
                // This will perform a query on the notifications table where the _id equals 3 to return a
                // Cursor containing that row of the table.
                cursor = database.query(NotificationContract.NotificationEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case NOTIFICATION_ID:
                // For the NOTIFICATION_ID code, extract out the ID from the URI.
                // For an example URI such as "content://com.example.android.pets/pets/3",
                // the selection will be "_id=?" and the selection argument will be a
                // String array containing the actual ID of 3 in this case.
                //
                // For every "?" in the selection, we need to have an element in the selection
                // arguments that will fill in the "?". Since we have 1 question mark in the
                // selection, we have 1 String in the selection arguments' String array.
                selection = NotificationContract.NotificationEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                // This will perform a query on the notifications table where the _id equals 3 to return a
                // Cursor containing that row of the table.
                cursor = database.query(NotificationContract.NotificationEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        // Set notification URI on the Cursor,
        // so we know what content URI the Cursor was created for.
        // If the data at this URI changes, then we know we need to update the Cursor.
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    /**
     * Insert new data into the provider with the given ContentValues.
     */
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case NOTIFICATIONS:
                return insertNotification(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    /**
     * Insert a notification into the database with the given content values. Return the new content URI
     * for that specific row in the database.
     */
    private Uri insertNotification(Uri uri, ContentValues values) {
        // Check that the asset id is not null
        Integer assetID = values.getAsInteger(NotificationContract.NotificationEntry.COLUMN_NOTIFICATION_ASSET_ID);
        if (assetID == null) {
            throw new IllegalArgumentException("Notification requires an asset ID");
        }

        // Check that the title is not null
        String title = values.getAsString(NotificationContract.NotificationEntry.COLUMN_NOTIFICATION_TITLE);
        if (title.isEmpty() || title == null) {
            throw new IllegalArgumentException("Notification requires a title");
        }

        // Check that the body message is not null
        String body = values.getAsString(NotificationContract.NotificationEntry.COLUMN_NOTIFICATION_BODY);
        if (body.isEmpty() || body == null) {
            throw new IllegalArgumentException("Notification requires a body");
        }

        // Check that the sending time message is not null
        String sendingTime = values.getAsString(NotificationContract.NotificationEntry.COLUMN_NOTIFICATION_SENDING_TIME);
        if (sendingTime.isEmpty() || sendingTime == null) {
            throw new IllegalArgumentException("Notification requires a sending time");
        }

        // No need to check the other column values, any value is valid (including null).

        SQLiteDatabase database = mDbHelper.getReadableDatabase();
        long id = database.insert(NotificationContract.NotificationEntry.TABLE_NAME, null, values);
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }
        // Notify all listeners that the data has changed for the notification content URI
        getContext().getContentResolver().notifyChange(uri, null);

        // Once we know the ID of the new row in the table,
        // return the new URI with the ID appended to the end of it
        return ContentUris.withAppendedId(uri, id);
    }

    /**
     * Updates the data at the given selection and selection arguments, with the new ContentValues.
     */
    @Override
    public int update(Uri uri, ContentValues contentValues, String selection,
                      String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case NOTIFICATIONS:
                return updateNotification(uri, contentValues, selection, selectionArgs);
            case NOTIFICATION_ID:
                // For the NOTIFICATION_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = NotificationContract.NotificationEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateNotification(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    /**
     * Update notifications in the database with the given content values.
     * Return the number of rows that were successfully updated.
     */
    private int updateNotification(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // No need to check the other column values, any value is valid (including null).
        // If there are no values to update, then don't try to update the database
        if (values.size() == 0) {
            return 0;
        }

        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        // Perform the update on the database and get the number of rows affected
        int rowsUpdated = database.update(NotificationContract.NotificationEntry.TABLE_NAME, values, selection, selectionArgs);

        // If 1 or more rows were updated, then notify all listeners that the data at the
        // given URI has changed
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of rows updated
        return rowsUpdated;
    }

    /**
     * Delete the data at the given selection and selection arguments.
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Track the number of rows that were deleted
        int rowsDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case NOTIFICATIONS:
                // Delete all rows that match the selection and selection args
                rowsDeleted = database.delete(NotificationContract.NotificationEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case NOTIFICATION_ID:
                // Delete a single row given by the ID in the URI
                selection = NotificationContract.NotificationEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(NotificationContract.NotificationEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }
        // If 1 or more rows were deleted, then notify all listeners that the data at the
        // given URI has changed
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        // Return the number of rows deleted
        return rowsDeleted;
    }

    /**
     * Returns the MIME type of data for the content URI.
     */
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case NOTIFICATIONS:
                return NotificationContract.NotificationEntry.CONTENT_LIST_TYPE;
            case NOTIFICATION_ID:
                return NotificationContract.NotificationEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }
}
