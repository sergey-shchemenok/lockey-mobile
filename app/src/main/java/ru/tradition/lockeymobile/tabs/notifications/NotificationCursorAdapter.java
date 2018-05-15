package ru.tradition.lockeymobile.tabs.notifications;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

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
        notificationItemShort.setVisibility(View.VISIBLE);

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
                            + cursor.getInt(cursor.getColumnIndexOrThrow(NotificationContract.NotificationEntry._ID)))) {
                rootView.setBackgroundColor(Color.LTGRAY);
                checkmark.setVisibility(View.VISIBLE);
            }
        }

        if (AppData.selectedNotificationLong.contains(
                "content://ru.tradition.lockeymobile/notifications/"
                        + cursor.getInt(cursor.getColumnIndexOrThrow(NotificationContract.NotificationEntry._ID)))) {
            notificationItemShort.setVisibility(View.GONE);
            notificationItemLong.setVisibility(View.VISIBLE);
        }

        TextView titleTextView = (TextView) view.findViewById(R.id.notification_title);
        TextView bodyTextView = (TextView) view.findViewById(R.id.notification_body);
        TextView sendingTimeTextView = (TextView) view.findViewById(R.id.notification_sending_time);

        TextView titleTextViewOptional = (TextView) view.findViewById(R.id.notification_title_optional);
        TextView bodyTextViewOptional = (TextView) view.findViewById(R.id.notification_body_optional);
        TextView sendingTimeTextViewOptional = (TextView) view.findViewById(R.id.notification_sending_time_optional);


        // Extract properties from cursor
        int notificationID = cursor.getInt(cursor.getColumnIndexOrThrow(NotificationContract.NotificationEntry.COLUMN_NOTIFICATION_ID));
        double notificationLatitude = cursor.getDouble(cursor.getColumnIndexOrThrow(NotificationContract.NotificationEntry.COLUMN_NOTIFICATION_LATITUDE));
        double notificationLongitude = cursor.getDouble(cursor.getColumnIndexOrThrow(NotificationContract.NotificationEntry.COLUMN_NOTIFICATION_LONGITUDE));
        String notificationTitle = cursor.getString(cursor.getColumnIndexOrThrow(NotificationContract.NotificationEntry.COLUMN_NOTIFICATION_TITLE));
        String notificationBody = cursor.getString(cursor.getColumnIndexOrThrow(NotificationContract.NotificationEntry.COLUMN_NOTIFICATION_BODY));
        String notificationSendingTime = cursor.getString(cursor.getColumnIndexOrThrow(NotificationContract.NotificationEntry.COLUMN_NOTIFICATION_SENDING_TIME));
        String formattedTime = getFormattedDate(notificationSendingTime);
        int isRead = cursor.getInt(cursor.getColumnIndexOrThrow(NotificationContract.NotificationEntry.COLUMN_NOTIFICATION_READ));

        // Populate fields with extracted properties
        titleTextView.setText(notificationTitle);
        sendingTimeTextView.setText(formattedTime);

        titleTextViewOptional.setText(notificationTitle);
        titleTextViewOptional.setTypeface(null, Typeface.NORMAL);
        sendingTimeTextViewOptional.setText(formattedTime);

        bodyTextView.setText(notificationBody);// + " Номер бортового комплекта - " + String.valueOf(notificationID));
        bodyTextViewOptional.setText(notificationBody);// + " Номер бортового комплекта - " + String.valueOf(notificationID));
        if (isRead == 0) {
            sendingTimeTextView.setTypeface(null, Typeface.BOLD);
            bodyTextView.setTypeface(null, Typeface.BOLD);
            titleTextView.setTypeface(null, Typeface.BOLD);
        } else {
            bodyTextView.setTypeface(null, Typeface.NORMAL);
            sendingTimeTextView.setTypeface(null, Typeface.NORMAL);
            titleTextView.setTypeface(null, Typeface.NORMAL);
        }

        //We need something to show if body text field is empty
//        if (!TextUtils.isEmpty(notificationBody)) {
//            bodyTextView.setText(notificationBody);
//            bodyTextViewOptional.setText(notificationBody);
//        } else {
//            bodyTextView.setText("Empty message");
//            bodyTextViewOptional.setText(notificationBody);
//        }
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

}