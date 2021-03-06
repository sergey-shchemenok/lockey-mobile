package ru.tradition.lockeymobile.tabs.notifications.fcmservices;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;

import ru.tradition.lockeymobile.MainActivity;
import ru.tradition.lockeymobile.NotificationActivity;
import ru.tradition.lockeymobile.R;
import ru.tradition.lockeymobile.tabs.notifications.NotificationsData;
import ru.tradition.lockeymobile.tabs.notifications.database.NotificationContract;

/**
 * Created by Caelestis on 24.12.2017.
 */
public class FcmMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {

    private static final String NOTIFICATION_ID_PREFERENCES = "notification_id_preferences";
    public static final String FCM_NOTIFICATION_ID = "fcm_notification_id";
    SharedPreferences preferences;

    private static final String LOG_TAG = FcmInstanceIDService.class.getSimpleName();

    private static int isRead = 0;

    //private static final int FCM_NOTIFICATION_ID = 1000;
    private static int fcmNotificationId = 0;
    private static final int FCM_PENDING_INTENT_ID = 2000;
    private static final String FCM_NOTIFICATION_CHANNEL_ID = "fcm_notification_channel";

    @Override
    public void onCreate() {
        super.onCreate();
        preferences = getSharedPreferences(NOTIFICATION_ID_PREFERENCES, Context.MODE_PRIVATE);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        Log.i(LOG_TAG, "FROM:" + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.i(LOG_TAG, "Message data payload: " + remoteMessage.getData());
            try {
                JSONObject data = new JSONObject(remoteMessage.getData());
                String jsonMessage = data.getString("extra_information");
                Log.i(LOG_TAG, "onMessageReceived: \n" +
                        "Extra Information: " + jsonMessage);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        // Check if message contains a notification payload.
        String click_action = "NOTIFICATIONACTIVITY";
        if (remoteMessage.getNotification() != null) {
            click_action = remoteMessage.getNotification().getClickAction(); //get click_action
            Log.i(LOG_TAG, "Message Notification click_action: " + click_action);
        }
        if (remoteMessage.getData() != null) {
            String title = String.valueOf(remoteMessage.getData().get("title"));
            String body = String.valueOf(remoteMessage.getData().get("body"));
            String date = String.valueOf(remoteMessage.getData().get("date"));
            int id = 0;
            double latitude = 0.0;
            double longitude = 0.0;
            String text = String.valueOf(remoteMessage.getData().get("text"));
            try {
                id = Integer.parseInt(String.valueOf(remoteMessage.getData().get("id")));
                latitude = Double.parseDouble(String.valueOf(remoteMessage.getData().get("latitude")));
                longitude = Double.parseDouble(String.valueOf(remoteMessage.getData().get("longitude")));
            } catch (NumberFormatException e) {
            }

            Log.i(LOG_TAG, "Message Notification Title: " + title);
            Log.i(LOG_TAG, "Message Notification Body: " + body);
            Log.i(LOG_TAG, "Message Notification id: " + id);
            Log.i(LOG_TAG, "Message Notification date: " + date);
            Log.i(LOG_TAG, "Message Notification latitude: " + latitude);
            Log.i(LOG_TAG, "Message Notification longitude: " + longitude);
            Log.i(LOG_TAG, "Message Notification longitude: " + text);

            sendNotification(id, title, body, click_action, date, latitude, longitude, text);
        }
    }

    /**
     * Dispay the notification
     */
//    private void sendNotification(String body) {
//
    private void sendNotification(int id, String title, String body, String click_action, String date,
                                  double latitude, double longitude, String text) {
        Log.i(LOG_TAG, body + ".........");

        Intent intent;
        if (click_action != null && !click_action.isEmpty()) {
            if (click_action.equals("NOTIFICATIONACTIVITY")) {
                intent = new Intent(this, NotificationActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            } else {
                intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            }
        } else {
            intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        }

        NotificationsData notificationsData = null;
        if (id != 0 && title != null
                && body != null
                && date != null
                ) {
            notificationsData = new NotificationsData(id, title, body, date, latitude, longitude, text);
        }
        if (notificationsData != null) {
            //todo nd could be empty
            Uri uri = insertNotification(notificationsData);
            if (uri == null)
                return;
            intent.putExtra("NotificationData", notificationsData);
            intent.putExtra("Uri", uri);
        }

//        if(preferences.contains(FCM_NOTIFICATION_ID)) {
//            fcmNotificationId = preferences.getInt(FCM_NOTIFICATION_ID, 0);
//            fcmNotificationId++;
//            SharedPreferences.Editor editor = preferences.edit();
//            editor.putInt(FCM_NOTIFICATION_ID, fcmNotificationId);
//            editor.commit();
//        }else {
//            SharedPreferences.Editor editor = preferences.edit();
//            fcmNotificationId = 0;
//            editor.putInt(FCM_NOTIFICATION_ID, fcmNotificationId);
//            editor.commit();
//        }

        fcmNotificationId = id;

        PendingIntent pendingIntent = PendingIntent.getActivity(this, fcmNotificationId/*Request code*/, intent, PendingIntent.FLAG_ONE_SHOT);

        Uri notificationSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            @SuppressLint("WrongConstant")
            NotificationChannel notificationChannel = new NotificationChannel(FCM_NOTIFICATION_CHANNEL_ID,
                    "Primary", NotificationManager.IMPORTANCE_MAX);

            // Configure the notification channel.
//            notificationChannel.setDescription("Channel description");
//            notificationChannel.enableLights(true);
//            notificationChannel.setLightColor(Color.RED);
//            notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
//            notificationChannel.enableVibration(true);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, FCM_NOTIFICATION_CHANNEL_ID)
                .setColor(Color.LTGRAY)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(body)
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setAutoCancel(true)
                .setSound(notificationSound)
                .setContentIntent(pendingIntent);

        // this is deprecated in API 26 but we can still use for below 26.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN
                && Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            notificationBuilder.setPriority(NotificationCompat.PRIORITY_MAX);
        }

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(fcmNotificationId /*ID of notification*/, notificationBuilder.build());
    }

    private Uri insertNotification(NotificationsData nd) {
        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();

        //filter
        String[] projectionTime = {NotificationContract.NotificationEntry.COLUMN_NOTIFICATION_SENDING_TIME};
        String selectionTime = NotificationContract.NotificationEntry.COLUMN_NOTIFICATION_SENDING_TIME + " = \"" + nd.getSending_time() + "\"";
        String[] projectionBody = {NotificationContract.NotificationEntry.COLUMN_NOTIFICATION_BODY};
        String selectionBody = NotificationContract.NotificationEntry.COLUMN_NOTIFICATION_BODY + " = \"" + nd.getBody() + "\"";
        Cursor cursorTime = getContentResolver().query(NotificationContract.NotificationEntry.CONTENT_URI, projectionTime,
                selectionTime, null, null);
        Cursor cursorBody = getContentResolver().query(NotificationContract.NotificationEntry.CONTENT_URI, projectionBody,
                selectionBody, null, null);
        if (cursorTime.getCount() > 0 && cursorBody.getCount() > 0) {
            cursorTime.close();
            cursorBody.close();
            return null;
        } else {
            cursorTime.close();
            cursorBody.close();
//        String savedSendingTime = cursor.getString(0);
//       int savedSendingTime = cursor.getColumnIndex(NotificationContract.NotificationEntry.COLUMN_NOTIFICATION_SENDING_TIME);
            Log.i(LOG_TAG, "time and body  " + cursorTime.getCount() + " " + cursorBody.getCount());

            //SQLiteException

            values.put(NotificationContract.NotificationEntry.COLUMN_NOTIFICATION_ID, nd.getId());
            //todo we can use this later
            values.put(NotificationContract.NotificationEntry.COLUMN_NOTIFICATION_ASSET_ID, 0);
            values.put(NotificationContract.NotificationEntry.COLUMN_NOTIFICATION_TITLE, nd.getTitle());
            values.put(NotificationContract.NotificationEntry.COLUMN_NOTIFICATION_BODY, nd.getBody());
            values.put(NotificationContract.NotificationEntry.COLUMN_NOTIFICATION_SENDING_TIME, nd.getSending_time());
            values.put(NotificationContract.NotificationEntry.COLUMN_NOTIFICATION_LATITUDE, nd.getLatitude());
            values.put(NotificationContract.NotificationEntry.COLUMN_NOTIFICATION_LONGITUDE, nd.getLongitude());
            values.put(NotificationContract.NotificationEntry.COLUMN_NOTIFICATION_READ, isRead);
            values.put(NotificationContract.NotificationEntry.COLUMN_NOTIFICATION_TEXT, nd.getText());

            return getContentResolver().insert(NotificationContract.NotificationEntry.CONTENT_URI, values);
        }
    }
}
