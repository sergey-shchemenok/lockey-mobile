package ru.tradition.lockeymobile.tabs.notifications.database;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Caelestis on 04.12.2017.
 */

public final class NotificationContract {
    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    private NotificationContract() {
    }

    public static final String CONTENT_AUTHORITY = "ru.tradition.lockeymobile";

    /**
     * Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
     * the content provider.
     */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    /**
     * Possible path (appended to base content URI for possible URI's)
     */
    public static final String PATH_NOTIFICATIONS = "notifications";


    /* Inner class that defines the table contents */
    public static abstract class NotificationEntry implements BaseColumns {
        /**
         * The content URI to access the notification data in the provider
         */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_NOTIFICATIONS);

        /**
         * The MIME type of the {@link #CONTENT_URI} for a list of notifications.
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_NOTIFICATIONS;

        /**
         * The MIME type of the {@link #CONTENT_URI} for a single notification.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_NOTIFICATIONS;

        /**
         * Name of database table for notifications
         */
        public final static String TABLE_NAME = "notifications";

        /**
         * Unique ID number for the notification (only for use in the database table).
         * <p>
         * Type: INTEGER
         */
        public final static String _ID = BaseColumns._ID;

        /**
         * ID .
         * <p>
         * Type: INTEGER
         */
        public final static String COLUMN_NOTIFICATION_ID = "notification_id";

        /**
         * asset ID .
         * <p>
         * Type: INTEGER
         */
        public final static String COLUMN_NOTIFICATION_ASSET_ID = "asset_id";

        /**
         * Title of the notification.
         * <p>
         * Type: TEXT
         */
        public final static String COLUMN_NOTIFICATION_TITLE = "title";

        /**
         * Body message of the notification.
         * <p>
         * Type: TEXT
         */
        public final static String COLUMN_NOTIFICATION_BODY = "body";

        /**
         * Sending time.
         * Type: TEXT
         */
        public final static String COLUMN_NOTIFICATION_SENDING_TIME = "sending_time";

        /**
         * Latitude of the asset.
         * Type: REAL
         */
        public final static String COLUMN_NOTIFICATION_LATITUDE = "latitude";

        /**
         * Longitude of the asset.
         * Type: REAL
         */
        public final static String COLUMN_NOTIFICATION_LONGITUDE = "longitude";

        /**
         * Status of notification.
         * Type: INTEGER
         */
        public final static String COLUMN_NOTIFICATION_READ = "read";

        /**
         * Picture of notification.
         * Type: BLOB
         */
        public final static String COLUMN_NOTIFICATION_PIC = "picture";

        /**
         * Other text.
         * Type: TEXT
         */
        public final static String COLUMN_NOTIFICATION_TEXT = "text";

    }


}
