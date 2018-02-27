package ru.tradition.lockeymobile.tabs.notifications;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.support.v4.widget.CursorAdapter;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import ru.tradition.lockeymobile.AppData;
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

        //to make the unfolding
        LinearLayout notificationItemShort = (LinearLayout) view.findViewById(R.id.notification_item_short);
        LinearLayout notificationItemLong = (LinearLayout) view.findViewById(R.id.notification_item_long);
        notificationItemLong.setVisibility(View.GONE);

        //checkbox
        LinearLayout rootView = (LinearLayout) view.findViewById(R.id.list_notification_root);
        rootView.setBackgroundColor(Color.WHITE);
        RelativeLayout checkBox = (RelativeLayout) view.findViewById(R.id.list_notifications_checkbox);
        ImageView checkmark = (ImageView) view.findViewById(R.id.list_notifications_checkmark);
        ImageView checkmarkEmpty = (ImageView) view.findViewById(R.id.list_notifications_checkmark_empty);
        if (!AppData.isNotificationSelectingMode) {
            checkBox.setVisibility(View.GONE);
            checkmark.setVisibility(View.INVISIBLE);
            checkmarkEmpty.setVisibility(View.INVISIBLE);
        } else {
            checkBox.setVisibility(View.VISIBLE);
            checkmarkEmpty.setVisibility(View.VISIBLE);
            checkmark.setVisibility(View.INVISIBLE);
            if (AppData.selectedNotification.contains(
                    "content://ru.tradition.lockeymobile/notifications/"
                    + cursor.getInt(cursor.getColumnIndexOrThrow(NotificationContract.NotificationEntry._ID)))){
                rootView.setBackgroundColor(Color.LTGRAY);
                checkmark.setVisibility(View.VISIBLE);
            }
        }


        TextView titleTextView = (TextView) view.findViewById(R.id.notification_title);
        TextView bodyTextView = (TextView) view.findViewById(R.id.notification_body);
        TextView sendingTimeTextView = (TextView) view.findViewById(R.id.notification_sending_time);

        TextView titleTextViewOptional = (TextView) view.findViewById(R.id.notification_title_optional);
        TextView bodyTextViewOptional = (TextView) view.findViewById(R.id.notification_body_optional);
        TextView sendingTimeTextViewOptional = (TextView) view.findViewById(R.id.notification_sending_time_optional);


        // Extract properties from cursor
        String notificationTitle = cursor.getString(cursor.getColumnIndexOrThrow(NotificationContract.NotificationEntry.COLUMN_NOTIFICATION_TITLE));
        String notificationBody = cursor.getString(cursor.getColumnIndexOrThrow(NotificationContract.NotificationEntry.COLUMN_NOTIFICATION_BODY));
        String notificationSendingTime = cursor.getString(cursor.getColumnIndexOrThrow(NotificationContract.NotificationEntry.COLUMN_NOTIFICATION_SENDING_TIME));
        // Populate fields with extracted properties
        titleTextView.setText(notificationTitle);
        sendingTimeTextView.setText(notificationSendingTime);

        titleTextViewOptional.setText(notificationTitle);
        sendingTimeTextViewOptional.setText(notificationSendingTime);


        //We need something to show if breed field is empty
        if (!TextUtils.isEmpty(notificationBody)) {
            bodyTextView.setText(notificationBody);
            bodyTextViewOptional.setText(notificationBody);
        } else {
            bodyTextView.setText("Empty message");
            bodyTextViewOptional.setText(notificationBody);
        }
    }
}