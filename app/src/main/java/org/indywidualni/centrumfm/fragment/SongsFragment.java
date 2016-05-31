package org.indywidualni.centrumfm.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.indywidualni.centrumfm.R;
import org.indywidualni.centrumfm.activity.SongsActivity;
import org.indywidualni.centrumfm.util.ui.SlidingTabLayout;

import butterknife.Bind;
import butterknife.ButterKnife;

public class SongsFragment extends TrackedFragment implements SongsActivity.IActivityToFragment {

    public static final String CURRENT_POSITION = "position";

    @Bind(R.id.pager)
    ViewPager viewPager;

    private SlidingTabLayout slidingTabLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_songs, container, false);
        slidingTabLayout = ButterKnife.findById(getActivity(), R.id.tabs);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewPager.setAdapter(new MyFragmentPagerAdapter());
        slidingTabLayout.setDistributeEvenly(true);
        slidingTabLayout.setViewPager(viewPager);

        // setting custom color for the scroll bar indicator of the tab view
        slidingTabLayout.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @Override
            public int getIndicatorColor(int position) {
                return ContextCompat.getColor(getActivity(), R.color.icon);
            }
        });

        // restore currently selected tab
        viewPager.setCurrentItem(1);
        if (savedInstanceState != null)
            viewPager.setCurrentItem(savedInstanceState.getInt(CURRENT_POSITION, 0));
        else
            viewPager.setCurrentItem(0);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(CURRENT_POSITION, viewPager.getCurrentItem());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);

        slidingTabLayout.setCustomTabColorizer(null);
        slidingTabLayout.setViewPager(null);
        slidingTabLayout = null;
    }

    @Override
    public void favouriteAdded() {
        ((MyFragmentPagerAdapter) viewPager.getAdapter()).update();
    }

    private class MyFragmentPagerAdapter extends FragmentStatePagerAdapter {

        private final int PAGE_COUNT = 3;

        public MyFragmentPagerAdapter() {
            super(getChildFragmentManager());
        }

        @Override
        public int getCount() {
            return PAGE_COUNT;
        }

        @Override
        public Fragment getItem(int position) {
            if (position != 1)
                return SongArchiveFragment.create(position == 2);
            else
                return new FavouriteSongsFragment();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return getResources().getStringArray(R.array.archives)[position];
        }

        // call this method to update fragments in ViewPager dynamically
        public void update() {
            notifyDataSetChanged();
        }

        @Override
        public int getItemPosition(Object object) {
            if (object instanceof UpdatableFragment)
                ((UpdatableFragment) object).update();

            return super.getItemPosition(object);
        }

    }

}
