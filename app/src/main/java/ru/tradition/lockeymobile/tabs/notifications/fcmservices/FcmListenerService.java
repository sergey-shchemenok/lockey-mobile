package ru.tradition.lockeymobile.tabs.notifications.fcmservices;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

/**
 * Created by Caelestis on 02.04.2018.
 */

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class FcmListenerService extends NotificationListenerService {
    private String TAG = this.getClass().getSimpleName();
    private FcmListenerServiceReceiver fcmListenerServiceReceiver;
    @Override
    public void onCreate() {
        super.onCreate();
        fcmListenerServiceReceiver = new FcmListenerServiceReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("ru.tradition.lockeymobile.tabs.notifications.fcmservices.NOTIFICATIONACTIVITY");
        registerReceiver(fcmListenerServiceReceiver,filter);
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(fcmListenerServiceReceiver);
    }
    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        Log.i(TAG,"**********  onNotificationPosted");
        Log.i(TAG,"ID :" + sbn.getId() + "t" + sbn.getNotification().tickerText + "t" + sbn.getPackageName());
        Intent i = new  Intent("ru.tradition.lockeymobile.tabs.notifications.fcmservices.NOTIFICATIONACTIVITY");
        i.putExtra("notification</em>event","onNotificationPosted :" + sbn.getPackageName() + "n");
        sendBroadcast(i);
    }
    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        Log.i(TAG,"********** onNOtificationRemoved");
        Log.i(TAG,"ID :" + sbn.getId() + "t" + sbn.getNotification().tickerText +"t" + sbn.getPackageName());
        Intent i = new  Intent("ru.tradition.lockeymobile.tabs.notifications.fcmservices.NOTIFICATIONACTIVITY");
        i.putExtra("notification<em>event","onNotificationRemoved :" + sbn.getPackageName() + "n");
        sendBroadcast(i);
    }
    class FcmListenerServiceReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getStringExtra("command").equals("clearall")){
                FcmListenerService.this.cancelAllNotifications();
            }
            else if(intent.getStringExtra("command").equals("list")){
                Intent i1 = new  Intent("ru.tradition.lockeymobile.tabs.notifications.fcmservices.NOTIFICATIONACTIVITY");
                i1.putExtra("notification</em>event","=====================");
                sendBroadcast(i1);
                int i=1;
                for (StatusBarNotification sbn : FcmListenerService.this.getActiveNotifications()) {
                    Intent i2 = new  Intent("ru.tradition.lockeymobile.tabs.notifications.fcmservices.NOTIFICATIONACTIVITY");
                    i2.putExtra("notification<em>event",i +" " + sbn.getPackageName() + "n");
                    sendBroadcast(i2);
                    i++;
                }
                Intent i3 = new  Intent("ru.tradition.lockeymobile.tabs.notifications.fcmservices.NOTIFICATIONACTIVITY");
                i3.putExtra("notification</em>event","===== Notification List ====");
                sendBroadcast(i3);
            }
        }
    }
}
