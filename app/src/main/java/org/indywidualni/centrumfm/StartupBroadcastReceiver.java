package org.indywidualni.centrumfm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.indywidualni.centrumfm.util.AlarmHelper;

public class StartupBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        final String ACTION_BOOT = "android.intent.action.BOOT_COMPLETED";
        final String ACTION_QUICK_BOOT = "android.intent.action.QUICKBOOT_POWERON";
        final String ACTION_REPLACED = "android.intent.action.PACKAGE_REPLACED";

        String action = intent.getAction();

        if (ACTION_BOOT.equals(action) || ACTION_QUICK_BOOT.equals(action)
                || ACTION_REPLACED.equals(action)) {
            Log.i("StartupBroadcast", "Boot time or package replaced!");

            // set all reminders
            if (AlarmHelper.isEnabled())
                AlarmHelper.setAllAlarms();
        }
    }

}