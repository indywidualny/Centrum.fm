package org.indywidualni.centrumfm.activity;

import android.app.Dialog;
import android.app.FragmentManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

import org.indywidualni.centrumfm.R;
import org.indywidualni.centrumfm.fragment.SettingsFragment;
import org.indywidualni.centrumfm.util.ChangeLog;
import org.indywidualni.centrumfm.util.Miscellany;

import butterknife.ButterKnife;

public class SettingsActivity extends BaseActivity implements SettingsFragment.IFragmentToActivity {

    private static final String TAG_FRAGMENT = "settings_fragment";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);

        toolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        FragmentManager fm = getFragmentManager();
        SettingsFragment fragment = (SettingsFragment) fm.findFragmentByTag(TAG_FRAGMENT);

        if (fragment == null) {
            fragment = new SettingsFragment();
            fm.beginTransaction().replace(R.id.content_frame, fragment, TAG_FRAGMENT).commit();
        }
    }

    @Override
    public void showChangelog() {
        DialogFragment newFragment = ChangelogDialogFragment.newInstance();
        newFragment.setCancelable(true);
        newFragment.show(getSupportFragmentManager(), "changelog");
    }

    @Override
    public void showLibraries() {
        DialogFragment newFragment = LibrariesDialogFragment.newInstance();
        newFragment.show(getSupportFragmentManager(), "libraries");
    }

    public static class ChangelogDialogFragment extends DialogFragment {
        public static ChangelogDialogFragment newInstance() {
            return new ChangelogDialogFragment();
        }

        @Override
        @NonNull
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return new ChangeLog(getActivity()).getFullLogDialog();
        }
    }

    public static class LibrariesDialogFragment extends DialogFragment {
        public static LibrariesDialogFragment newInstance() {
            return new LibrariesDialogFragment();
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

            if (android.os.Build.VERSION.SDK_INT >= 24) {
                msg.setText(Html.fromHtml(Miscellany.readFromAssets("libraries.html"),
                        Html.FROM_HTML_MODE_LEGACY));
            } else {
                //noinspection deprecation
                msg.setText(Html.fromHtml(Miscellany.readFromAssets("libraries.html")));
            }

            scroller.addView(msg);

            AlertDialog.Builder ab = new AlertDialog.Builder(getActivity());
            ab.setTitle(getString(R.string.open_source_libraries_pref)).setView(scroller)
                    .setCancelable(true);
            return ab.create();
        }
    }

}