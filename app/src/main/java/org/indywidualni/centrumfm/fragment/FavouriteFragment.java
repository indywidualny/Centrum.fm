package org.indywidualni.centrumfm.fragment;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.indywidualni.centrumfm.R;
import org.indywidualni.centrumfm.rest.adapter.FavouriteAdapter;
import org.indywidualni.centrumfm.rest.model.Schedule;
import org.indywidualni.centrumfm.util.database.DataSource;
import org.indywidualni.centrumfm.util.ui.AnimatedLayoutManager;
import org.indywidualni.centrumfm.util.ui.RecyclerViewEmptySupport;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class FavouriteFragment extends TrackedFragment {

    @BindView(R.id.recycler_view) RecyclerViewEmptySupport mRecyclerView;
    private Unbinder unbinder;

    private View emptyView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favourite, container, false);
        emptyView = view.findViewById(R.id.empty_view);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new AnimatedLayoutManager(getActivity()));
        mRecyclerView.setAdapter(new FavouriteAdapter(new ArrayList<>()));

        new AsyncTask<Void, Void, List<Object>>() {
            @Override
            protected List<Object> doInBackground(Void... arg0) {
                List<Schedule.Event> items = DataSource.getInstance().getScheduleFavourite();
                Collections.sort(items);
                return getFavouritesWithLabels(items);
            }

            @Override
            protected void onPostExecute(List<Object> result) {
                try {
                    FavouriteAdapter adapter = new FavouriteAdapter(result);
                    adapter.setHasStableIds(true);
                    mRecyclerView.setEmptyView(emptyView);
                    mRecyclerView.setAdapter(adapter);
                } catch (NullPointerException e) {
                    // fragment was destroyed while AsyncTask was running
                    e.printStackTrace();
                }
            }
        }.execute();
    }

    private ArrayList<Object> getFavouritesWithLabels(List<Schedule.Event> data) {
        ArrayList<Object> items = new ArrayList<>();
        for (int i = 0; i < 7; ++i) {
            items.add(getResources().getStringArray(R.array.weekdays)[i]);
            for (int j = 0; j < data.size(); ++j) {
                if (data.get(j).getWeekdays().contains(Integer.toString(i)))
                    items.add(data.get(j));
            }
            if (items.get(items.size() - 1) instanceof String)
                items.remove(items.size() - 1);
        }
        return items;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        emptyView = null;
        unbinder.unbind();
    }

}
