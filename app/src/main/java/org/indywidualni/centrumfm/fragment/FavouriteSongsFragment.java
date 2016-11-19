package org.indywidualni.centrumfm.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.SearchView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import org.indywidualni.centrumfm.R;
import org.indywidualni.centrumfm.activity.SongsActivity;
import org.indywidualni.centrumfm.rest.adapter.SongsAdapter;
import org.indywidualni.centrumfm.rest.model.Song;
import org.indywidualni.centrumfm.util.database.AsyncWrapper;
import org.indywidualni.centrumfm.util.database.DataSource;
import org.indywidualni.centrumfm.util.ui.AnimatedLayoutManager;
import org.indywidualni.centrumfm.util.ui.NonSwipeableViewPager;
import org.indywidualni.centrumfm.util.ui.RecyclerViewEmptySupport;
import org.indywidualni.centrumfm.util.ui.SlidingTabLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class FavouriteSongsFragment extends Fragment implements SearchView.OnQueryTextListener,
        SongsAdapter.ViewHolder.IViewHolderClicks, UpdatableFragment {

    @BindView(R.id.recycler_view) RecyclerViewEmptySupport mRecyclerView;
    @BindView(R.id.coordinator_layout) CoordinatorLayout coordinatorLayout;
    @BindView(R.id.empty_view) View emptyView;
    private Unbinder unbinder;

    private SlidingTabLayout slidingTabLayout;
    private NonSwipeableViewPager viewPager;

    private IFragmentToActivity mCallback;
    private List<Song> songs = new ArrayList<>();
    private final ActionModeCallback actionModeCallback = new ActionModeCallback();
    private ActionMode actionMode;
    private SongsAdapter adapter;

    @Override
    public void update() {
        songs = DataSource.getInstance().getFavouriteSongs();
        Collections.sort(songs);
        adapter.setDataset(songs);
    }

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_song_favourite, container, false);
        slidingTabLayout = ButterKnife.findById(getActivity(), R.id.tabs);
        viewPager = ButterKnife.findById(getActivity(), R.id.pager);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setHasOptionsMenu(true);

        songs = DataSource.getInstance().getFavouriteSongs();
        Collections.sort(songs);
        adapter = new SongsAdapter(this, songs);
        adapter.setHasStableIds(true);

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setEmptyView(emptyView);
        mRecyclerView.setLayoutManager(new AnimatedLayoutManager(getActivity()));
        mRecyclerView.setAdapter(adapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        registerForContextMenu(mRecyclerView);
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterForContextMenu(mRecyclerView);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.context_menu_fav_songs, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        // Handle item selection
        if (getUserVisibleHint()) {
            Song currentItem = adapter.getDataset().get(SongsActivity.currentPosition);
            switch (item.getItemId()) {
                case R.id.context_listen:
                    mCallback.playSong(currentItem);
                    return true;
                case R.id.context_share:
                    mCallback.shareSong(currentItem);
                    return true;
                case R.id.context_unfavourite:
                    final Song removed = adapter.removeItem(SongsActivity.currentPosition);
                    songs.remove(removed);
                    if (SongsActivity.currentPosition == 0)
                        adapter.notifyDataSetChanged();
                    // allow to revert this action
                    Snackbar snackbar = Snackbar.make(coordinatorLayout,
                            getString(R.string.unfaved), Snackbar.LENGTH_LONG)
                            .setAction(getString(R.string.revert), view -> {
                                adapter.addItem(SongsActivity.currentPosition, removed);
                                songs.add(SongsActivity.currentPosition, removed);
                                if (SongsActivity.currentPosition == 0)
                                    adapter.notifyDataSetChanged();
                            });
                    snackbar.setCallback(new Snackbar.Callback() {
                        @Override
                        public void onDismissed(Snackbar snackbar, int event) {
                            if (event != DISMISS_EVENT_ACTION) {
                                if (mCallback != null)
                                    mCallback.unfavouriteSong(removed);
                                else
                                    AsyncWrapper.removeFavouriteSong(removed);
                            }
                        }
                    });
                    snackbar.show();
                    return true;
                default:
                    return super.onContextItemSelected(item);
            }
        } else
            return super.onContextItemSelected(item);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        final MenuItem item = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
        searchView.setOnQueryTextListener(this);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onQueryTextChange(String query) {
        // Here is where we are going to implement our filter logic
        final List<Song> filteredModelList = filter(songs, query);
        adapter.animateTo(filteredModelList);
        mRecyclerView.scrollToPosition(0);
        return false;
    }

    private List<Song> filter(List<Song> models, String query) {
        query = query.toLowerCase();

        final List<Song> filteredModelList = new ArrayList<>();
        for (Song model : models) {
            final String text = model.getSearchableData().toLowerCase();
            if (text.contains(query)) {
                filteredModelList.add(model);
            }
        }
        return filteredModelList;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }
    
    @Override
    public void onContentClick(FrameLayout caller, int position) {
        if (actionMode != null)
            toggleSelection(position);
        else {
            SongsActivity.currentPosition = position;
            getActivity().openContextMenu(caller);
        }
    }
    
    @Override
    public boolean onContentLongClick(int position) {
        if (actionMode == null) {
            actionMode = ((AppCompatActivity) getActivity())
                    .startSupportActionMode(actionModeCallback);
        }
        toggleSelection(position);
        return true;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        slidingTabLayout = null;
        viewPager = null;
        unbinder.unbind();
    }

    public interface IFragmentToActivity {
        void unfavouriteSong(Song song);
        void playSong(Song song);
        void shareSong(Song song);
    }

    /**
     * Toggle the selection state of an item.
     *
     * If the item was the last one in the selection and is unselected, the selection is stopped.
     * Note that the selection must already be started (actionMode must not be null).
     *
     * @param position Position of the item to toggle the selection state
     */
    private void toggleSelection(int position) {
        adapter.toggleSelection(position);
        int count = adapter.getSelectedItemCount();

        if (count == 0) {
            actionMode.finish();
        } else {
            actionMode.setTitle(getResources().getQuantityString(R.plurals.songs_n_selected,
                    count, count));
            actionMode.invalidate();
        }
    }

    private class ActionModeCallback implements ActionMode.Callback {
        @SuppressWarnings("unused")
        private final String TAG = ActionModeCallback.class.getSimpleName();

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate (R.menu.menu_actionmode_selected, menu);
            slidingTabLayout.setVisibility(View.GONE);
            viewPager.setSwipeEnabled(false);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.delete_selected:
                    List<Integer> selected = adapter.getSelectedItems();
                    List<Song> toRemove = new ArrayList<>();
                    for (int position : selected)
                        toRemove.add(adapter.getDataset().get(position));
                    songs.removeAll(toRemove);
                    adapter.getDataset().removeAll(toRemove);
                    adapter.notifyDataSetChanged();
                    AsyncWrapper.removeFavouriteSongs(toRemove);
                    mode.finish();
                    return true;

                default:
                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            adapter.clearSelection();
            actionMode = null;
            slidingTabLayout.setVisibility(View.VISIBLE);
            viewPager.setSwipeEnabled(true);
        }
    }

}
