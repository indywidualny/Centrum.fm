package org.indywidualni.centrumfm.fragment;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import org.indywidualni.centrumfm.MyApplication;
import org.indywidualni.centrumfm.R;
import org.indywidualni.centrumfm.rest.RestClient;
import org.indywidualni.centrumfm.rest.adapter.WeekdayAdapter;
import org.indywidualni.centrumfm.rest.model.Schedule;
import org.indywidualni.centrumfm.util.database.AsyncWrapper;
import org.indywidualni.centrumfm.util.database.DataSource;
import org.indywidualni.centrumfm.util.ui.SlidingTabLayout;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import retrofit2.adapter.rxjava.HttpException;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class ScheduleFragment extends TrackedFragment {

    private static final String TAG = ScheduleFragment.class.getSimpleName();

    private static final String DATA_PARCEL = "data_parcel";
    private static List<Schedule.Event> eventList;
    
    @BindView(R.id.pager) ViewPager viewPager;
    @BindView(R.id.loading) RelativeLayout loading;
    private Unbinder unbinder;

    private SlidingTabLayout slidingTabLayout;
    
    private Subscription subscription;
    private Tracker tracker;

    public static WeekdayAdapter getAdapter(int day) {
        List<Schedule.Event> list = new ArrayList<>();
        for (Schedule.Event e : eventList)
            if (e.getWeekdays().contains(Integer.toString(day)))
                list.add(e);
        Collections.sort(list);
        return new WeekdayAdapter(list, day);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_schedule, container, false);
        slidingTabLayout = ButterKnife.findById(getActivity(), R.id.tabs);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Google Analytics tracker
        tracker = ((MyApplication) getActivity().getApplication()).getDefaultTracker();

        if (savedInstanceState != null) {
            eventList = savedInstanceState.getParcelableArrayList(DATA_PARCEL);
            onItemsLoadComplete();
        } else
            getSchedule();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(DATA_PARCEL, (ArrayList<Schedule.Event>) eventList);
    }
    
    @Override
    public void onPause() {
        super.onPause();
        // cancel Retrofit call
        if (subscription != null)
            subscription.unsubscribe();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        slidingTabLayout.setCustomTabColorizer(null);
        slidingTabLayout.setViewPager(null);
        slidingTabLayout = null;
        unbinder.unbind();
    }

    private void onItemsLoadComplete() {
        try {
            viewPager.setAdapter(new MyFragmentPagerAdapter());
            slidingTabLayout.setViewPager(viewPager);

            // setting custom color for the scroll bar indicator of the tab view
            slidingTabLayout.setCustomTabColorizer(position -> ContextCompat.getColor(getActivity(),
                    R.color.icon));

            // get current day of the week
            int currentDay = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1;

            // sunday tab color fix
            if (currentDay == 0)
                viewPager.setCurrentItem(1);

            // set current day view as an active tab
            viewPager.setCurrentItem(currentDay);

            loading.setVisibility(View.GONE);
            viewPager.setVisibility(View.VISIBLE);
        } catch (NullPointerException e) {
            // fragment was destroyed while AsyncTask was running
            e.printStackTrace();
        }
    }

    private void getSchedule() {
        Observable<Schedule> call = RestClient.getClientXml().getSchedule();
        subscription = call
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(new Subscriber<Schedule>() {
                    @Override
                    public void onCompleted() {
                        // Notifies the Observer that the Observable has finished sending push-based
                        // notifications.
                    }

                    @Override
                    public void onError(Throwable e) {
                        // cast to retrofit.HttpException to get the response code
                        if (e instanceof HttpException) {
                            HttpException response = (HttpException) e;
                            int code = response.code();

                            // error response, no access to resource?
                            Log.v(TAG, "getSchedule: response " + code);
                            new LoadFallback().execute();

                            tracker.send(new HitBuilders.EventBuilder()
                                    .setCategory("Error response")
                                    .setAction("Get Schedule")
                                    .setLabel("error " + code)
                                    .build());
                        } else {
                            Log.e(TAG, "getSchedule: " + e.getLocalizedMessage());
                            new LoadFallback().execute();
                        }
                    }

                    @Override
                    public void onNext(final Schedule schedule) {
                        Log.v(TAG, "getSchedule: response 200");
                        new AsyncTask<Void, Void, Void>() {
                            @Override
                            protected Void doInBackground(Void... arg0) {
                                eventList = schedule.getEvents();
                                for (Schedule.Event event : eventList) {
                                    event.setFavourite(DataSource.getInstance()
                                            .isEventFavourite(event.getId()));
                                }
                                AsyncWrapper.insertSchedule(eventList);
                                return null;
                            }

                            @Override
                            protected void onPostExecute(Void arg) {
                                onItemsLoadComplete();
                            }
                        }.execute();
                    }
                });
    }

    private class LoadFallback extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... arg0) {
            eventList = DataSource.getInstance().getScheduleNormal();
            return null;
        }

        @Override
        protected void onPostExecute(Void arg) {
            onItemsLoadComplete();
        }
    }

    private class MyFragmentPagerAdapter extends FragmentStatePagerAdapter {

        private final int PAGE_COUNT = 7;

        public MyFragmentPagerAdapter() {
            super(getChildFragmentManager());
        }

        @Override
        public int getCount() {
            return PAGE_COUNT;
        }

        @Override
        public Fragment getItem(int position) {
            return WeekdayFragment.create(position);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return getResources().getStringArray(R.array.weekdays)[position];
        }

    }

}
