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

    public static final String LOG_TAG = AuthActivity.class.getName();
    /**
     * URL for assets data from the Lockey Server
     */

    private ConnectivityManager connectivityManager;

    private LoaderManager loaderManager;

    private  NetworkInfo activeNetwork;


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
                UserData.pwd = passwordView.getText().toString();
                UserData.usr = loginView.getText().toString();
                getToken();
            }
        });

        getToken();


    }

    public void getToken(){
        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        activeNetwork = connectivityManager.getActiveNetworkInfo();
        if (activeNetwork != null && activeNetwork.isConnected()) {
            loaderManager = getLoaderManager();
            loaderManager.initLoader(UserData.AUTH_LOADER_ID, null, this);
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
        return new TokenLoader(this, UserData.AUTH_REQUEST_URL, UserData.pwd, UserData.usr);
    }

    @Override
    public void onLoadFinished(Loader<String> loader, String message) {
        loaderManager.destroyLoader(2);
        if (!message.equals("OK")) {
            return;
        }
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

}
