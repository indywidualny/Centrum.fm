package org.indywidualni.centrumfm.fragment;

import android.database.sqlite.SQLiteConstraintException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import org.indywidualni.centrumfm.MyApplication;
import org.indywidualni.centrumfm.R;
import org.indywidualni.centrumfm.rest.RestClient;
import org.indywidualni.centrumfm.rest.adapter.NewsAdapter;
import org.indywidualni.centrumfm.rest.model.Channel;
import org.indywidualni.centrumfm.rest.model.Rss;
import org.indywidualni.centrumfm.util.Connectivity;
import org.indywidualni.centrumfm.util.database.DataSource;

import java.util.ArrayList;
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

public class MainFragment extends TrackedFragment {

    private static final String TAG = MainFragment.class.getSimpleName();

    private static final String DATA_PARCEL = "data_parcel";
    private static List<Channel.Item> rssItems = new ArrayList<>();
    
    @BindView(R.id.recycler_view) RecyclerView mRecyclerView;
    @BindView(R.id.swipe_refresh) SwipeRefreshLayout mSwipeRefreshLayout;
    private Unbinder unbinder;

    private Subscription subscription;
    private Tracker tracker;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorAccent);
        mSwipeRefreshLayout.setOnRefreshListener(() -> {
            if (Connectivity.isConnected(getActivity()))
                getRss();
            else {
                Snackbar.make(getActivity().findViewById(R.id.panel_main),
                        getString(R.string.no_network), Snackbar.LENGTH_LONG).show();
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        if (savedInstanceState != null)
            rssItems = savedInstanceState.getParcelableArrayList(DATA_PARCEL);

        mRecyclerView.setAdapter(new NewsAdapter(rssItems, getActivity()));

        // Google Analytics tracker
        tracker = ((MyApplication) getActivity().getApplication()).getDefaultTracker();

        if (savedInstanceState == null) {
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... arg0) {
                    // load offline data initially
                    rssItems = DataSource.getInstance().getAllNews();
                    return null;
                }

                @Override
                protected void onPostExecute(Void arg) {
                    try {
                        // set the right adapter now
                        mRecyclerView.setAdapter(new NewsAdapter(rssItems, getActivity()));

                        // now go online and update items
                        if (Connectivity.isConnected(getActivity())) {
                            mSwipeRefreshLayout.post(() -> {
                                if (mSwipeRefreshLayout != null)
                                    mSwipeRefreshLayout.setRefreshing(true);
                            });
                            getRss();
                        }
                    } catch (NullPointerException e) {
                        // fragment was destroyed while AsyncTask was running
                        e.printStackTrace();
                    }
                }
            }.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(DATA_PARCEL, (ArrayList<Channel.Item>) rssItems);
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
        unbinder.unbind();
    }

    private void getRss() {
        Observable<Rss> call = RestClient.getClientRss().getRss();
        subscription = call
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(new Subscriber<Rss>() {
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
                            Log.v(TAG, "getRss: response " + code);
                            if (mSwipeRefreshLayout != null)
                                mSwipeRefreshLayout.setRefreshing(false);

                            tracker.send(new HitBuilders.EventBuilder()
                                    .setCategory("Error response")
                                    .setAction("Get RSS")
                                    .setLabel("error " + code)
                                    .build());
                        } else {
                            Log.e(TAG, "getRss: " + e.getLocalizedMessage());
                            if (mSwipeRefreshLayout != null)
                                mSwipeRefreshLayout.setRefreshing(false);
                        }
                    }

                    @Override
                    public void onNext(final Rss rss) {
                        Log.v(TAG, "getRss: response 200");
                        new AsyncTask<Void, Void, List<Channel.Item>>() {
                            @Override
                            protected List<Channel.Item> doInBackground(Void... arg0) {
                                try {
                                    DataSource.getInstance().insertNews(rss.getChannel()
                                            .getItems());
                                } catch (SQLiteConstraintException e) {
                                    e.printStackTrace();
                                }
                                return DataSource.getInstance().getAllNews();
                            }

                            @Override
                            protected void onPostExecute(List<Channel.Item> result) {
                                try {
                                    rssItems.clear();
                                    mRecyclerView.getAdapter().notifyDataSetChanged();
                                    rssItems.addAll(result);
                                    mRecyclerView.getAdapter().notifyDataSetChanged();
                                    mSwipeRefreshLayout.setRefreshing(false);
                                } catch (NullPointerException e) {
                                    // fragment was destroyed while AsyncTask was running
                                    e.printStackTrace();
                                }
                            }
                        }.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
                    }
                });
    }

}
