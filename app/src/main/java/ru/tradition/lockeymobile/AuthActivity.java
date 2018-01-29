package ru.tradition.lockeymobile;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.net.HttpURLConnection;

import ru.tradition.lockeymobile.auth.AuthQueryUtils;
import ru.tradition.lockeymobile.auth.TokenLoader;


public class AuthActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<String> {

//    private TextView result;

    private EditText loginView;
    private EditText passwordView;
    private Button loginButton;

    public static final String LOG_TAG = AuthActivity.class.getName();
    /**
     * URL for assets data from the Lockey Server
     */

    private ConnectivityManager connectivityManager;

    private LoaderManager loaderManager;

    private NetworkInfo activeNetwork;

    private TextView infoMessage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        loginView = (EditText) findViewById(R.id.edit_login);
        passwordView = (EditText) findViewById(R.id.edit_password);
        loginButton = (Button) findViewById(R.id.login_button);

        infoMessage = (TextView) findViewById(R.id.main_info_message);
        infoMessage.setVisibility(View.INVISIBLE);

        if (!UserData.usr.isEmpty())
            loginView.setText(UserData.usr);
        if (!UserData.pwd.isEmpty())
            passwordView.setText(UserData.pwd);

        mHandler = new Handler();

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UserData.pwd = passwordView.getText().toString();
                UserData.usr = loginView.getText().toString();
                getToken();
            }
        });

    }

    public void getToken() {
        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        activeNetwork = connectivityManager.getActiveNetworkInfo();
        if (activeNetwork != null && activeNetwork.isConnected()) {
            loaderManager = getLoaderManager();
            loaderManager.initLoader(UserData.AUTH_LOADER_ID, null, this);
            Log.v(LOG_TAG, "initLoader");
        } else {
            infoMessage.setVisibility(View.VISIBLE);
            infoMessage.setText(R.string.no_connection);
        }
    }

    @Override
    public Loader<String> onCreateLoader(int i, Bundle bundle) {
        // Create a new loader for the given URL
        Log.v(LOG_TAG, "onCreateLoader");
        return new TokenLoader(this, UserData.AUTH_REQUEST_URL, UserData.pwd, UserData.usr);
    }

    @Override
    public void onLoadFinished(Loader<String> loader, String message) {
        loaderManager.destroyLoader(2);
        if (!message.equals("OK") && AuthQueryUtils.authUrlResponseCode != HttpURLConnection.HTTP_OK) {
            infoMessage.setVisibility(View.VISIBLE);
            infoMessage.setText(AuthQueryUtils.authUrlResponseMessage);
            return;
        }
        if (!message.equals("OK")) {
            infoMessage.setVisibility(View.VISIBLE);
            infoMessage.setText(R.string.no_credentials);
            return;
        }
        infoMessage.setVisibility(View.INVISIBLE);
        Log.v(LOG_TAG, "onLoadFinished");
//        result = (TextView)findViewById(R.id.result);
//        result.setText(message);
        Intent intent = new Intent(AuthActivity.this, MainActivity.class);
        startActivity(intent);

    }

    @Override
    public void onLoaderReset(Loader<String> loader) {
        Log.v(LOG_TAG, "onLoadReset");

    }

    @Override
    protected void onStart() {
        Log.i(LOG_TAG, "Activity has started..............................");
        super.onStart();
        startRepeatingTask();
    }

    @Override
    protected void onStop() {
        Log.i(LOG_TAG, "Activity has stopped..............................");
        super.onStop();
        stopRepeatingTask();
    }

    //The code for assets updating
    private int mInterval = 5000; // 5 seconds by default, can be changed later
    private Handler mHandler;

    Runnable mStatusChecker = new Runnable() {
        @Override
        public void run() {
            try {
                Log.i(LOG_TAG, "checking network");
                connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                activeNetwork = connectivityManager.getActiveNetworkInfo();
                if (activeNetwork != null && activeNetwork.isConnected()) {
                    infoMessage.setVisibility(View.INVISIBLE);
                    infoMessage.setText("");
                } else {
                    infoMessage.setVisibility(View.VISIBLE);
                    infoMessage.setText(R.string.no_connection);
                }
            } finally {
                // 100% guarantee that this always happens, even if
                // your update method throws an exception
                mHandler.postDelayed(mStatusChecker, mInterval);
            }
        }
    };
    void startRepeatingTask() {
        mStatusChecker.run();
    }
    void stopRepeatingTask() {
        mHandler.removeCallbacks(mStatusChecker);
    }
    //end here
}
