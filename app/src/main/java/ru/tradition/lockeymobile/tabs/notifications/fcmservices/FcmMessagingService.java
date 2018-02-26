package ru.tradition.lockeymobile.tabs.notifications.fcmservices;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;

import ru.tradition.lockeymobile.AuthActivity;
import ru.tradition.lockeymobile.MainActivity;
import ru.tradition.lockeymobile.NotificationActivity;
import ru.tradition.lockeymobile.R;
import ru.tradition.lockeymobile.tabs.assetstab.AssetsData;
import ru.tradition.lockeymobile.tabs.notifications.NotificationsData;
import ru.tradition.lockeymobile.tabs.notifications.database.NotificationContract;

/**
 * Created by Caelestis on 24.12.2017.
 */
public class FcmMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {

    private static final String LOG_TAG = FcmInstanceIDService.class.getSimpleName();

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
        if (remoteMessage.getNotification() != null) {
            String click_action = remoteMessage.getNotification().getClickAction(); //get click_action
            String title = String.valueOf(remoteMessage.getData().get("title"));
            String body = String.valueOf(remoteMessage.getData().get("body"));
            String date = String.valueOf(remoteMessage.getData().get("date"));
            int id = 0;
            double latitude = 0.0;
            double longitude = 0.0;
            try {
                id = Integer.parseInt(String.valueOf(remoteMessage.getData().get("id")));
                latitude = Double.parseDouble(String.valueOf(remoteMessage.getData().get("latitude")));
                longitude = Double.parseDouble(String.valueOf(remoteMessage.getData().get("longitude")));
            } catch (NumberFormatException e) {
            }

            Log.i(LOG_TAG, "Message Notification Title: " + title);
            Log.i(LOG_TAG, "Message Notification Body: " + body);
            Log.i(LOG_TAG, "Message Notification click_action: " + click_action);
            Log.i(LOG_TAG, "Message Notification id: " + id);
            Log.i(LOG_TAG, "Message Notification date: " + date);
            Log.i(LOG_TAG, "Message Notification latitude: " + latitude);
            Log.i(LOG_TAG, "Message Notification longitude: " + longitude);


            sendNotification(id, title, body, click_action, date, latitude, longitude);
        }

    }

    /**
     * Dispay the notification
     */
//    private void sendNotification(String body) {
//
    private void sendNotification(int id, String title, String body, String click_action, String date, double latitude, double longitude) {
        Log.i(LOG_TAG, body + ".........");

        Intent intent;
        if (click_action.equals("AUTHACTIVITY")) {
            intent = new Intent(this, NotificationActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        } else if (click_action.equals("MAINACTIVITY")) {
            intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        } else {
            intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        }

        NotificationsData notificationsData = null;

        if (id != 0 && title != null
                && body != null
                && date != null
                && latitude != 0.0
                && longitude != 0.0) {
            notificationsData = new NotificationsData(id, title, body, date, latitude, longitude);
        } else if (id != 0 && title != null
                && body != null
                && date != null) {
            notificationsData = new NotificationsData(id, title, body, date);
        }
        if (notificationsData != null) {
            //todo nd could be empty
            insertNotification(notificationsData);
            intent.putExtra("NotificationData", notificationsData);
        }
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0/*Request code*/, intent, PendingIntent.FLAG_ONE_SHOT);
        //Set sound of notification
        Log.i(LOG_TAG, click_action + ".........pending intent");

        Uri notificationSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String NOTIFICATION_CHANNEL_ID = "my_channel_id_01";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            @SuppressLint("WrongConstant") NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "My Notifications", NotificationManager.IMPORTANCE_MAX);

            // Configure the notification channel.
            notificationChannel.setDescription("Channel description");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
            notificationChannel.enableVibration(true);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        NotificationCompat.Builder notifiBuilder = new NotificationCompat.Builder(this, "M_CH_ID")
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setPriority(Notification.PRIORITY_MAX) // this is deprecated in API 26 but we can still use for below 26.
                .setContentText(body)
                //.setContentInfo("Info")
                .setAutoCancel(true)
                .setSound(notificationSound)
                .setContentIntent(pendingIntent);

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0 /*ID of notification*/, notifiBuilder.build());


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

        Uri newUri = getContentResolver().insert(NotificationContract.NotificationEntry.CONTENT_URI, values);
    }


}
