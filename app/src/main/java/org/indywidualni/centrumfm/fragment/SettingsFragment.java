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

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SettingsFragment extends PreferenceFragment
        implements Preference.OnPreferenceClickListener {

    public static final String DEFAULT_REMINDER_OFFSET = "-300000";
    private static final String TAG = SettingsFragment.class.getSimpleName();
    private SharedPreferences.OnSharedPreferenceChangeListener prefChangeListener;
    private SharedPreferences preferences;
    private IFragmentToActivity mCallback;
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
        // cancel Retrofit calls
        RestClient.getClientJSON().getServerStatus().cancel();
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        switch (preference.getKey()) {
            case "open_source_libraries":
                mCallback.showLibraries();
                return true;

            case "changelog":
                mCallback.showChangelog();
                return true;

            default:
                return false;
        }
    }

    private void getServerStatus() {
        Call<Server> call = RestClient.getClientJSON().getServerStatus();
        call.enqueue(new Callback<Server>() {
            @Override
            public void onResponse(Call<Server> call, Response<Server> response) {
                Log.v(TAG, "getServerStatus: response " + response.code());
                Preference status = findPreference("indywidualni_server_status");

                if (response.isSuccess()) {
                    if (isAdded())
                        status.setSummary(String.format(getString(R.string.indywidualni_server_ok),
                                response.body().getVersion()));
                } else {
                    // error response, no access to resource?
                    if (isAdded())
                        status.setSummary(String.format(getString(R.string.indywidualni_server_not_ok),
                                response.code()));

                    tracker.send(new HitBuilders.EventBuilder()
                            .setCategory("Error response")
                            .setAction("Get Server Status")
                            .setLabel("error " + response.code())
                            .build());
                }
            }

            @Override
            public void onFailure(Call<Server> call, Throwable t) {
                Log.e(TAG, "getServerStatus: " + t.getLocalizedMessage());
                Preference status = findPreference("indywidualni_server_status");
                if (isAdded())
                    status.setSummary(getString(R.string.indywidualni_server_failure));
            }
        });
    }

    public interface IFragmentToActivity {
        void showChangelog();

        void showLibraries();
    }

}