package org.indywidualni.centrumfm.activity;

import android.app.Dialog;
import android.app.SearchManager;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.indywidualni.centrumfm.R;
import org.indywidualni.centrumfm.fragment.FavouriteSongsFragment;
import org.indywidualni.centrumfm.fragment.SongArchiveFragment;
import org.indywidualni.centrumfm.fragment.SongsFragment;
import org.indywidualni.centrumfm.rest.model.Song;
import org.indywidualni.centrumfm.util.Miscellany;
import org.indywidualni.centrumfm.util.database.AsyncWrapper;
import org.indywidualni.centrumfm.util.database.DataSource;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SongsActivity extends AppCompatActivity
        implements SongArchiveFragment.IFragmentToActivity,
        FavouriteSongsFragment.IFragmentToActivity {

    private static final String TAG_FRAGMENT = "songs_fragment";
    public static int currentPosition;
    
    @BindView(R.id.toolbar) Toolbar toolbar;
    
    private IActivityToFragment songsFragmentCallback;

    @Override
    public void onAttachFragment(Fragment fragment) {
        super.onAttachFragment(fragment);

        if (fragment instanceof IActivityToFragment)
            songsFragmentCallback = (IActivityToFragment) fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_songs);
        ButterKnife.bind(this);

        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.setSubtitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        FragmentManager fm = getSupportFragmentManager();
        SongsFragment fragment = (SongsFragment) fm.findFragmentByTag(TAG_FRAGMENT);

        if (fragment == null) {
            fragment = new SongsFragment();
            fm.beginTransaction().replace(R.id.fragment, fragment, TAG_FRAGMENT).commit();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        songsFragmentCallback = null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_songs, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.songs_info:
                showInfo();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void favouriteSong(Song song) {
        DataSource.getInstance().insertFavouriteSong(song);
        if (songsFragmentCallback != null)
            songsFragmentCallback.favouriteAdded();
    }

    @Override
    public void unfavouriteSong(Song song) {
        AsyncWrapper.removeFavouriteSong(song);
    }

    @Override
    public void playSong(Song song) {
        String searchFor = song.getArtist() + " " + song.getTitle();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setAction(MediaStore.INTENT_ACTION_MEDIA_PLAY_FROM_SEARCH);
        intent.putExtra(SearchManager.QUERY, searchFor);
        intent.putExtra("queryComplete", searchFor);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        //intent.setPackage("com.google.android.music");
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, getString(R.string.action_play_song_error),
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void shareSong(Song song) {
        String shareBody = song.getTitle() + " - " + song.getArtist();
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
                getString(R.string.action_share_song_title));
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
        startActivity(Intent.createChooser(sharingIntent, getString(R.string.action_share_song)));
    }

    public void showInfo() {
        DialogFragment newFragment = InfoDialogFragment.newInstance();
        newFragment.show(getSupportFragmentManager(), "information");
    }

    public interface IActivityToFragment {
        void favouriteAdded();
    }

    public static class InfoDialogFragment extends DialogFragment {
        public static InfoDialogFragment newInstance() {
            return new InfoDialogFragment();
        }

        @Override
        @NonNull
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            int padding = Miscellany.dpToPx(17);

            ScrollView scroller = new ScrollView(getActivity());
            scroller.setPadding(padding, padding, padding, padding);
            scroller.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_INSET);
            scroller.setClipToPadding(false);

            TextView msg = new TextView(getActivity());
            msg.setMovementMethod(LinkMovementMethod.getInstance());
            msg.setText(Html.fromHtml(Miscellany.readFromAssets("songs_info.html")));

            scroller.addView(msg);

            AlertDialog.Builder ab = new AlertDialog.Builder(getActivity());
            ab.setTitle(getString(R.string.songs_info)).setView(scroller)
                    .setCancelable(true);
            return ab.create();
        }
    }

}
