package org.indywidualni.centrumfm;

import android.app.Application;
import android.content.Context;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

@ReportsCrashes(formUri = "",  // will not be used
        mailTo = "koras@indywidualni.org",
        mode = ReportingInteractionMode.TOAST,
        resToastText = R.string.acra_crash_toast
)

public class MyApplication extends Application {

    private static Context mContext;
    private Tracker mTracker;

    @Override
    public void onCreate() {
        mContext = getApplicationContext();
        super.onCreate();

        /**
         * The following line triggers the initialization of ACRA.
         */
        ACRA.init(this);
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
            analytics.setDryRun(false);

            mTracker = analytics.newTracker(R.xml.global_tracker);
            mTracker.enableAutoActivityTracking(true);
            mTracker.enableExceptionReporting(true);
        }
        return mTracker;
    }

}
