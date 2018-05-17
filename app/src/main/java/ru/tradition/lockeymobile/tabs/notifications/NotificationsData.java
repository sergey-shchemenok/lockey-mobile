package ru.tradition.lockeymobile.tabs.notifications;

import java.io.Serializable;

/**
 * Created by Caelestis on 26.02.2018.
 */

public class NotificationsData implements Serializable {
    private int id;
    private String title;
    private String body;
    private String sending_time;
    private double latitude;
    private double longitude;
    private String text;


//    public NotificationsData(int id, String title, String body, String sending_time) {
//        this.id = id;
//        this.title = title;
//        this.body = body;
//        this.sending_time = sending_time;
//    }

    public NotificationsData(int id, String title, String body, String sending_time, double latitude, double longitude, String text) {
        this.id = id;
        this.title = title;
        this.body = body;
        this.sending_time = sending_time;
        this.latitude = latitude;
        this.longitude = longitude;
        this.text = text;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getBody() {
        return body;
    }

    public String getSending_time() {
        return sending_time;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getText() {
        return text;
    }
}
