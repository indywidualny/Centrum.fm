package org.indywidualni.centrumfm;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import org.indywidualni.centrumfm.activity.MainActivity;
import org.indywidualni.centrumfm.util.AlarmHelper;
import org.indywidualni.centrumfm.util.WakeLocker;

public class AlarmReceiver extends BroadcastReceiver {

    private static final Context context = MyApplication.getContextOfApplication();

    @Override
    public void onReceive(Context context, Intent intent) {
        WakeLocker.acquire(context);

        Log.v("AlarmReceiver", "New event soon");
        String name = intent.getStringExtra(AlarmHelper.EVENT_TITLE);
        int timeToStart = Integer.parseInt(intent.getStringExtra(AlarmHelper.TIME_TO_START))
                / (60 * 1000);
        notify(name, Math.abs(timeToStart));

        WakeLocker.release();
    }

    @TargetApi(16)
    private void notify(String title, int timeToStart) {
        String soon = String.format(context.getString(R.string.soon), title);
        String eventStarting = String.format(context.getString(R.string.event_starting), timeToStart);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(soon))
                        .setSmallIcon(R.drawable.ic_notification)
                        .setContentText(eventStarting)
                        .setContentTitle(soon)
                        .setTicker(soon)
                        .setWhen(System.currentTimeMillis())
                        .setAutoCancel(true)
                        .setOngoing(false)
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            mBuilder.setPriority(Notification.PRIORITY_HIGH);

        Intent intent = new Intent(context, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(intent);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);

        NotificationManager mNotificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationManager.notify(0, mBuilder.build());
    }

}