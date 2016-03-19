package org.indywidualni.centrumfm.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

public class SplashActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent startIntent;
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        if (preferences.getBoolean("show_intro_0", true))
            startIntent = new Intent(SplashActivity.this, IntroActivity.class);
        else
            startIntent = new Intent(SplashActivity.this, MainActivity.class);

        startActivity(startIntent);
        finish();
    }

}