package org.indywidualni.centrumfm;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;

import org.indywidualni.centrumfm.activity.MainActivity;
import org.indywidualni.centrumfm.util.Connectivity;

import java.io.IOException;

public class StreamService extends Service implements MediaPlayer.OnPreparedListener,
        MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener,
        AudioManager.OnAudioFocusChangeListener {

    public static final String ACTION_PAUSE = "action_pause";
    public static final String ACTION_STOP = "action_stop";
    public static final String ACTION_RESUME = "action_resume";
    private static final String ACTION_NOISY = "android.media.AUDIO_BECOMING_NOISY";
    private static final int NOTIFICATION_ID = 999;
    
    public static boolean shouldServiceStopSoon = true;
    private static String currentUrl;
    private static int reportedErrors;
    
    private final IBinder mBinder = new LocalBinder();
    private MediaPlayer mMediaPlayer;
    private WifiManager.WifiLock wifiLock;
    private AudioManager audioManager;
    private IntentFilter intentFilter;
    private NoisyReceiver noisyReceiver;
    private boolean isReceiverRegistered;

    @Override
    public void onCreate() {
        super.onCreate();
        wifiLock = ((WifiManager) getSystemService(Context.WIFI_SERVICE))
                .createWifiLock(WifiManager.WIFI_MODE_FULL, "StreamService");
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        intentFilter = new IntentFilter(ACTION_NOISY);
        noisyReceiver = new NoisyReceiver();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // control media player with intents
        if (intent != null) {
            String action = intent.getAction();
            if (ACTION_PAUSE.equals(action)) {
                pausePlayer();
                startForeground(NOTIFICATION_ID, buildNotification(true));
            } else if (ACTION_RESUME.equals(action)) {
                resumePlayer();
                startForeground(NOTIFICATION_ID, buildNotification(false));
            } else if (ACTION_STOP.equals(action)) {
                stopPlayer();
                if (shouldServiceStopSoon)
                    stopSelf();
            }
        }

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onPrepared(MediaPlayer player) {
        player.start();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        stopPlayer();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        stopPlayer();
        // try to restart the player
        if (currentUrl != null && Connectivity.isConnected(this)) {
            Log.e("StreamService", "An error occurred, trying to restart the player");
            // report this error to the tracker
            if (reportedErrors < 3) {
                ((MyApplication) getApplication()).getDefaultTracker()
                        .send(new HitBuilders.EventBuilder()
                                .setCategory("Playback error " + what + " " + extra)
                                .setAction("Restart player")
                                .setLabel("error playback")
                                .build());
                reportedErrors++;
            }
            // reinitialize the player
            playUrl(currentUrl);
            foregroundStart();
        }
        return true;
    }

    @Override
    public void onLowMemory() {
        // stay alive, please
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopPlayer();
        Log.i("StreamService", "Service is gonna stop now");
    }

    public void onAudioFocusChange(int focusChange) {
        Log.v("onAudioFocusChange", "Value: " + focusChange);
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
                // resume playback
                if (mMediaPlayer == null)
                    break;
                if (!mMediaPlayer.isPlaying())
                    resumePlayer();
                mMediaPlayer.setVolume(1.0f, 1.0f);
                break;

            case AudioManager.AUDIOFOCUS_LOSS:
                // Lost focus for an unbounded amount of time: stop playback and release player
                // But maybe it's a better idea to just pause and let user decide
                startForeground(NOTIFICATION_ID, buildNotification(true));
                pausePlayer();
                break;

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                // Lost focus for a short time, but we have to stop
                // playback. We don't release the media player because playback
                // is likely to resume
                startForeground(NOTIFICATION_ID, buildNotification(true));
                pausePlayer();
                break;

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                // Lost focus for a short time, but it's ok to keep playing
                // at an attenuated level
                if (isPlaying())
                    mMediaPlayer.setVolume(0.1f, 0.1f);
                break;
        }
    }

    private void initMediaPlayer(String url) {
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnCompletionListener(this);
        mMediaPlayer.setOnErrorListener(this);
        mMediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);

        try {
            mMediaPlayer.setDataSource(url);
            if (!isReceiverRegistered) {
                registerReceiver(noisyReceiver, intentFilter);
                isReceiverRegistered = true;
            }
            lockWifi();
        } catch (IllegalArgumentException | IllegalStateException | IOException e) {
            e.printStackTrace();
        }

        try {
            mMediaPlayer.prepareAsync();
            updateWidget(true);
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    public void playUrl(String url) {
        stopPlayer();
        int result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN);

        switch (result) {
            case AudioManager.AUDIOFOCUS_REQUEST_GRANTED:
                currentUrl = url;
                initMediaPlayer(url);
                break;
            case AudioManager.AUDIOFOCUS_REQUEST_FAILED:
                Log.e("playUrl", "Cannot get audio focus");
                focusProblemToast();
                break;
        }
    }

    public void stopPlayer() {
        if (mMediaPlayer != null)
            mMediaPlayer.release();
        mMediaPlayer = null;
        if (isReceiverRegistered) {
            unregisterReceiver(noisyReceiver);
            isReceiverRegistered = false;
        }
        audioManager.abandonAudioFocus(this);
        unlockWifi();
        stopForeground(true);
        updateWidget(false);
    }

    private void pausePlayer() {
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
            unlockWifi();
        }
    }

    private void resumePlayer() {
        if (mMediaPlayer != null && !mMediaPlayer.isPlaying()) {
            mMediaPlayer.start();
            lockWifi();
        }
    }

    public void pauseResumePlayer() {
        stopForeground(true);
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
            unlockWifi();
        } else if (mMediaPlayer != null) {
            mMediaPlayer.start();
            lockWifi();
        }
    }

    public boolean isPlaying() {
        try {
            return mMediaPlayer != null && mMediaPlayer.isPlaying();
        } catch (IllegalStateException e) {  // just in case
            e.printStackTrace();
            return false;
        }
    }

    private void lockWifi() {
        if (!wifiLock.isHeld())
            wifiLock.acquire();
    }

    private void unlockWifi() {
        if (wifiLock.isHeld())
            wifiLock.release();
    }

    private void updateWidget(boolean isPlaying) {
        RemoteViews views = new RemoteViews(getPackageName(), R.layout.widget_quick_play);
        if (isPlaying)
            views.setImageViewResource(R.id.button, R.drawable.ic_stop_white_48dp);
        else
            views.setImageViewResource(R.id.button, R.drawable.ic_play_arrow_white_48dp);
        views.setOnClickPendingIntent(R.id.button, WidgetProvider.buildButtonPendingIntent(this));
        WidgetProvider.pushWidgetUpdate(getApplicationContext(), views);
    }

    public int getCurrentPosition() {
        return mMediaPlayer.getCurrentPosition();
    }

    public int getDuration() {
        return mMediaPlayer.getDuration();
    }

    public boolean isMediaPlayerNull() {
        return mMediaPlayer == null;
    }

    public void foregroundStart() {
        startForeground(NOTIFICATION_ID, buildNotification(false));
    }

    public void foregroundStop() {
        stopForeground(true);
    }

    private PendingIntent retrievePlaybackAction(int which) {
        Intent action;
        PendingIntent pendingIntent;
        final ComponentName serviceName = new ComponentName(this, StreamService.class);
        switch (which) {
            case 0:
                action = new Intent(ACTION_PAUSE);
                action.setComponent(serviceName);
                pendingIntent = PendingIntent.getService(this, 0, action, 0);
                return pendingIntent;
            case 1:
                action = new Intent(ACTION_RESUME);
                action.setComponent(serviceName);
                pendingIntent = PendingIntent.getService(this, 1, action, 0);
                return pendingIntent;
            case 2:
                action = new Intent(ACTION_STOP);
                action.setComponent(serviceName);
                pendingIntent = PendingIntent.getService(this, 2, action, 0);
                return pendingIntent;
            default:
                break;
        }
        return null;
    }

    private Notification buildNotification(boolean paused) {
        Intent showTaskIntent = new Intent(getApplicationContext(), MainActivity.class);
        showTaskIntent.setAction(Intent.ACTION_MAIN);
        showTaskIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        showTaskIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(),
                0, showTaskIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = (NotificationCompat.Builder) new NotificationCompat.Builder(this)
                .setShowWhen(false)
                .setStyle(new NotificationCompat.MediaStyle().setShowActionsInCompactView(0, 1))
                .setSmallIcon(R.drawable.ic_radio_white_24dp)
                .setContentIntent(contentIntent)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setContentText(getString(R.string.stream))
                .setContentTitle(getString(R.string.toolbar_default_title));

        builder.setContentInfo(getString(R.string.high_quality));

        if (Build.VERSION.SDK_INT >= 16) {
            Bitmap artwork = BitmapFactory.decodeResource(getResources(), R.drawable.logo_big_artwork);
            builder.setLargeIcon(artwork);
        }

        if (paused)
            builder.addAction(R.drawable.ic_play_arrow_white_24dp, "play", retrievePlaybackAction(1));
        else
            builder.addAction(R.drawable.ic_pause_white_24dp, "pause", retrievePlaybackAction(0));

        builder.addAction(R.drawable.ic_stop_white_24dp, "stop", retrievePlaybackAction(2));

        return builder.build();
    }

    // show a Focus Problem Toast while not being on UI Thread
    private void focusProblemToast() {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), getString(R.string.audio_focus_problem),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    public class LocalBinder extends Binder {
        public StreamService getService() {
            return StreamService.this;
        }
    }

    private class NoisyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_NOISY.equals(action)) {
                startForeground(NOTIFICATION_ID, buildNotification(true));
                pausePlayer();
            }
        }
    }

}
