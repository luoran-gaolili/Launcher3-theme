package com.android.launcher3;

import android.app.Activity;
import android.app.Instrumentation;
import android.app.Instrumentation.ActivityMonitor;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.test.ActivityUnitTestCase;
import android.util.Log;

import com.android.launcher3.Launcher;

public class LauncherLifeCycleTest extends ActivityUnitTestCase<Launcher> {
    private static final String TAG = "LauncherLifeCycleTest";
    private static int WAIT_PERIOD = 1000;

    private Instrumentation mInst;
    private Activity mActivity;
    private Launcher mLauncher;

    public LauncherLifeCycleTest() {
        super(Launcher.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mInst = getInstrumentation();

        mLauncher = this.getActivity();
    }

    @Override
    protected void tearDown() throws Exception {
        if (mActivity != null) {
            mActivity.finish();
        }
        super.tearDown();
    }

    public void test01_ActivityLaunchTest() {
        Log.d(TAG, "test01_LauncherActivityTest");
        Intent intent = new Intent(mInst.getTargetContext(), Launcher.class);
        ActivityMonitor am = new ActivityMonitor(Launcher.class.getName(), null, false);
        try {
            mInst.addMonitor(am);
            startActivity(intent, null, null);
            mActivity = am.waitForActivityWithTimeout(WAIT_PERIOD);
            assertNotNull(mActivity);
        } finally {
            mInst.removeMonitor(am);
        }
    }

    public void test02_ActivityStartTest() {
        Intent intent = new Intent(mInst.getTargetContext(), Launcher.class);
        Launcher launcherActivity = startActivity(intent, null, null);

        Log.d(TAG, "test02_ActivityStartTest()");
        mInst.callActivityOnStart(launcherActivity);
        LauncherUtils.shortWaiting();
    }

    public void test03_ActivityResumeTest() {
        Intent intent = new Intent(mInst.getTargetContext(), Launcher.class);
        Launcher launcherActivity = startActivity(intent, null, null);

        Log.d(TAG, "test03_ActivityResumeTest");
        mInst.callActivityOnResume(launcherActivity);
        LauncherUtils.shortWaiting();
    }

    public void test04_ActivityPausTest() {
        Intent intent = new Intent(mInst.getTargetContext(), Launcher.class);
        Launcher launcherActivity = startActivity(intent, null, null);

        Log.d(TAG, "test04_ActivityPausTest");
        mInst.callActivityOnPause(launcherActivity);
        LauncherUtils.shortWaiting();
    }

    public void test05_ActivityStopTest() {
        Intent intent = new Intent(mInst.getTargetContext(), Launcher.class);
        Launcher launcherActivity = startActivity(intent, null, null);

        Log.d(TAG, "test05_ActivityStopTest");
        mInst.callActivityOnStop(launcherActivity);
        LauncherUtils.shortWaiting();
    }
}
