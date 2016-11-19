package org.indywidualni.centrumfm.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import org.indywidualni.centrumfm.MyApplication;
import org.indywidualni.centrumfm.R;
import org.indywidualni.centrumfm.rest.RestClient;
import org.indywidualni.centrumfm.rest.model.Server;
import org.indywidualni.centrumfm.util.AlarmHelper;

import retrofit2.adapter.rxjava.HttpException;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class SettingsFragment extends PreferenceFragment
        implements Preference.OnPreferenceClickListener {

    private static final String TAG = SettingsFragment.class.getSimpleName();
    
    public static final String DEFAULT_REMINDER_OFFSET = "-300000";
        
    private SharedPreferences.OnSharedPreferenceChangeListener prefChangeListener;
    private SharedPreferences preferences;
    private IFragmentToActivity mCallback;
    private Subscription subscription;
    private Tracker tracker;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mCallback = (IFragmentToActivity) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement IFragmentToActivity");
        }
    }

    @Override
    public void onDetach() {
        mCallback = null;
        super.onDetach();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);

        // Google Analytics tracker
        tracker = ((MyApplication) getActivity().getApplication()).getDefaultTracker();

        // get Shared Preferences
        preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        // shared preference changed
        prefChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                switch (key) {
                    case "reminders_mode":
                        // reset reminders
                        if (prefs.getString("reminders_mode", DEFAULT_REMINDER_OFFSET).equals("0"))
                            AlarmHelper.cancelAllAlarms();
                        else
                            AlarmHelper.setAllAlarms();
                        break;
                }
                Log.v("SharedPreferenceChange", key + " changed in SettingsFragment");
            }
        };

        // set listeners
        findPreference("open_source_libraries").setOnPreferenceClickListener(this);
        findPreference("changelog").setOnPreferenceClickListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        // register listener
        preferences.registerOnSharedPreferenceChangeListener(prefChangeListener);
        // update server status
        getServerStatus();
    }

    @Override
    public void onPause() {
        super.onPause();
        // unregister listener
        preferences.unregisterOnSharedPreferenceChangeListener(prefChangeListener);
        // cancel Retrofit call
        if (subscription != null)
            subscription.unsubscribe();
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        switch (preference.getKey()) {
            case "open_source_libraries":
                if (mCallback != null)
                    mCallback.showLibraries();
                return true;

            case "changelog":
                if (mCallback != null)
                    mCallback.showChangelog();
                return true;

            default:
                return false;
        }
    }

    private void getServerStatus() {
        Observable<Server> call = RestClient.getClientJson().getServerStatus();
        subscription = call
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(new Subscriber<Server>() {
                    @Override
                    public void onCompleted() {
                        // Notifies the Observer that the Observable has finished sending push-based
                        // notifications.
                    }

                    @Override
                    public void onError(Throwable e) {
                        Preference status = findPreference("indywidualni_server_status");
                        // cast to retrofit.HttpException to get the response code
                        if (e instanceof HttpException) {
                            HttpException response = (HttpException) e;
                            int code = response.code();

                            // error response, no access to resource?
                            Log.v(TAG, "getServerStatus: response " + code);
                            if (isAdded())
                                status.setSummary(String.format(getString(R.string.indywidualni_server_not_ok),
                                        code));

                            tracker.send(new HitBuilders.EventBuilder()
                            .setCategory("Error response")
                            .setAction("Get Server Status")
                            .setLabel("error " + code)
                            .build());
                        } else {
                            Log.e(TAG, "getServerStatus: " + e.getLocalizedMessage());
                            if (isAdded())
                                status.setSummary(getString(R.string.indywidualni_server_failure));
                        }
                    }

                    @Override
                    public void onNext(Server server) {
                        Log.v(TAG, "getServerStatus: response 200");
                        Preference status = findPreference("indywidualni_server_status");
                        if (isAdded())
                            status.setSummary(String.format(getString(R.string.indywidualni_server_ok),
                                    server.getVersion()));
                    }
                });
    }

    public interface IFragmentToActivity {
        void showChangelog();
        void showLibraries();
    }

}
