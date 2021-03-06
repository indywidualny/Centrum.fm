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

import butterknife.BindView;

public abstract class BaseActivity extends AppCompatActivity {

    @BindView(R.id.toolbar) Toolbar toolbar;

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
                onBackPressed();
                return true;
            case R.id.email_me:
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto",
                        getString(R.string.author_email_address), null));
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name) + ": " +
                        getString(R.string.email_author));
                emailIntent.putExtra(Intent.EXTRA_TEXT, "\n\n--" + Miscellany.getDeviceInfo(this));
                startActivity(Intent.createChooser(emailIntent, getString(R.string.choose_email_client)));
                return true;
            case R.id.me_google_play:
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.google_play_url))));
                return true;
            case R.id.github:
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.github_url))));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
