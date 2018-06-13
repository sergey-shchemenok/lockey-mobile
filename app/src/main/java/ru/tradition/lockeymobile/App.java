package ru.tradition.lockeymobile;

import android.app.Application;

import ru.tradition.locker.view.AppLocker;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        AppLocker.getInstance().enableAppLock(this);
    }
}