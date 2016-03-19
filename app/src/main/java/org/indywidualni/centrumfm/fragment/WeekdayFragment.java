package org.indywidualni.centrumfm.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.indywidualni.centrumfm.R;

import butterknife.Bind;
import butterknife.ButterKnife;

public class WeekdayFragment extends Fragment {

    public static final String ARG_WEEKDAY = "ARG_WEEKDAY";

    @Bind(R.id.recycler_view) RecyclerView mRecyclerView;

    private int mPage;

    public static WeekdayFragment create(int page) {
        Bundle args = new Bundle();
        args.putInt(ARG_WEEKDAY, page);
        WeekdayFragment fragment = new WeekdayFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPage = getArguments().getInt(ARG_WEEKDAY);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_weekday, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setAdapter(ScheduleFragment.getAdapter(mPage));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

}