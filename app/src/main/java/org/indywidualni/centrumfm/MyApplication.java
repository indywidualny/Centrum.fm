package org.indywidualni.centrumfm;

import android.app.Application;
import android.content.Context;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

public class MyApplication extends Application {

    private static Context mContext;
    private Tracker mTracker;

    public static Context getContextOfApplication() {
        return mContext;
    }

    @Override
    public void onCreate() {
        mContext = getApplicationContext();
        super.onCreate();
    }

    /**
     * Gets the default {@link Tracker} for this {@link Application}.
     *
     * @return tracker
     */
    synchronized public Tracker getDefaultTracker() {
        if (mTracker == null) {
            // To enable debug logging use: adb shell setprop log.tag.GAv4 DEBUG
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            analytics.setDryRun(BuildConfig.DEBUG);

            mTracker = analytics.newTracker(R.xml.global_tracker);
            mTracker.enableAutoActivityTracking(true);
            mTracker.enableExceptionReporting(true);
        }
        return mTracker;
    }

}
