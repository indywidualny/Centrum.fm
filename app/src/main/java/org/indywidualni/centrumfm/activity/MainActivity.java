package org.indywidualni.centrumfm.activity;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PersistableBundle;
import android.preference.PreferenceManager;
import android.support.annotation.IdRes;
import android.support.customtabs.CustomTabsIntent;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import org.indywidualni.centrumfm.MyApplication;
import org.indywidualni.centrumfm.R;
import org.indywidualni.centrumfm.StreamService;
import org.indywidualni.centrumfm.fragment.FavouriteFragment;
import org.indywidualni.centrumfm.fragment.MainFragment;
import org.indywidualni.centrumfm.fragment.ScheduleFragment;
import org.indywidualni.centrumfm.rest.RestClient;
import org.indywidualni.centrumfm.rest.model.RDS;
import org.indywidualni.centrumfm.util.ChangeLog;
import org.indywidualni.centrumfm.util.Connectivity;
import org.indywidualni.centrumfm.util.customtabs.CustomTabActivityHelper;
import org.indywidualni.centrumfm.util.ui.MarqueeToolbar;
import org.indywidualni.centrumfm.util.ui.ScrollAwareFABBehavior;
import org.indywidualni.centrumfm.util.ui.SlidingTabLayout;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@SuppressWarnings("UnusedDeclaration")
public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        CustomTabActivityHelper.ConnectionCallback {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String WEBSITE_URL = "http://centrum.fm";
    private static final String WEBSITE_PEOPLE = "http://centrum.fm/dyzury/";
    public static final String STREAM_URL = "http://5.201.13.191:80/live";
    private static final String APP_PLAY_URL = "https://play.google.com/store/apps/details?id=" +
            "org.indywidualni.centrumfm";
    private static final int RDS_REFRESH_INTERVAL = 30000;

    private Handler rdsHandler = new Handler();
    private Handler playerHandler = new Handler();
    private SharedPreferences preferences;
    private CustomTabActivityHelper customTabActivityHelper;
    private Tracker tracker;
    private StreamService mService;
    private boolean mBound;

    @Bind(R.id.toolbar) MarqueeToolbar toolbar;
    @Bind(R.id.drawer_layout) DrawerLayout mDrawerLayout;
    @Bind(R.id.panel_main) CoordinatorLayout panelMain;
    @Bind(R.id.sliding_layout) SlidingUpPanelLayout sup;
    @Bind(R.id.fab) FloatingActionButton fab;
    @Bind(R.id.main_drawer) NavigationView mDrawer;
    @Bind(R.id.panel_slider) LinearLayout panelSlider;
    @Bind(R.id.tabs) SlidingTabLayout slidingTabLayout;

    @Bind(R.id.playerElapsed) TextView playerElapsed;
    @Bind(R.id.playerConnection) TextView playerConnection;
    @Bind(R.id.playerPauseResume) ImageView playerPauseResume;
    @Bind(R.id.playerStop) ImageView playerStop;

    private ActionBarDrawerToggle drawerToggle;
    @IdRes private int mSelectedId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Google Analytics tracker
        tracker = ((MyApplication) getApplication()).getDefaultTracker();

        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.setSubtitleTextColor(Color.WHITE);
        toolbar.setTitle(getString(R.string.toolbar_default_title));
        setSupportActionBar(toolbar);

        mDrawer.setNavigationItemSelectedListener(this);

        drawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.drawer_open,
                R.string.drawer_close);
        mDrawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();

        // set the first item as selected by default
        //noinspection ResourceType
        mSelectedId = savedInstanceState == null ? R.id.navigation_news : savedInstanceState
                .getInt("SELECTED_ID");
        itemSelection(mSelectedId);
        mDrawer.setCheckedItem(mSelectedId);

        sup.addPanelSlideListener(
                new SlidingUpPanelLayout.PanelSlideListener() {
                    @Override
                    public void onPanelSlide(View panel, float slideOffset) {
                        // squeeze panelMain (with animation)
                        float newPadding = panelSlider.getHeight() * slideOffset;
                        panelMain.setPadding(0, 0, 0, (int) newPadding);
                    }

                    @Override
                    public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState
                            previousState, SlidingUpPanelLayout.PanelState newState) {
                        if (newState == SlidingUpPanelLayout.PanelState.EXPANDED) {
                            ScrollAwareFABBehavior.setDisableBehaviourPermanently(true);
                            fab.hide();

                            playerSetConnectionType();
                            startPlayerUpdater();
                        } else if (newState == SlidingUpPanelLayout.PanelState.COLLAPSED) {
                            ScrollAwareFABBehavior.setDisableBehaviourPermanently(false);
                            fab.show();

                            if (mBound)
                                mService.stopPlayer();

                            stopPlayerUpdater();
                        }
                    }
                }
        );

        sup.setTouchEnabled(false);

        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        customTabActivityHelper = new CustomTabActivityHelper();
        customTabActivityHelper.setConnectionCallback(this);

        startStreamService();

        // show changelog once for a version
        ChangeLog cl = new ChangeLog(this);
        if (cl.isFirstRun())
            cl.getLogDialog().show();
    }

    @Override
    public void onCustomTabsConnected() {
        Log.v(TAG, "Custom tabs connected");
    }

    @Override
    public void onCustomTabsDisconnected() {
        Log.v(TAG, "Custom tabs disconnected");
    }

    @Override
    protected void onStart() {
        super.onStart();
        bindStreamService();
        customTabActivityHelper.bindCustomTabsService(this);
    }

    @Override
    public void onResume() {
        super.onResume();  // Always call the superclass method first

        // schedule RDS updates
        rdsHandler.post(rdsRunnable);

        // schedule player updates
        if (mBound) {
            if (mService.isPlaying())
                startPlayerUpdater();
            else
                mService.stopPlayer();
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        // cancel RDS updates
        rdsHandler.removeCallbacks(rdsRunnable);

        // stop player updates
        stopPlayerUpdater();
    }

    @Override
    protected void onStop() {
        super.onStop();
        customTabActivityHelper.unbindCustomTabsService(this);

        if (mBound) {
            if (mService.isPlaying())
                mService.foregroundStart();
            else
                mService.stopPlayer();
            unbindStreamService();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.unbind(this);
        customTabActivityHelper.setConnectionCallback(null);

        if (mService != null && !mService.isPlaying())
            stopStreamService();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_website) {
            openCustomTab(WEBSITE_URL);
            return true;
        } else if (id == R.id.action_people) {
            openCustomTab(WEBSITE_PEOPLE);
            return true;
        } else if (id == R.id.action_share) {
            shareTextUrl(APP_PLAY_URL, getString(R.string.share_app_description));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        drawerToggle.onConfigurationChanged(newConfig);

        if (sup.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED)
            panelMain.setPadding(0, 0, 0, sup.findViewById(R.id.panel_slider).getHeight());
        else
            panelMain.setPadding(0, 0, 0, 0);
    }

    private void itemSelection(int mSelectedId) {
        switch (mSelectedId) {
            case R.id.navigation_news:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment,
                        new MainFragment()).commit();
                slidingTabLayout.setVisibility(View.GONE);
                mDrawerLayout.closeDrawer(GravityCompat.START);
                restoreFab();
                break;
            case R.id.navigation_schedule:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment,
                        new ScheduleFragment()).commit();
                slidingTabLayout.setVisibility(View.VISIBLE);
                mDrawerLayout.closeDrawer(GravityCompat.START);
                restoreFab();
                break;
            case R.id.navigation_favourite:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment,
                        new FavouriteFragment()).commit();
                slidingTabLayout.setVisibility(View.GONE);
                mDrawerLayout.closeDrawer(GravityCompat.START);
                restoreFab();
                break;
            case R.id.navigation_about:
                startActivity(new Intent(this, AboutActivity.class));
                slidingTabLayout.setVisibility(View.GONE);
                mDrawerLayout.closeDrawer(GravityCompat.START);
                break;
            case R.id.navigation_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                slidingTabLayout.setVisibility(View.GONE);
                mDrawerLayout.closeDrawer(GravityCompat.START);
                break;
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem menuItem) {
        menuItem.setChecked(true);
        mSelectedId = menuItem.getItemId();
        itemSelection(mSelectedId);
        return true;
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        //save selected item so it will remains same even after orientation change
        outState.putInt("SELECTED_ID", mSelectedId);
    }

    private void getRDS() {
        Call<List<RDS>> call = RestClient.getClientJSON().getRDS();
        call.enqueue(new Callback<List<RDS>>() {
            @Override
            public void onResponse(Call<List<RDS>> call, Response<List<RDS>> response) {
                Log.v(TAG, "getRDS: response " + response.code());

                if (response.isSuccess()) { // tasks available
                    updateTitle(response.body());
                } else {
                    // error response, no access to resource?
                    if (toolbar != null) {
                        toolbar.setTitle(getString(R.string.toolbar_default_title));
                        toolbar.setSubtitle(null);
                    }

                    tracker.send(new HitBuilders.EventBuilder()
                    .setCategory("Error response")
                            .setAction("Get RDS")
                            .setLabel("error " + response.code())
                            .build());
                }
            }

            @Override
            public void onFailure(Call<List<RDS>> call, Throwable t) {
                Log.e(TAG, "getRDS: " + t.getLocalizedMessage());
                if (toolbar != null) {
                    toolbar.setTitle(getString(R.string.toolbar_default_title));
                    toolbar.setSubtitle(null);
                }
            }
        });
    }

    private void updateTitle(List<RDS> items){
        if (toolbar == null)
            return;

        RDS now = items.get(0);
        RDS soon = items.get(1);

        if (!now.getTitle().isEmpty() && !now.getArtist().isEmpty())
            toolbar.setTitle("\u266A " + now.getArtist() + " – " + now.getTitle());
        else
            toolbar.setTitle(getString(R.string.toolbar_default_title));

        if (!soon.getTitle().isEmpty() && !soon.getArtist().isEmpty())
            toolbar.setSubtitle("\u00BB " + soon.getArtist() + " – " + soon.getTitle());
        else
            toolbar.setSubtitle(null);
    }

    public void openCustomTab(String pageUrl) {
        tracker.send(new HitBuilders.EventBuilder()
                .setCategory("Open Custom Tab")
                .setAction(pageUrl)
                .build());

        CustomTabsIntent customTabsIntent =
                new CustomTabsIntent.Builder(customTabActivityHelper.getSession())
                        .setToolbarColor(ContextCompat.getColor(this, R.color.colorPrimary))
                        .setStartAnimations(this, R.anim.slide_in_right, R.anim.slide_out_left)
                        .setExitAnimations(this, R.anim.slide_in_left, R.anim.slide_out_right)
                        .build();
        CustomTabActivityHelper.openCustomTab(this, customTabsIntent, Uri.parse(pageUrl),
                new CustomTabActivityHelper.CustomTabFallback() {
                    @Override
                    public void openUri(Activity activity, Uri uri) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                        startActivity(intent);
                    }
                });
    }

    public void shareTextUrl(String url, String description) {
        tracker.send(new HitBuilders.EventBuilder()
                .setCategory("Share")
                .setAction(url)
                .build());

        Intent share = new Intent(android.content.Intent.ACTION_SEND);
        share.setType("text/plain");

        if (Build.VERSION.SDK_INT < 21) {
            //noinspection deprecation
            share.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        } else
            share.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);

        // add data to the intent, the receiving app will decide
        // what to do with it.
        share.putExtra(Intent.EXTRA_SUBJECT, description);
        share.putExtra(Intent.EXTRA_TEXT, url);

        startActivity(Intent.createChooser(share, getString(R.string.action_share)));
    }

    public void playEnclosure(String url) {
        if (url == null)
            return;

        tracker.send(new HitBuilders.EventBuilder()
                .setCategory("Play Enclosure")
                .setAction(url)
                .build());

        if (Connectivity.isConnected(this)) {
            if (mBound)
                mService.playUrl(url);
            sup.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
        } else {
            Snackbar.make(panelMain, getString(R.string.no_network),
                    Snackbar.LENGTH_LONG).show();
        }
    }

    @OnClick(R.id.fab)
    public void fabClicked() {
        if (mBound) {
            if (Connectivity.isConnected(this)) {
                if (Connectivity.isConnectedMobile(this)
                        && !preferences.getBoolean("mobile_streaming", false)) {
                    Snackbar snackbar = Snackbar.make(panelMain,
                            getString(R.string.mobile_restriction), Snackbar.LENGTH_LONG)
                            .setAction(getString(R.string.app_settings), new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    startActivity(new Intent(getApplicationContext(),
                                            SettingsActivity.class));
                                }
                            });
                    snackbar.show();
                } else {
                    mService.playUrl(STREAM_URL);
                    sup.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);

                    if (Connectivity.isConnectedMobile(this)) {
                        tracker.send(new HitBuilders.EventBuilder()
                                .setCategory("Play Radio")
                                .setAction("Play stream mobile")
                                .build());
                    } else {
                        tracker.send(new HitBuilders.EventBuilder()
                                .setCategory("Play Radio")
                                .setAction("Play stream Wi-Fi")
                                .build());
                    }
                }
            } else {
                Snackbar.make(panelMain, getString(R.string.no_network),
                        Snackbar.LENGTH_LONG).show();
            }
        }
    }

    @OnClick(R.id.playerStop)
    public void stopClicked() {
        sup.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
    }

    @OnClick(R.id.playerPauseResume)
    public void pauseResumeClicked() {
        if (mBound)
            mService.pauseResumePlayer();
    }

    private void restoreFab() {
        if (sup.getPanelState() == SlidingUpPanelLayout.PanelState.COLLAPSED)
            fab.show();
    }

    private void startStreamService() {
        Intent startIntent = new Intent(getApplicationContext(), StreamService.class);
        startService(startIntent);
    }

    private void stopStreamService() {
        Intent stopIntent = new Intent(getApplicationContext(), StreamService.class);
        stopService(stopIntent);
    }

    private void bindStreamService() {
        Intent bindIntent = new Intent(getApplicationContext(), StreamService.class);
        getApplicationContext().bindService(bindIntent, mConnection, Context.BIND_AUTO_CREATE);
    }

    private void unbindStreamService() {
        if (mBound)
            getApplicationContext().unbindService(mConnection);
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            Log.d("ServiceConnection", "connected");
            StreamService.LocalBinder binder = (StreamService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;

            // when the service is bound there is no need to display a notification (foreground)
            mService.foregroundStop();

            if (mService.isPlaying())
                sup.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
            else
                sup.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        }
        @Override
        public void onServiceDisconnected(ComponentName className) {
            Log.d("ServiceConnection", "disconnected");
            mBound = false;
        }
    };

    private Runnable rdsRunnable = new Runnable() {
        @Override
        public void run() {
            // Do something here on the main thread
            getRDS();
            // Repeat this the same runnable code block again another 30 seconds
            rdsHandler.postDelayed(rdsRunnable, RDS_REFRESH_INTERVAL);
        }
    };

    // to avoid creating new Strings every second
    String elapsed;
    String duration;
    long totalDuration;

    private static String convertMillisToHuman(long total) {
        total /= 1000;
        int minutes = (int) (total % 3600) / 60;
        int seconds = (int) total % 60;
        return minutes + ":" + (seconds < 10 ? "0" + seconds : seconds);
    }

    private Runnable playerRunnable = new Runnable() {
        @Override
        public void run() {
            // Do something here on the main thread
            if (mBound) {
                if (mService.isPlaying()) {
                    totalDuration = mService.getDuration();
                    if (totalDuration / 1000 == 0)
                        duration = "\u221e";
                    else
                        duration = convertMillisToHuman(totalDuration);

                    elapsed = convertMillisToHuman(mService.getCurrentPosition()) + " / " + duration;
                    if (playerElapsed != null)
                        playerElapsed.setText(elapsed);
                    playerSetConnectionType();
                } else {
                    if (mService.isMediaPlayerNull()) {
                        stopPlayerUpdater();
                        if (sup != null)
                            sup.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                        return;
                    }
                    if (playerElapsed != null)
                        playerElapsed.setText(getString(R.string.preparing_playback));
                }
            } else {
                stopPlayerUpdater();
                return;
            }

            // Repeat this the same runnable code block again another 1 second
            playerHandler.postDelayed(playerRunnable, 1000);
        }
    };

    private void startPlayerUpdater() {
        playerHandler.removeCallbacks(playerRunnable);
        playerHandler.postDelayed(playerRunnable, 1000);
    }

    private void stopPlayerUpdater() {
        playerHandler.removeCallbacks(playerRunnable);
    }

    private void playerSetConnectionType() {
        if (playerConnection == null)
            return;
        if (Connectivity.isConnected(this)) {
            if (Connectivity.isConnectedMobile(this))
                playerConnection.setText(getString(R.string.mobile_connection));
            else
                playerConnection.setText(getString(R.string.wifi_connection));
        }
    }

}