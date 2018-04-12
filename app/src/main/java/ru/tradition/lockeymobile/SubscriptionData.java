package ru.tradition.lockeymobile;

/**
 * Created by Caelestis on 12.04.2018.
 */

class SubscriptionData {
    private int sid;
    private String title;
    private String zoneTitle;
    private String zid;
    private boolean isSubscribed;
    private int[] cars;

    public SubscriptionData(int sid, String title, String zoneTitle, String zid, boolean isSubscribed, int... cars) {
        this.sid = sid;
        this.title = title;
        this.zoneTitle = zoneTitle;
        this.zid = zid;
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

    public String getZid() {
        return zid;
    }

    public boolean isSubscribed() {
        return isSubscribed;
    }

    public int[] getCars() {
        return cars;
    }
}
