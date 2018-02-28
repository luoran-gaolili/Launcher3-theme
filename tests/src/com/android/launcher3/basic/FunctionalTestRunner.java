package com.android.launcher3;

import android.test.InstrumentationTestRunner;
import com.android.launcher3.model.GridSizeMigrationTaskTest;
import junit.framework.TestSuite;

public class FunctionalTestRunner extends InstrumentationTestRunner {
     @Override
     public TestSuite getAllTests() {
         TestSuite tests = new TestSuite();

          ///M: MTK enhance test.
          tests.addTestSuite(RotationChangeTest.class);
          tests.addTestSuite(LauncherLifeCycleTest.class);
          tests.addTestSuite(WallpaperCropLifeCycleTest.class);

          ///M: ASOP Function test.
          tests.addTestSuite(GridSizeMigrationTaskTest.class);
          tests.addTestSuite(QuickAddWidgetTest.class);
          tests.addTestSuite(RotationPreferenceTest.class);
          tests.addTestSuite(FocusLogicTest.class);
          tests.addTestSuite(InvariantDeviceProfileTest.class);

          return tests;
     }
}
