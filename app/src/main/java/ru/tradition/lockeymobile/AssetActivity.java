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

import ru.tradition.lockeymobile.tabs.assetstab.AssetsData;

public class AssetActivity extends AppCompatActivity {
    private TextView kitNumber;
    private TextView regNumber;
    private TextView carModel;
    private TextView name;
    //just for debugging
    private TextView lastTime;

    private Button commandButton1;
    private Button commandButton2;

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

        setTitle(R.string.asset_activity_title);

        kitNumber = (TextView)findViewById(R.id.asset_kit_number);
        regNumber = (TextView)findViewById(R.id.asset_reg_number);
        carModel = (TextView)findViewById(R.id.asset_car_model);
        name = (TextView)findViewById(R.id.asset_name);
        //lastTime = (TextView)findViewById(R.id.asset_last_time);
        commandButton1 = (Button)findViewById(R.id.activity_asset_command1);
        commandButton2 = (Button)findViewById(R.id.activity_asset_command2);

        AssetsData assetData = (AssetsData) getIntent().getSerializableExtra("AssetData");

        kitNumber.setText(String.valueOf(assetData.getId()));
        regNumber.setText(assetData.getRegNumber());
        carModel.setText(assetData.getModel());
        name.setText(assetData.getName());
        //lastTime.setText(String.valueOf(assetData.getLastSignalTime()));

        commandButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        commandButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

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
                //todo settings here
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void logout() {
        Intent intent = new Intent(this, AuthActivity.class);
        AppData.isFinished = false;
        AppData.isRepeated = false;
        startActivity(intent);
    }

}
