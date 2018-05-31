package ru.tradition.lockeymobile.tabs.assetstab;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Created by Caelestis on 22.01.2018.
 */

public class AssetsData implements Serializable {
    private int CID;
    private int id;
    private String name;
    private String model;
    private String regNumber;
    private int lastSignalTime;//time since the last position signal in minutes
    private double latitude;
    private double longitude;

    public AssetsData(int CID, int id, String name, String model, String regNumber, int lastSignalTime, double latitude, double longitude) {
        this.CID = CID;
        this.id = id;
        this.name = name;
        this.model = model;
        this.regNumber = regNumber;
        this.lastSignalTime = lastSignalTime;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public int getCID() {
        return CID;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getModel() {
        return model;
    }

    public String getRegNumber() {
        return regNumber;
    }

    public int getLastSignalTime() {
        return lastSignalTime;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }


    public static final Comparator<AssetsData> COMPARE_BY_LAST_SIGNAL_TIME = new Comparator<AssetsData>() {
        @Override
        public int compare(AssetsData o1, AssetsData o2) {
            return o1.getLastSignalTime() - o2.getLastSignalTime();
        }
    };

}
