package org.indywidualni.centrumfm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.indywidualni.centrumfm.util.AlarmHelper;

public class StartupBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("StartupBroadcast", "Boot time or package replaced!");

        // set all reminders
        if (AlarmHelper.isEnabled())
            AlarmHelper.setAllAlarms();
    }

}