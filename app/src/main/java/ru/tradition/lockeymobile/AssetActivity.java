package ru.tradition.lockeymobile;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import ru.tradition.lockeymobile.tabs.assetstab.AssetsData;

public class AssetActivity extends AppCompatActivity {
    private TextView kitNumber;
    private TextView regNumber;
    private TextView carModel;
    private TextView name;

    private Button toMapButton;
    private Button blockEngineButton;

    private AssetsData assetData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asset);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //to add up button
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);

        }

        toolbar.setTitle(R.string.asset_activity_title);

        kitNumber = (TextView) findViewById(R.id.asset_kit_number);
        regNumber = (TextView) findViewById(R.id.asset_reg_number);
        carModel = (TextView) findViewById(R.id.asset_car_model);
        name = (TextView) findViewById(R.id.asset_name);
        //lastTime = (TextView)findViewById(R.id.asset_last_time);
        toMapButton = (Button) findViewById(R.id.activity_asset_to_map);
        blockEngineButton = (Button) findViewById(R.id.activity_asset_block_engine);

        assetData = (AssetsData) getIntent().getSerializableExtra("AssetData");

        kitNumber.setText(String.valueOf(assetData.getId()));
        regNumber.setText(assetData.getRegNumber());
        carModel.setText(assetData.getModel());
        name.setText(assetData.getName());
        //lastTime.setText(String.valueOf(assetData.getLastSignalTime()));

        toMapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AssetActivity.this, MainActivity.class);
                intent.putExtra("latitude", assetData.getLatitude());
                intent.putExtra("longitude", assetData.getLongitude());
                startActivity(intent);
            }
        });

        blockEngineButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(AssetActivity.this, "Функция блокировки недоступна в текущей версии приложения", Toast.LENGTH_LONG).show();
            }
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_asset, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            case R.id.asset_menu_logout:
                logout();
                return true;
            case R.id.asset_menu_settings:
                Intent settingsIntent = new Intent(this, SettingsActivity.class);
                startActivity(settingsIntent);
                return true;
            case R.id.asset_menu_subscriptions:
                Intent subscriptionsIntent = new Intent(this, SubscriptionsActivity.class);
                startActivity(subscriptionsIntent);
                return true;

            case R.id.asset_menu_about_program:
                MainActivity.showAboutTheProgram(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void logout() {
        Intent intent = new Intent(this, AuthActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onSupportNavigateUp() {
        super.onBackPressed();
        return true;
    }


}
