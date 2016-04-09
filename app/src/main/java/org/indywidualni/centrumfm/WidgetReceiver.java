package org.indywidualni.centrumfm;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import org.indywidualni.centrumfm.activity.MainActivity;
import org.indywidualni.centrumfm.util.Connectivity;

public class WidgetReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (WidgetProvider.QUICK_PLAY.equals(intent.getAction()))
            bindStreamService(context);
    }

    private void bindStreamService(Context context) {
        context = context.getApplicationContext();
        Intent intent = new Intent(context, StreamService.class);
        context.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            Log.d("ServiceConnectionWidget", "connected");
            StreamService.LocalBinder binder = (StreamService.LocalBinder) service;
            StreamService streamService = binder.getService();

            Context context = MyApplication.getContextOfApplication();
            Tracker tracker = ((MyApplication) context).getDefaultTracker();
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);

            if (streamService.isPlaying()) {
                streamService.stopPlayer();
                context.unbindService(this);
            } else {
                if (Connectivity.isConnected(context)) {
                    if (Connectivity.isConnectedMobile(context)
                            && !preferences.getBoolean("mobile_streaming", false)) {
                        Toast.makeText(context, context.getString(R.string.mobile_restriction),
                                Toast.LENGTH_SHORT).show();
                    } else {
                        streamService.playUrl(MainActivity.STREAM_URL);
                        streamService.foregroundStart();

                        if (Connectivity.isConnectedMobile(context)) {
                            tracker.send(new HitBuilders.EventBuilder()
                                    .setCategory("Quick Play Radio")
                                    .setAction("Play stream mobile")
                                    .build());
                        } else {
                            tracker.send(new HitBuilders.EventBuilder()
                                    .setCategory("Quick Play Radio")
                                    .setAction("Play stream Wi-Fi")
                                    .build());
                        }
                    }
                } else {
                    Toast.makeText(context, context.getString(R.string.no_network),
                            Toast.LENGTH_SHORT).show();
                }
            }
        }
        @Override
        public void onServiceDisconnected(ComponentName className) {
            Log.d("ServiceConnectionWidget", "disconnected");
        }
    };

}