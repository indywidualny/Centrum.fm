package org.indywidualni.centrumfm.fragment;

import android.support.v4.app.Fragment;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import org.indywidualni.centrumfm.MyApplication;

public abstract class TrackedFragment extends Fragment {

    @Override
    public void onResume() {
        super.onResume();

        if (getUserVisibleHint()) {
            final Tracker tracker = ((MyApplication) getActivity().getApplication())
                    .getDefaultTracker();
            tracker.setScreenName(getActivity().getClass().getSimpleName() + "/"
                    + getClass().getSimpleName());
            tracker.send(new HitBuilders.ScreenViewBuilder().build());
        }
    }

}
