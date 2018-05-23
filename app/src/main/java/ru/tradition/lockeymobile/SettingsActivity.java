package ru.tradition.lockeymobile;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;


public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //to add up button
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.arrow_back);
        }

        toolbar.setTitle(R.string.settings_activity_title);


        getFragmentManager().beginTransaction()
                .replace(R.id.settings_container, new LockeyMobilePreferenceFragment()).commit();


    }

    public static class LockeyMobilePreferenceFragment extends PreferenceFragment
            implements Preference.OnPreferenceChangeListener {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.settings);

//            Preference allowNotification = findPreference(getString(R.string.settings_allow_notifications_key));
//            bindPreferenceSummaryToValue(allowNotification);

            Preference orderBy = findPreference(getString(R.string.settings_order_by_key));
            bindPreferenceSummaryToValue(orderBy);

            Preference useMap = findPreference(getString(R.string.settings_use_map_key));
            bindPreferenceSummaryToValue(useMap);

            Preference notificationsOrderBy = findPreference(getString(R.string.settings_notifications_order_by_key));
            bindPreferenceSummaryToValue(notificationsOrderBy);

        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();
            if (preference instanceof CheckBoxPreference) {
            } else if (preference instanceof ListPreference) {
                ListPreference listPreference = (ListPreference) preference;
                int prefIndex = listPreference.findIndexOfValue(stringValue);
                if (prefIndex >= 0) {
                    CharSequence[] labels = listPreference.getEntries();
                    preference.setSummary(labels[prefIndex]);
                }
            }
            return true;
        }

        private void bindPreferenceSummaryToValue(Preference preference) {
            preference.setOnPreferenceChangeListener(this);
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(preference.getContext());
//            if (preference instanceof CheckBoxPreference) {
//                boolean preferenceBoolean = preferences.getBoolean(preference.getKey(), false);
//                onPreferenceChange(preference, preferenceBoolean);
//            }  else
            if (preference instanceof ListPreference) {
                String preferenceString = preferences.getString(preference.getKey(), "");
                onPreferenceChange(preference, preferenceString);
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
//        super.onBackPressed();
        toPreviousActivityAndSave();
        return true;
    }


    @Override
    public void onBackPressed() {
        onSupportNavigateUp();
    }

    private void toPreviousActivityAndSave() {
        Bundle bundle = getIntent().getExtras();
        if (bundle.containsKey("activity")) {
            String activity = bundle.getString("activity");
            if (activity.equals("MainActivity") && bundle.containsKey("currentPage")) {
                int page = bundle.getInt("currentPage");
                Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
                intent.putExtra("page", page);
                startActivity(intent);
            } else {
                super.onSupportNavigateUp();
            }
//            else if (activity.equals("SubscriptionsActivity")) {
//                Intent intent = new Intent(SettingsActivity.this, SubscriptionsActivity.class);
//                startActivity(intent);
//            } else if (activity.equals("NotificationActivity")) {
//                Intent intent = new Intent(SettingsActivity.this, NotificationActivity.class);
//                startActivity(intent);
//            } else if (activity.equals("AssetActivity")) {
//                Intent intent = new Intent(SettingsActivity.this, AssetActivity.class);
//                startActivity(intent);
//            }

        }
    }

}
