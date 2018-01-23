package ru.tradition.lockeymobile;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import ru.tradition.lockeymobile.auth.TokenLoader;


public class AuthActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<String>{

//    private TextView result;

    private EditText loginView;
    private EditText passwordView;
    private Button loginButton;

    private String pwd;
    private String usr;

    private static final int AUTH_LOADER_ID = 2;

    public static final String LOG_TAG = AuthActivity.class.getName();
    /**
     * URL for assets data from the Lockey Server
     */
    private static final String AUTH_REQUEST_URL = "http://my.lockey.ru/LockeyREST/api/Auth";

    private ConnectivityManager connectivityManager;

    private LoaderManager loaderManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        loginView = (EditText)findViewById(R.id.edit_login);
        passwordView = (EditText)findViewById(R.id.edit_password);
        loginButton = (Button)findViewById(R.id.login_button);



        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pwd = passwordView.getText().toString();
                usr = loginView.getText().toString();
                startLoader();
            }
        });

    }

    public void startLoader(){
        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        if (activeNetwork != null && activeNetwork.isConnected()) {
            loaderManager = getLoaderManager();
            loaderManager.initLoader(AUTH_LOADER_ID, null, this);
            Log.v(LOG_TAG, "initLoader");
        } else {
//            progressCircle.setVisibility(View.GONE);
//            mEmptyStateTextView.setText(R.string.no_connection);
        }
    }

    @Override
    public Loader<String> onCreateLoader(int i, Bundle bundle) {
        // Create a new loader for the given URL
        Log.v(LOG_TAG, "onCreateLoader");
        return new TokenLoader(this, AUTH_REQUEST_URL, pwd, usr);
    }

    @Override
    public void onLoadFinished(Loader<String> loader, String message) {
        loaderManager.destroyLoader(2);
        pwd = "";
        usr = "";
        if (!message.equals("OK")) {
            return;
        }
        Log.v(LOG_TAG, "onLoadFinished");
//        result = (TextView)findViewById(R.id.result);
//        result.setText(message);
        Intent intent = new Intent(AuthActivity.this, AssetsActivity.class);
        startActivity(intent);

    }

    @Override
    public void onLoaderReset(Loader<String> loader) {
        Log.v(LOG_TAG, "onLoadReset");

    }

}
