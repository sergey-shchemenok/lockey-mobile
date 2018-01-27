package ru.tradition.lockeymobile;

import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

import java.util.List;

import ru.tradition.lockeymobile.obtainingassets.AssetsData;

/**
 * Created by Caelestis on 28.01.2018.
 */

public class UserData {
    //Here is the data from Loader about cars and other assets
    //todo move it into User class
    public static List<AssetsData> mAssetData;
    //To save camera position in case of restarting app
    public static CameraPosition target = CameraPosition.builder()
            .target(new LatLng(55.7522200, 37.6155600))
            .zoom(10)
            .build();


    //todo Here is the data from Loader about cars and other assets for API lesser 26
    public static List<AssetsData> mAssetDataAPIBefore26;
}
