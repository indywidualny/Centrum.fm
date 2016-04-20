package org.indywidualni.centrumfm;

import android.app.Application;
import android.content.Context;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

public class MyApplication extends Application {

    private static Context mContext;
    private Tracker mTracker;

    @Override
    public void onCreate() {
        mContext = getApplicationContext();
        super.onCreate();
    }

    public static Context getContextOfApplication() {
        return mContext;
    }

    /**
     * Gets the default {@link Tracker} for this {@link Application}.
     * @return tracker
     */
    synchronized public Tracker getDefaultTracker() {
        if (mTracker == null) {
            // To enable debug logging use: adb shell setprop log.tag.GAv4 DEBUG
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            analytics.setDryRun(true);  // TODO: false

            mTracker = analytics.newTracker(R.xml.global_tracker);
            mTracker.enableAutoActivityTracking(true);
            mTracker.enableExceptionReporting(true);
        }
        return mTracker;
    }

}
