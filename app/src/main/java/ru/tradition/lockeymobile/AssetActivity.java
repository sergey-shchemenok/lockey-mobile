package ru.tradition.lockeymobile;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import ru.tradition.lockeymobile.obtainingassets.AssetsData;

public class AssetActivity extends AppCompatActivity {
    private TextView kitNumber;
    private TextView regNumber;
    private TextView carModel;
    private TextView name;
    private TextView lastTime;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asset);

        setTitle(R.string.asset_activity_title);

        kitNumber = (TextView)findViewById(R.id.asset_kit_number);
        regNumber = (TextView)findViewById(R.id.asset_reg_number);
        carModel = (TextView)findViewById(R.id.asset_car_model);
        name = (TextView)findViewById(R.id.asset_name);
        //lastTime = (TextView)findViewById(R.id.asset_last_time);

        AssetsData assetData = (AssetsData) getIntent().getSerializableExtra("AssetData");

        kitNumber.setText(String.valueOf(assetData.getId()));
        regNumber.setText(assetData.getRegNumber());
        carModel.setText(assetData.getModel());
        name.setText(assetData.getName());
        //lastTime.setText(String.valueOf(assetData.getLastSignalTime()));

    }
}
