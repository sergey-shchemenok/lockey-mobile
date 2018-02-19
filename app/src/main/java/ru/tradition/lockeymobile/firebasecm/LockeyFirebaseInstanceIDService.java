package ru.tradition.lockeymobile.firebasecm;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * Created by NgocTri on 8/9/2016.
 */
public class LockeyFirebaseInstanceIDService extends FirebaseInstanceIdService {

    private static final String LOG_TAG = LockeyFirebaseInstanceIDService.class.getSimpleName();

    @Override
    public void onTokenRefresh() {
        //Get updated token
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(LOG_TAG, "New Token: " + refreshedToken);

        //You can save the token into third party server to do anything you want
    }
}
