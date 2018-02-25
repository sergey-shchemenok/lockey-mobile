package ru.tradition.lockeymobile.tabs.notifications;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import ru.tradition.lockeymobile.R;
import ru.tradition.lockeymobile.tabs.notifications.database.NotificationContract;

/**
 * Created by Caelestis on 06.12.2017.
 */

public class NotificationCursorAdapter extends CursorAdapter {

    /**
     * Constructs a new {@link NotificationCursorAdapter}.
     *
     * @param context The context
     * @param c       The cursor from which to get the data.
     */
    public NotificationCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    /**
     * Makes a new blank list item view. No data is set (or bound) to the views yet.
     *
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already
     *                moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return the newly created list item view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.notification_list_item, parent, false);
    }

    /**
     * This method binds the notification data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the name for the current notification can be set on the name TextView
     * in the list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // Find fields to populate in inflated template
        TextView titleTextView = (TextView) view.findViewById(R.id.notification_title);
        TextView bodyTextView = (TextView) view.findViewById(R.id.notification_body);
        TextView sendingTimeTextView = (TextView) view.findViewById(R.id.notification_sending_time);


        // Extract properties from cursor
        String notificationTitle = cursor.getString(cursor.getColumnIndexOrThrow(NotificationContract.NotificationEntry.COLUMN_NOTIFICATION_TITLE));
        String notificationBody = cursor.getString(cursor.getColumnIndexOrThrow(NotificationContract.NotificationEntry.COLUMN_NOTIFICATION_BODY));
        String notificationSendingTime = cursor.getString(cursor.getColumnIndexOrThrow(NotificationContract.NotificationEntry.COLUMN_NOTIFICATION_SENDING_TIME));
        // Populate fields with extracted properties
        titleTextView.setText(notificationTitle);
        sendingTimeTextView.setText(notificationSendingTime);


        //We need something to show if breed field is empty
        if (!TextUtils.isEmpty(notificationBody)) {
            bodyTextView.setText(notificationBody);
        } else {
            bodyTextView.setText("Empty message");
        }
    }
}