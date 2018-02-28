package com.android.launcher3;

public class LauncherUtils {
    private static int WAIT_PERIOD_SHORT = 2000;
    private static int WAIT_PERIOD_MEDIUM = 4000;

    public static void shortWaiting() {
        try {
            Thread.sleep(WAIT_PERIOD_SHORT);
        } catch (InterruptedException exception) {
            // ignore it
        }
    }

    public static void longWaiting() {
        try {
            Thread.sleep(WAIT_PERIOD_MEDIUM);
        } catch (InterruptedException exception) {
            // ignore it
        }
    }
}
