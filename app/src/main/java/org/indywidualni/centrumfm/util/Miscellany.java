package org.indywidualni.centrumfm.util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.Toast;

import org.indywidualni.centrumfm.MyApplication;
import org.indywidualni.centrumfm.R;
import org.indywidualni.centrumfm.rest.model.Rds;
import org.indywidualni.centrumfm.rest.model.Song;
import org.indywidualni.centrumfm.util.database.AsyncWrapper;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public abstract class Miscellany {

    public static int dpToPx(int dp) {
        final float scale = MyApplication.getContextOfApplication().getResources()
                .getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    public static String readFromAssets(String filename) {
        String result = "";
        try {
            result = convertStreamToString(MyApplication.getContextOfApplication()
                    .getAssets().open(filename));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    private static String convertStreamToString(java.io.InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }
    
    public static String convertMillisToHuman(long total) {
        total /= 1000;
        int minutes = (int) (total % 3600) / 60;
        int seconds = (int) total % 60;
        return minutes + ":" + (seconds < 10 ? "0" + seconds : seconds);
    }

    public static String constructDateQueryForDay(boolean dayStart) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        Date today = Calendar.getInstance(TimeZone.getTimeZone("Europe/Warsaw")).getTime();
        String reportDate = df.format(today);
        return dayStart ? reportDate + "T00:00:00" : reportDate + "T23:59:59";
    }
    
    public static String constructDateQueryForDay(boolean dayStart,
                                                  int year, int month, int day) {
        @SuppressLint("DefaultLocale") String reportDate = year + "-" +
                String.format("%02d", month + 1) + "-" + String.format("%02d", day);
        return dayStart ? reportDate + "T00:00:00" : reportDate + "T23:59:59";
    }
    
    public static void addFavouriteSongFromRds(Context context, List<Rds> rds) {
        String error = context.getString(R.string.cannot_favourite_song);
        if (rds == null)
            Toast.makeText(context, error, Toast.LENGTH_SHORT).show();
        else {
            Rds now = rds.get(0);
            if (!now.getTitle().isEmpty() && !now.getArtist().isEmpty()) {
                Song song = new Song();
                song.setTitle(now.getTitle());
                song.setArtist(now.getArtist());
                song.setDuration(now.getTotal());
                AsyncWrapper.insertFavouriteSong(song);
                String message = "\u2605 " + now.getArtist() + " – " + now.getTitle();
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
            } else
                Toast.makeText(context, error, Toast.LENGTH_SHORT).show();
        }
    }

    public static String formatSubtitleSave(Context context, String subtitle) {
        return subtitle == null ? null : context.getString(R.string.songs_given_day,
                subtitle.substring(0, 10));
    }

    public static String formatSubtitleRead(Context context, String subtitle) {
        return subtitle == null ? context.getString(R.string.songs_current_day) : subtitle;
    }

    // get some information about the device (needed for e-mail signature)
    public static String getDeviceInfo(Activity activity) {
        StringBuilder sb = new StringBuilder();

        try {
            PackageInfo pInfo = activity.getPackageManager().getPackageInfo(
                    activity.getPackageName(), PackageManager.GET_META_DATA);
            sb.append("\nApp Package Name: ").append(activity.getPackageName());
            sb.append("\nApp Version Name: ").append(pInfo.versionName);
            sb.append("\nApp Version Code: ").append(pInfo.versionCode);
        } catch (PackageManager.NameNotFoundException ex) {
            Log.e("Misc: getDeviceInfo", "" + ex.getMessage());
        }

        sb.append("\nOS Version: ").append(System.getProperty("os.version")).append(" (")
                .append(android.os.Build.VERSION.RELEASE).append(")");
        sb.append("\nOS API Level: ").append(android.os.Build.VERSION.SDK_INT);
        sb.append("\nDevice: ").append(android.os.Build.DEVICE);
        sb.append("\nModel: ").append(android.os.Build.MODEL);
        sb.append("\nManufacturer: ").append(android.os.Build.MANUFACTURER);

        DisplayMetrics metrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);

        sb.append("\nScreen: ").append(metrics.heightPixels).append(" x ")
                .append(metrics.widthPixels);
        sb.append("\nLocale: ").append(Locale.getDefault().toString());

        return sb.toString();
    }

}
