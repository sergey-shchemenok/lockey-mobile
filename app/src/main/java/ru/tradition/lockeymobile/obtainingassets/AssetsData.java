package ru.tradition.lockeymobile.obtainingassets;

/**
 * Created by Caelestis on 22.01.2018.
 */

public class AssetsData {
    private int id;
    private String name;
    private String model;
    private String regNumber;
    private int lastSignalTime;


    public AssetsData(int id, String name, String model, String regNumber, int lastSignalTime) {
        this.id = id;
        this.name = name;
        this.model = model;
        this.regNumber = regNumber;
        this.lastSignalTime = lastSignalTime;
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
}
