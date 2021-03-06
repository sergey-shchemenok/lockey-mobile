package ru.tradition.lockeymobile.tabs;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;

import ru.tradition.lockeymobile.tabs.maptab.MapFragmentTab;
import ru.tradition.lockeymobile.tabs.maptab.MapFragmentTabOSM;
import ru.tradition.lockeymobile.tabs.notifications.NotificationsFragmentTab;
import ru.tradition.lockeymobile.R;
import ru.tradition.lockeymobile.tabs.assetstab.AssetsFragmentTab;

/**
 * Created by Caelestis on 25.01.2018.
 */

public class AppTabAdapter extends FragmentPagerAdapter {

    private String mUseMap;

    /**
     * Context of the app
     */
    private Context mContext;

    /**
     * Create a new {@link AppTabAdapter} object.
     *
     * @param fm is the fragment manager that will keep each fragment's state in the adapter
     *           across swipes.
     */
    public AppTabAdapter(Context context, FragmentManager fm, String useMap) {
        super(fm);
        mContext = context;
        mUseMap = useMap;
    }

    /**
     * Return the {@link Fragment} that should be displayed for the given page number.
     */
    @Override
    public Fragment getItem(int position) {
        if (position == 0) {
            return new AssetsFragmentTab();
        } else if (position == 1) {
            if (mUseMap.equals(mContext.getString(R.string.settings_google_map_value)))
                return new MapFragmentTab();
            else
                return new MapFragmentTabOSM();
        } else {
            return new NotificationsFragmentTab();
        }
    }

    /**
     * Return the total number of pages.
     */
    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        if (position == 0) {
            return mContext.getString(R.string.tab_assets);
        } else if (position == 1) {
            return mContext.getString(R.string.tab_map);
        } else {
            return mContext.getString(R.string.tab_notice);//todo add tab here
        }
    }


}
