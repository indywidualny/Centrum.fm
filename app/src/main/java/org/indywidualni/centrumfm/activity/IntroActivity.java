package org.indywidualni.centrumfm.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;

import com.github.paolorotolo.appintro.AppIntro2;
import com.github.paolorotolo.appintro.AppIntroFragment;

import org.indywidualni.centrumfm.MyApplication;
import org.indywidualni.centrumfm.R;
import org.indywidualni.centrumfm.util.ChangeLog;

public class IntroActivity extends AppIntro2 {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ((MyApplication) getApplication()).getDefaultTracker();
        int color = ContextCompat.getColor(this, R.color.colorPrimaryDark);
        //setSkipButtonEnabled(false);

        addSlide(AppIntroFragment.newInstance(getString(R.string.slide_radio),
                getString(R.string.slide_radio_desc), R.drawable.slide_radio, color));
        addSlide(AppIntroFragment.newInstance(getString(R.string.slide_reader),
                getString(R.string.slide_reader_desc), R.drawable.slide_reader, color));
        addSlide(AppIntroFragment.newInstance(getString(R.string.slide_songs),
                getString(R.string.slide_songs_desc), R.drawable.slide_songs, color));
        addSlide(AppIntroFragment.newInstance(getString(R.string.slide_fav),
                getString(R.string.slide_fav_desc), R.drawable.slide_fav, color));
        
        // to avoid showing changelog after this intro
        new ChangeLog(this).skipLogDialog();
    }

    private void loadMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void rememberShown() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        preferences.edit().putBoolean("show_intro_1", false).apply();
    }
    
    @Override
    public void onSkipPressed(Fragment currentFragment) {
        super.onSkipPressed(currentFragment);
        onDonePressed(currentFragment);
    }

    @Override
    public void onDonePressed(Fragment currentFragment) {
        super.onDonePressed(currentFragment);
        rememberShown();
        loadMainActivity();
    }

}