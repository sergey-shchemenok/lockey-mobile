package ru.tradition.lockeymobile;

/**
 * Created by Caelestis on 12.04.2018.
 */

public class SubscriptionData {
    private int sid;
    private String title;
    private int zid;
    private String zoneTitle;
    private boolean isSubscribed;
    private int[] cars;

    public SubscriptionData(int sid, String title, int zid, String zoneTitle, boolean isSubscribed, int... cars) {
        this.sid = sid;
        this.title = title;
        this.zid = zid;
        this.zoneTitle = zoneTitle;
        this.isSubscribed = isSubscribed;
        this.cars = cars;
    }

    public int getSid() {
        return sid;
    }

    public String getTitle() {
        return title;
    }

    public String getZoneTitle() {
        return zoneTitle;
    }

    public int getZid() {
        return zid;
    }

    public boolean isSubscribed() {
        return isSubscribed;
    }

    public int[] getCars() {
        return cars;
    }
}
