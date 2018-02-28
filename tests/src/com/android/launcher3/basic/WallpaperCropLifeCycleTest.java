package com.android.launcher3;

import android.app.Activity;
import android.app.Instrumentation;
import android.app.Instrumentation.ActivityMonitor;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.test.ActivityUnitTestCase;
import android.util.Log;

import com.android.launcher3.WallpaperCropActivity;
import com.android.launcher3.R;

public class WallpaperCropLifeCycleTest extends ActivityUnitTestCase<WallpaperCropActivity> {
    private static final String TAG = "WallpaperCropLifeCycleTest";
    private static int WAIT_PERIOD = 1000;

    private Instrumentation mInst;
    private Activity mActivity;
    private Context mContext;

    public WallpaperCropLifeCycleTest() {
        super(WallpaperCropActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mInst = getInstrumentation();
        mContext = mInst.getTargetContext();
    }

    @Override
    protected void tearDown() throws Exception {
        if (mActivity != null) {
            mActivity.finish();
        }
        super.tearDown();
    }

    public void test01_ActivityStartTest() {
        Log.d(TAG, "test01_ActivityStartTest");

        Intent intent = new Intent(mInst.getTargetContext(), WallpaperCropActivity.class);
        ActivityMonitor am = new ActivityMonitor(WallpaperCropActivity.class.getName(), null,
                false);
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
        Log.d(TAG, "test02_ActivityStartTest");

        Intent intent = new Intent(mInst.getTargetContext(), WallpaperCropActivity.class);
        WallpaperCropActivity wallPaperActivity = startActivity(intent, null, null);

        mInst.callActivityOnStart(wallPaperActivity);
        LauncherUtils.shortWaiting();
    }

    public void test03_ActivityResumeTest() {
        Log.d(TAG, "test03_ActivityResumeTest");

        Intent intent = new Intent(mInst.getTargetContext(), WallpaperCropActivity.class);
        WallpaperCropActivity wallPaperActivity = startActivity(intent, null, null);

        mInst.callActivityOnResume(wallPaperActivity);
        LauncherUtils.shortWaiting();
    }

    public void test04_ActivityPauseTest() {
        Log.d(TAG, "test04_ActivityPauseTest");

        Intent intent = new Intent(mInst.getTargetContext(), WallpaperCropActivity.class);
        WallpaperCropActivity wallPaperActivity = startActivity(intent, null, null);

        mInst.callActivityOnPause(wallPaperActivity);
        LauncherUtils.shortWaiting();
    }

    public void test05_ActivityStopTest() {
        Log.d(TAG, "test05_ActivityStopTest");

        Intent intent = new Intent(mInst.getTargetContext(), WallpaperCropActivity.class);
        WallpaperCropActivity wallPaperActivity = startActivity(intent, null, null);

        mInst.callActivityOnStop(wallPaperActivity);
        LauncherUtils.shortWaiting();
    }
}
