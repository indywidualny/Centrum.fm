package org.indywidualni.centrumfm.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.indywidualni.centrumfm.R;
import org.indywidualni.centrumfm.activity.SongsActivity;
import org.indywidualni.centrumfm.util.Miscellany;
import org.indywidualni.centrumfm.util.ui.NonSwipeableViewPager;
import org.indywidualni.centrumfm.util.ui.SlidingTabLayout;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class SongsFragment extends Fragment implements SongsActivity.IActivityToFragment {

    public static final String CURRENT_POSITION = "position";

    @BindView(R.id.pager) NonSwipeableViewPager viewPager;
    private Unbinder unbinder;

    private SlidingTabLayout slidingTabLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_songs, container, false);
        slidingTabLayout = ButterKnife.findById(getActivity(), R.id.tabs);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewPager.setAdapter(new MyFragmentPagerAdapter());
        slidingTabLayout.setDistributeEvenly(true);
        slidingTabLayout.setViewPager(viewPager);

        // setting custom color for the scroll bar indicator of the tab view
        slidingTabLayout.setCustomTabColorizer(position -> ContextCompat.getColor(getActivity(),
                R.color.icon));

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                setSubtitle(position);
            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPx) {}

            @Override
            public void onPageScrollStateChanged(int state) {}
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
        slidingTabLayout.setCustomTabColorizer(null);
        slidingTabLayout.setViewPager(null);
        slidingTabLayout = null;
        unbinder.unbind();
    }

    @Override
    public void favouriteAdded() {
        ((MyFragmentPagerAdapter) viewPager.getAdapter()).update();
    }

    @SuppressWarnings("ConstantConditions")
    private void setSubtitle(int position) {
        assert ((AppCompatActivity) getActivity()).getSupportActionBar() != null;
        if (position == 0) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setSubtitle(Miscellany.
                    formatSubtitleRead(getContext(), SongArchiveFragment.currentSubtitle));
        } else
            ((AppCompatActivity) getActivity()).getSupportActionBar().setSubtitle(null);
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
