package org.indywidualni.centrumfm.activity;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import org.indywidualni.centrumfm.R;
import org.indywidualni.centrumfm.util.Miscellany;

import butterknife.Bind;
import butterknife.ButterKnife;

public abstract class BaseActivity extends AppCompatActivity {

    @Bind(R.id.toolbar) Toolbar toolbar;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.unbind(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_base, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // the default action is not good for me
                onBackPressed();
                return true;
            case R.id.email_me:
                // contact with author (sends some data about device)
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO,
                        Uri.fromParts("mailto", "koras@indywidualni.org", null));
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name) + ": " +
                        getString(R.string.email_author));
                emailIntent.putExtra(Intent.EXTRA_TEXT, "\n\n--" + Miscellany.getDeviceInfo(this));
                startActivity(Intent.createChooser(emailIntent,
                        getString(R.string.choose_email_client)));
                return true;
            case R.id.me_google_play:
                // author at Google Play
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/developer?id=Indywidualni")));
                return true;
            case R.id.github:
                // source code on GitHub
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://github.com/indywidualny/Centrum.fm")));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
