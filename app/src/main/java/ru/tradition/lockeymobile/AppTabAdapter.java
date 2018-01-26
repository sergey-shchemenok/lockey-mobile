package ru.tradition.lockeymobile;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import ru.tradition.lockeymobile.obtainingassets.AssetsFragment;

/**
 * Created by Caelestis on 25.01.2018.
 */

public class AppTabAdapter extends FragmentPagerAdapter {

    /** Context of the app */
    private Context mContext;

    /**
     * Create a new {@link AppTabAdapter} object.
     *
     * @param fm is the fragment manager that will keep each fragment's state in the adapter
     *           across swipes.
     */
    public AppTabAdapter(Context context, FragmentManager fm) {
        super(fm);
        mContext = context;
    }

    /**
     * Return the {@link Fragment} that should be displayed for the given page number.
     */
    @Override
    public Fragment getItem(int position) {
        if (position == 0) {
            return new AssetsFragment();
        } else if (position == 1) {
            return new MapFragmentActivity();
        } else {
            return new OtherFragment();
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
        } else  {
            return mContext.getString(R.string.tab_other);//todo add tab here
        }
    }


}
