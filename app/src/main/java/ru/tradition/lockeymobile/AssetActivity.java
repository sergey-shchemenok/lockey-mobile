package ru.tradition.lockeymobile;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Spinner;
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
    private Button sendCommand;

    private AssetsData assetData;

    //for spinner
    private Spinner mCommandSpinner;
    //Переменные для спинера
    private final static int CHOOSE_COMMAND = 0;
    private final static int COMMAND_LOCK_ENGINE = 1;
    private final static int COMMAND_UNLOCK_ENGINE = 2;
    private static int mCommand;

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
        sendCommand = (Button) findViewById(R.id.activity_asset_send_command);

        mCommandSpinner = (Spinner) findViewById(R.id.spinner_command);
        setupSpinner();
        mCommandSpinner.getBackground().setColorFilter(getResources().getColor(R.color.checkboxColor), PorterDuff.Mode.SRC_ATOP);

        assetData = (AssetsData) getIntent().getSerializableExtra("AssetData");

        if (assetData != null) {
            kitNumber.setText(String.valueOf(assetData.getId()));
            regNumber.setText(assetData.getRegNumber());
            carModel.setText(assetData.getModel());
            name.setText(assetData.getName());
            //lastTime.setText(String.valueOf(assetData.getLastSignalTime()));
        }
        toMapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AssetActivity.this, MainActivity.class);
                if (assetData != null) {
                    intent.putExtra("latitude", assetData.getLatitude());
                    intent.putExtra("longitude", assetData.getLongitude());
                }
                startActivity(intent);
            }
        });

        sendCommand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showUserAgreement();
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
    protected void onStart() {
        super.onStart();
        Log.i("AssetActivity", "starting asset activity...");
    }


    /**
     * Setup the dropdown spinner that allows the user to select the command.
     */
    private void setupSpinner() {
        // Create adapter for spinner. The list options are from the String array it will use
        // the spinner will use the default layout
        ArrayAdapter commandSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_command_options, android.R.layout.simple_spinner_item);

        // Specify dropdown layout style - simple list view with 1 item per line
        commandSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        // Apply the adapter to the spinner
        mCommandSpinner.setAdapter(commandSpinnerAdapter);

        // Set the integer mSelected to the constant values
        mCommandSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.lock_engine))) {
                        mCommand = COMMAND_LOCK_ENGINE; // Lock
                    } else if (selection.equals(getString(R.string.unlock_engine))) {
                        mCommand = COMMAND_UNLOCK_ENGINE; // Unlock
                    } else {
                        mCommand = CHOOSE_COMMAND;
                    }
                }
            }

            // Because AdapterView is an abstract class, onNothingSelected must be defined
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mCommand = CHOOSE_COMMAND; // Unknown
            }
        });
    }

    int a = 5;

    public void showUserAgreement() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        View mView = getLayoutInflater().inflate(R.layout.check_box, null);
        CheckBox mCheckBox = mView.findViewById(R.id.checkBox);

        builder.setTitle("Пользовательское соглашение")
//                .setMessage(R.string.user_agreement_text)
                .setView(mView)
                .setPositiveButton(R.string.accept, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        // User clicked the "Delete" button, so delete the notifications.
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked the "Cancel" button, so dismiss the dialog
                        // and continue editing the pet.
                        if (dialog != null) {
                            dialog.dismiss();
                        }
                    }
                });

        final AlertDialog alertDialog = builder.create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button button = ((AlertDialog) alertDialog).getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        // TODO Do something
                        a--;

                        if (a == 0)
                            //Dismiss once everything is OK.
                            alertDialog.dismiss();
                    }
                });
            }
        });


        alertDialog.show();


        mCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (compoundButton.isChecked()) {
//                    storeDialogStatus(true);
                } else {
//                    storeDialogStatus(false);
                }
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
                settingsIntent.putExtra("activity", "AssetActivity");
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
