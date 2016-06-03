package org.indywidualni.centrumfm.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
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
import org.indywidualni.centrumfm.util.Connectivity;
import org.indywidualni.centrumfm.util.Miscellany;
import org.indywidualni.centrumfm.util.ui.RecyclerViewEmptySupport;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static org.indywidualni.centrumfm.rest.RestClient.ApiEndpointInterface.SONGS_FROM;
import static org.indywidualni.centrumfm.rest.RestClient.ApiEndpointInterface.SONGS_LIMIT;
import static org.indywidualni.centrumfm.rest.RestClient.ApiEndpointInterface.SONGS_POPULAR;
import static org.indywidualni.centrumfm.rest.RestClient.ApiEndpointInterface.SONGS_SKIP;
import static org.indywidualni.centrumfm.rest.RestClient.ApiEndpointInterface.SONGS_TO;

public class SongArchiveFragment extends Fragment implements SearchView.OnQueryTextListener {

    private static final String TAG = SongArchiveFragment.class.getSimpleName();
    
    private static final String FRAGMENT_TYPE = "fragment_type";
    private static final String DATA_PARCEL = "data_parcel";
    private static final String QUERY_PARAMETERS = "query_parameters";
    private static final String ITEMS_PER_REQUEST = "50";

    @BindView(R.id.coordinator_layout) CoordinatorLayout coordinatorLayout;
    @BindView(R.id.recycler_view) RecyclerViewEmptySupport mRecyclerView;
    @BindView(R.id.swipe_refresh) SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.empty_view) View emptyView;
    @BindView(R.id.loading) View loadingView;
    @BindView(R.id.fab) FloatingActionButton fab;
    private Unbinder unbinder;

    private IFragmentToActivity mCallback;
    private List<Song> songs = new ArrayList<>();
    private Map<String, String> queryParameters = new HashMap<>();
    private LinearLayoutManager linearLayoutManager;
    private boolean dynamicLoadingEnabled = true;
    private Call<List<Song>> call;
    private SongsAdapter adapter;
    private boolean popular;
    private boolean loading;

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
        unbinder = ButterKnife.bind(this, view);
        if (popular) fab.setVisibility(View.GONE);
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
            queryParameters.put(SONGS_LIMIT, ITEMS_PER_REQUEST);
            setDefaultDateTimeRange();
            mRecyclerView.setEmptyView(loadingView);
        }

        swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getSongs(false);
            }
        });

        linearLayoutManager = new LinearLayoutManager(getActivity());
        adapter = new SongsAdapter(getContext(), songs);

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.setAdapter(adapter);

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                int totalItem = linearLayoutManager.getItemCount();
                int lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();

                if (dynamicLoadingEnabled && !loading && lastVisibleItem == totalItem - 1) {
                    // scrolled to bottom
                    loading = true;
                    queryParameters.put(SONGS_SKIP, Integer.toString(totalItem));
                    getSongs(true);
                }
            }
        });

        // no data yet, load it for the first time
        if (songs.isEmpty())
            getSongs(false);
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onResume() {
        super.onResume();
        registerForContextMenu(mRecyclerView);

        // it's not simply an instance of TrackedFragment
        String affix = "Normal";  // we need to know whether it's used for popular songs
        if (popular) affix = "Popular";
        if (getUserVisibleHint()) {
            final Tracker tracker = ((MyApplication) getActivity().getApplication())
                    .getDefaultTracker();
            tracker.setScreenName(getActivity().getClass().getSimpleName() + "/"
                    + getClass().getSimpleName() + "/" + affix);
            tracker.send(new HitBuilders.ScreenViewBuilder().build());
        }
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
    
    private void setDefaultDateTimeRange() {
        if (!popular) {
            queryParameters.put(SONGS_FROM, Miscellany.constructDateQueryForDay(true));
            queryParameters.put(SONGS_TO, Miscellany.constructDateQueryForDay(false));
        }
    }

    private void getSongs(final boolean appendToList) {
        if (call != null) call.cancel();
        swipeRefreshLayout.setRefreshing(true);
        call = RestClient.getClientJSON().getSongs(queryParameters);
        call.enqueue(new Callback<List<Song>>() {
            @Override
            public void onResponse(Call<List<Song>> call, Response<List<Song>> response) {
                Log.v(TAG, "getSongs: response " + response.code());
                if (isAdded()) {
                    if (response.isSuccessful()) {
                        if (appendToList)
                            songs.addAll(response.body());
                        else
                            songs = response.body();
                        adapter.setDataset(songs);
                        if (!appendToList) mRecyclerView.scrollToPosition(0);
                    } else {
                        // error response, no access to resource?
                        Log.v(TAG, "Cannot obtain list of songs");
                        showSnackbarNotice(getString(R.string.problem_server_response, response.code()));
                    }
                    mRecyclerView.changeEmptyView(emptyView);
                    swipeRefreshLayout.setRefreshing(false);
                }
                loading = false;
            }

            @Override
            public void onFailure(Call<List<Song>> call, Throwable t) {
                Log.e(TAG, "getSongs: " + t.getLocalizedMessage());
                if (isAdded()) {
                    mRecyclerView.changeEmptyView(emptyView);
                    swipeRefreshLayout.setRefreshing(false);
                    if (!call.isCanceled()) {
                        if (!Connectivity.isConnected(getContext()))
                            showSnackbarNotice(R.string.no_network);
                        else
                            showSnackbarNotice(R.string.no_response);
                    }
                }
                loading = false;
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
        if (!popular) {
            MenuItem now = menu.add(Menu.NONE, R.id.get_today, 1, R.string.songs_show_today);
            now.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_NEVER);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.get_today:
                setDefaultDateTimeRange();
                getSongs(false);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
    @Override
    public boolean onQueryTextChange(String query) {
        // Here is where we are going to implement our filter logic
        dynamicLoadingEnabled = TextUtils.isEmpty(query);
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

    public void showSnackbarNotice(@StringRes int resource) {
        showSnackbarNotice(getString(resource));
    }

    public void showSnackbarNotice(String string) {
        Snackbar.make(coordinatorLayout, string, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    public interface IFragmentToActivity {
        void favouriteSong(Song song);
        void playSong(Song song);
        void shareSong(Song song);
    }

}
