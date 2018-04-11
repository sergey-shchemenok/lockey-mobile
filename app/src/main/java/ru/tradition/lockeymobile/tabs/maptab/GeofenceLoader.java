package ru.tradition.lockeymobile.tabs.maptab;

import android.content.AsyncTaskLoader;
import android.content.Context;

import java.util.List;
import java.util.Map;

import ru.tradition.lockeymobile.tabs.assetstab.AssetsData;

/**
 * Created by Caelestis on 11.04.2018.
 */

public class GeofenceLoader extends AsyncTaskLoader<List<GeofencePolygon>> {

    public GeofenceLoader(Context context) {
        super(context);
    }

    @Override
    public List<GeofencePolygon> loadInBackground() {
        return null;
    }
}
