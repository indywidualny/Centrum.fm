package org.indywidualni.centrumfm.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import org.indywidualni.centrumfm.MyApplication;
import org.indywidualni.centrumfm.R;
import org.indywidualni.centrumfm.activity.SongsActivity;
import org.indywidualni.centrumfm.rest.RestClient;
import org.indywidualni.centrumfm.rest.adapter.SongsAdapter;
import org.indywidualni.centrumfm.rest.model.Song;
import org.indywidualni.centrumfm.util.ui.RecyclerViewEmptySupport;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static org.indywidualni.centrumfm.rest.RestClient.ApiEndpointInterface.SONGS_FROM;
import static org.indywidualni.centrumfm.rest.RestClient.ApiEndpointInterface.SONGS_TO;
import static org.indywidualni.centrumfm.rest.RestClient.ApiEndpointInterface.SONGS_LIMIT;
import static org.indywidualni.centrumfm.rest.RestClient.ApiEndpointInterface.SONGS_SKIP;
import static org.indywidualni.centrumfm.rest.RestClient.ApiEndpointInterface.SONGS_POPULAR;
import static org.indywidualni.centrumfm.rest.RestClient.ApiEndpointInterface.SONGS_COUNT;

public class SongArchiveFragment extends Fragment implements SearchView.OnQueryTextListener {

    private static final String TAG = SongArchiveFragment.class.getSimpleName();
    
    private static final String FRAGMENT_TYPE = "fragment_type";
    private static final String DATA_PARCEL = "data_parcel";
    private static final String QUERY_PARAMETERS = "query_parameters";

    private RecyclerViewEmptySupport mRecyclerView;
    private View emptyView;

    private IFragmentToActivity mCallback;
    private List<Song> songs = new ArrayList<>();
    private Map<String, String> queryParameters = new HashMap<>();
    private Call<List<Song>> call;
    private SongsAdapter adapter;
    private boolean popular;

    public static SongArchiveFragment create(boolean popular) {
        Bundle args = new Bundle();
        args.putBoolean(FRAGMENT_TYPE, popular);
        SongArchiveFragment fragment = new SongArchiveFragment();
        fragment.setArguments(args);
        return fragment;
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        popular = getArguments().getBoolean(FRAGMENT_TYPE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_song_archive, container, false);
        mRecyclerView = (RecyclerViewEmptySupport) view.findViewById(R.id.recycler_view);
        emptyView = view.findViewById(R.id.empty_view);
        return view;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setHasOptionsMenu(true);

        if (savedInstanceState != null) {
            songs = savedInstanceState.getParcelableArrayList(DATA_PARCEL);
            queryParameters = (HashMap<String, String>) savedInstanceState
                    .getSerializable(QUERY_PARAMETERS);
            
            mRecyclerView.setEmptyView(emptyView);
        } else {
            queryParameters.put(SONGS_POPULAR, popular ? "1" : "0");
            queryParameters.put(SONGS_LIMIT, popular ? "100" : "500");
        }

        adapter = new SongsAdapter(getContext(), songs);

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setAdapter(adapter);

        if (songs.isEmpty())
            getSongs();
    }

    @Override
    public void onResume() {
        super.onResume();
        registerForContextMenu(mRecyclerView);
        
        // it's not simply an instance of TrackedFragment
        String affix = "normal";  // we need to know whether it's used for popular songs
        if (popular) affix = "popular";
        final Tracker tracker = ((MyApplication) getActivity().getApplication())
                .getDefaultTracker();
        tracker.setScreenName(getActivity().getClass().getSimpleName() + "/"
                + getClass().getSimpleName() + "/" + affix);
        tracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterForContextMenu(mRecyclerView);
        if (call != null)
            call.cancel();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(DATA_PARCEL, (ArrayList<Song>) songs);
        outState.putSerializable(QUERY_PARAMETERS, (HashMap<String, String>) queryParameters);
    }

    private void getSongs() {
        call = RestClient.getClientJSON().getSongs(queryParameters);
        call.enqueue(new Callback<List<Song>>() {
            @Override
            public void onResponse(Call<List<Song>> call, Response<List<Song>> response) {
                Log.v(TAG, "getSongs: response " + response.code());
                if (response.isSuccessful()) {
                    songs = response.body();
                    adapter.setDataset(songs);
                } else {
                    // error response, no access to resource?
                    Log.v(TAG, "Cannot obtain list of songs");
                }
                mRecyclerView.setEmptyView(emptyView);
            }

            @Override
            public void onFailure(Call<List<Song>> call, Throwable t) {
                Log.e(TAG, "getSongs: " + t.getLocalizedMessage());
                mRecyclerView.setEmptyView(emptyView);
            }
        });
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.context_menu_songs, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        // Handle item selection
        if (getUserVisibleHint()) {
            Song currentItem = adapter.getDataset().get(SongsActivity.currentPosition);
            switch (item.getItemId()) {
                case R.id.context_like:
                    mCallback.favouriteSong(currentItem);
                    return true;
                case R.id.context_listen:
                    mCallback.playSong(currentItem);
                    return true;
                case R.id.context_share:
                    mCallback.shareSong(currentItem);
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

    @OnClick(R.id.fab)
    public void fabClicked() {
        Toast.makeText(getActivity(), "Fab Click!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mRecyclerView = null;
        emptyView = null;
    }

    public interface IFragmentToActivity {
        void favouriteSong(Song song);
        void playSong(Song song);
        void shareSong(Song song);
    }

}
