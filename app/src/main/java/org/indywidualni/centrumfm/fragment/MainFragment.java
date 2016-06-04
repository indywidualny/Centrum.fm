package org.indywidualni.centrumfm.fragment;

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
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainFragment extends TrackedFragment {

    private static final String TAG = MainFragment.class.getSimpleName();

    private static final String DATA_PARCEL = "data_parcel";
    private static List<Channel.Item> rssItems = new ArrayList<>();
    
    @BindView(R.id.recycler_view) RecyclerView mRecyclerView;
    @BindView(R.id.swipe_refresh) SwipeRefreshLayout mSwipeRefreshLayout;
    private Unbinder unbinder;

    private Call<Rss> call;
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
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (Connectivity.isConnected(getActivity()))
                    getRss();
                else {
                    Snackbar.make(getActivity().findViewById(R.id.panel_main),
                            getString(R.string.no_network), Snackbar.LENGTH_LONG).show();
                    mSwipeRefreshLayout.setRefreshing(false);
                }
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
                            mSwipeRefreshLayout.post(new Runnable() {
                                @Override
                                public void run() {
                                    mSwipeRefreshLayout.setRefreshing(true);
                                }
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
        if (call != null)
            call.cancel();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    private void getRss() {
        call = RestClient.getClientRss().getRss();
        call.enqueue(new Callback<Rss>() {
            @Override
            public void onResponse(Call<Rss> call, final Response<Rss> response) {
                Log.v(TAG, "getRss: response " + response.code());

                if (response.isSuccessful()) {  // tasks available
                    new AsyncTask<Void, Void, List<Channel.Item>>() {
                        @Override
                        protected List<Channel.Item> doInBackground(Void... arg0) {
                            DataSource.getInstance().insertNews(response.body()
                                    .getChannel().getItems());
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
                } else {
                    // error response, no access to resource?
                    if (mSwipeRefreshLayout != null)
                        mSwipeRefreshLayout.setRefreshing(false);

                    tracker.send(new HitBuilders.EventBuilder()
                            .setCategory("Error response")
                            .setAction("Get RSS")
                            .setLabel("error " + response.code())
                            .build());
                }
            }

            @Override
            public void onFailure(Call<Rss> call, Throwable t) {
                Log.e(TAG, "getRss: " + t.getLocalizedMessage());
                if (mSwipeRefreshLayout != null)
                    mSwipeRefreshLayout.setRefreshing(false);
            }
        });
    }

}
