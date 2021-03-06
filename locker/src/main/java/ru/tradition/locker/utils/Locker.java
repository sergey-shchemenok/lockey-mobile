package ru.tradition.locker.utils;

import java.util.HashSet;


/**
 * Created by Balram Pandey 12/11/16.
 */

public abstract class Locker {
    public static final int ENABLE_PASSLOCK = 0;
    public static final int DISABLE_PASSLOCK = 1;
    public static final int CHANGE_PASSWORD = 2;
    public static final int UNLOCK_PASSWORD = 3;

    public static final String MESSAGE = "message";
    public static final String TYPE = "type";

    public static final int DEFAULT_TIMEOUT = 5000; // 2000ms

    public static boolean shouldBeLocked = true;

    protected int lockTimeOut;
    protected HashSet<String> ignoredActivities;

    public void setTimeout(int timeout) {
        this.lockTimeOut = timeout;
    }

    public Locker() {
        ignoredActivities = new HashSet<String>();
        lockTimeOut = DEFAULT_TIMEOUT;
    }

    public void addIgnoredActivity(Class<?> clazz) {
        String clazzName = clazz.getName();
        this.ignoredActivities.add(clazzName);
    }

    public void removeIgnoredActivity(Class<?> clazz) {
        String clazzName = clazz.getName();
        this.ignoredActivities.remove(clazzName);
    }

    public static void setShouldBeLocked(boolean shouldBeLocked) {
        Locker.shouldBeLocked = shouldBeLocked;
    }

    public abstract void enable();

    public abstract void disable();

    public abstract boolean setPasscode(String passcode);

    public abstract boolean checkPasscode(String passcode);

    public abstract boolean isPasscodeSet();
}
