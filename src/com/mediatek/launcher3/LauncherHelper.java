package com.mediatek.launcher3;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Trace;
import android.util.Log;

import java.lang.reflect.Method;

public class LauncherHelper {
    private static final String TAG = "LauncherHelper";

    ///M: Hide in Trace.java
    public static final long TRACE_TAG_INPUT = 1L << 2;

    public static final String ACTION_UNREAD_CHANGED =
                     "com.mediatek.action.UNREAD_CHANGED";
    public static final String EXTRA_UNREAD_COMPONENT =
                     "com.mediatek.intent.extra.UNREAD_COMPONENT";
    public static final String EXTRA_UNREAD_NUMBER =
                     "com.mediatek.intent.extra.UNREAD_NUMBER";

    ///M: Plase enable debug tag during performance debuging.
    private static Boolean DEBUG = LauncherLog.DEBUG_PERFORMANCE;

    public static ComponentName getExtra(Intent intent, String name) {
        ComponentName component = null;
        try {
             Method getExtra = intent.getClass().getMethod("getExtra");
             component = (ComponentName) getExtra.invoke(name);
        } catch (Exception ignored) {
             Log.e(TAG, "getExtra() reflect fail");
        }
        return component;
    }

    public static void traceCounter(long traceTag,
                     String counterName,int counterValue) {
        try {
             Method traceCounter = Trace.class.getMethod("traceCounter");
             traceCounter.invoke(traceTag, counterName, counterValue);
        } catch (Exception ignored) {
             Log.e(TAG, "traceCounter() reflect fail");
        }
    }

    public static void beginSection(String tag) {
        if (DEBUG) {
            Trace.beginSection(tag);
        }
    }

    public static void endSection() {
        if (DEBUG) {
            Trace.endSection();
        }
    }
}
