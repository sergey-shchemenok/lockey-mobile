package ru.tradition.lockeymobile.firebasecm;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
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

import ru.tradition.lockeymobile.AppData;
import ru.tradition.lockeymobile.AssetActivity;
import ru.tradition.lockeymobile.AuthActivity;
import ru.tradition.lockeymobile.MainActivity;
import ru.tradition.lockeymobile.R;

/**
 * Created by NgocTri on 8/9/2016.
 */
public class LockeyFirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {

    private static final String LOG_TAG = LockeyFirebaseInstanceIDService.class.getSimpleName();

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        Log.i(LOG_TAG, "FROM:" + remoteMessage.getFrom());

//        //Check if the message contains data
//        if(remoteMessage.getData().size() > 0) {
//            Log.d(LOG_TAG, "Message data: " + remoteMessage.getData());
//        }
//
//        //Check if the message contains notification
//
//        if(remoteMessage.getNotification() != null) {
//            Log.d(LOG_TAG, "Mesage body:" + remoteMessage.getNotification().getBody());
//            sendNotification(remoteMessage.getNotification().getBody());
//        }
        //Log.i(TAG, "Содержимое" + remoteMessage.getNotification().get);


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
            String title = remoteMessage.getNotification().getTitle(); //get title
            String message = remoteMessage.getNotification().getBody(); //get message
            String click_action = remoteMessage.getNotification().getClickAction(); //get click_action

            Log.i(LOG_TAG, "Message Notification Title: " + title);
            Log.i(LOG_TAG, "Message Notification Body: " + message);
            Log.i(LOG_TAG, "Message Notification click_action: " + click_action);

            sendNotification(title, message, "AUTHACTIVITY");
        }


    }

    /**
     * Dispay the notification
     */
//    private void sendNotification(String body) {
//
    private void sendNotification(String title, String messageBody, String click_action) {
        Log.i(LOG_TAG, messageBody + ".........");

        Intent intent;
        if (click_action.equals("ASSETACTIVITY")) {
            intent = new Intent(this, AssetActivity.class);
            //AppData.noticeReceived = true;
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        } else if (click_action.equals("AUTHACTIVITY")) {
            intent = new Intent(this, AuthActivity.class);
            AppData.noticeReceived = true;
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        } else if (click_action.equals("MAINACTIVITY")) {
            intent = new Intent(getApplicationContext(), MainActivity.class);
            //AppData.isFinished = false;
            //AppData.isRepeated = false;
            AppData.noticeReceived = true;
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        } else {
            intent = new Intent(this, MainActivity.class);
            //AppData.isFinished = false;
            //AppData.isRepeated = false;
            AppData.noticeReceived = true;
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        }


//        Intent intent = new Intent(this, MainActivity.class);
//        //intent.putExtra("TabNumber", 2);
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        intent.setAction("OPEN_TAB_2");
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
                .setContentTitle("Firebase Cloud Messaging")
                .setPriority(Notification.PRIORITY_MAX) // this is deprecated in API 26 but we can still use for below 26.
                .setContentText(messageBody)
                .setContentTitle("Default notification")
                .setContentText("Lorem ipsum dolor sit amet, consectetur adipiscing elit.")
                .setContentInfo("Info")
                .setAutoCancel(true)
                .setSound(notificationSound)
                .setContentIntent(pendingIntent);

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0 /*ID of notification*/, notifiBuilder.build());


    }


}
