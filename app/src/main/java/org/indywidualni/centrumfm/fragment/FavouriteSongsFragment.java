package org.indywidualni.centrumfm.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.SearchView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import org.indywidualni.centrumfm.R;
import org.indywidualni.centrumfm.activity.SongsActivity;
import org.indywidualni.centrumfm.rest.adapter.SongsAdapter;
import org.indywidualni.centrumfm.rest.model.Song;
import org.indywidualni.centrumfm.util.database.DataSource;
import org.indywidualni.centrumfm.util.ui.RecyclerViewEmptySupport;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FavouriteSongsFragment extends Fragment implements SearchView.OnQueryTextListener,
        UpdatableFragment {

    private RecyclerViewEmptySupport mRecyclerView;
    private CoordinatorLayout coordinatorLayout;
    private View emptyView;

    private IFragmentToActivity mCallback;
    private List<Song> songs = new ArrayList<>();
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
        coordinatorLayout = (CoordinatorLayout) getActivity().findViewById(R.id.coordinator_layout);
        mRecyclerView = (RecyclerViewEmptySupport) view.findViewById(R.id.recycler_view);
        emptyView = view.findViewById(R.id.empty_view);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setHasOptionsMenu(true);

        songs = DataSource.getInstance().getFavouriteSongs();
        Collections.sort(songs);
        adapter = new SongsAdapter(getContext(), songs);

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setEmptyView(emptyView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
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
                    // allow to revert this action
                    Snackbar snackbar = Snackbar.make(coordinatorLayout,
                            getString(R.string.unfaved), Snackbar.LENGTH_LONG)
                            .setAction(getString(R.string.revert), new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    adapter.addItem(SongsActivity.currentPosition, removed);
                                    songs.add(SongsActivity.currentPosition, removed);
                                }
                            });
                    snackbar.setCallback(new Snackbar.Callback() {
                        @Override
                        public void onDismissed(Snackbar snackbar, int event) {
                            if (event != DISMISS_EVENT_ACTION)
                                mCallback.unfavouriteSong(removed);
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
    public void onDestroyView() {
        super.onDestroyView();
        coordinatorLayout = null;
        mRecyclerView = null;
        emptyView = null;
    }

    public interface IFragmentToActivity {
        void unfavouriteSong(Song song);
        void playSong(Song song);
        void shareSong(Song song);
    }

}
