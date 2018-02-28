package com.android.launcher3;

import android.app.Instrumentation;
import android.content.pm.ActivityInfo;
import android.os.SystemClock;
import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;

import com.android.launcher3.Launcher;
import com.jayway.android.robotium.solo.Solo;

public class RotationChangeTest extends ActivityInstrumentationTestCase2<Launcher> {
    private static final String TAG = "RotationChangeTest";

    private static final int NUM_ITERATIONS = 2;
    private static final int WAIT_TIME_MS = 2000;

    private Launcher mLauncher;
    private Instrumentation mInst;
    private Solo mSolo;

    public RotationChangeTest() {
        super(Launcher.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mLauncher = this.getActivity();
        mInst = this.getInstrumentation();
        mSolo = new Solo(mInst, mLauncher);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void test01_EnableRotationTest() throws Exception {
        Log.d(TAG, "test01_EnableRotationTest");

        sendKeyDownUpSync(KeyEvent.KEYCODE_MENU);

        View settingsButton = (View) mLauncher.findViewById(R.id.settings_button);
        if (settingsButton != null) {
            mSolo.clickOnView(settingsButton);
            SystemClock.sleep(WAIT_TIME_MS);

            sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_DOWN);
            sendKeyDownUpSync(KeyEvent.KEYCODE_ENTER);
        }
    }

    public void test02_HomeScreenRotationTest() throws Exception {
        Log.d(TAG, "test02_HomeScreenRotationTest");

        mSolo.sleep(WAIT_TIME_MS);
        mInst.waitForIdleSync();

        setRequestedOrientation(
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        setRequestedOrientation(
                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        // Restore.
        setRequestedOrientation(
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    public void test03_AllAppRotationTest() throws Exception {
        Log.d(TAG, "test03_AllAppRotationTest");

        mSolo.sleep(WAIT_TIME_MS);
        mInst.waitForIdleSync();

        // Enter AllApp.
        sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_LEFT);
        sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_LEFT);
        sendKeyDownUpSync(KeyEvent.KEYCODE_ENTER);

        setRequestedOrientation(
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        setRequestedOrientation(
                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        // Restore.
        setRequestedOrientation(
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    public void test04_WidgetRotationStress() throws Exception {
        Log.d(TAG, "test04_WidgetRotationStress");

        sendKeyDownUpSync(KeyEvent.KEYCODE_MENU);
        SystemClock.sleep(WAIT_TIME_MS);

        // Widget page.
        View wigdetButton = (View) mLauncher.findViewById(R.id.widget_button);
        if (wigdetButton != null) {
           mSolo.clickOnView(wigdetButton);
           SystemClock.sleep(WAIT_TIME_MS);

           setRequestedOrientation(
                  ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

           setRequestedOrientation(
                  ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

           // Restore.
           setRequestedOrientation(
                  ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    public void test05_WallpaperRotationStress() throws Exception {
        Log.d(TAG, "test05_WallpaperRotationStress");

        sendKeyDownUpSync(KeyEvent.KEYCODE_MENU);
        SystemClock.sleep(WAIT_TIME_MS);

        // Rotation page.
        View wallpaperButton = (View) mLauncher.findViewById(R.id.wallpaper_button);
        if(wallpaperButton != null) {
           mSolo.clickOnView(wallpaperButton);
           SystemClock.sleep(WAIT_TIME_MS);

           setRequestedOrientation(
                  ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

           setRequestedOrientation(
                  ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

            // Restore.
            setRequestedOrientation(
                  ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    private void sendKeyDownUpSync(int keycode) {
        mInst.sendKeyDownUpSync(keycode);
        mSolo.sleep(WAIT_TIME_MS);
        mInst.waitForIdleSync();
    }

    private void setRequestedOrientation(int orientation) {
        mLauncher.setRequestedOrientation(orientation);
        mSolo.sleep(WAIT_TIME_MS);
        mInst.waitForIdleSync();
    }
}
