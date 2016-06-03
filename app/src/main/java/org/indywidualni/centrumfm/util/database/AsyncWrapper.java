package org.indywidualni.centrumfm.util.database;

import android.os.AsyncTask;

import org.indywidualni.centrumfm.rest.model.Schedule;
import org.indywidualni.centrumfm.rest.model.Song;
import org.indywidualni.centrumfm.util.AlarmHelper;

import java.util.List;

public abstract class AsyncWrapper {

    public static void insertSchedule(final List<Schedule.Event> eventList) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... arg0) {
                DataSource.getInstance().insertSchedule(eventList);
                return null;
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public static void removeFavourite(final Schedule.Event event) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... arg0) {
                DataSource.getInstance().removeFavourite(event.getId());
                if (AlarmHelper.isEnabled())
                    AlarmHelper.cancelAlarms(event);
                return null;
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public static void insertFavourite(final Schedule.Event event) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... arg0) {
                DataSource.getInstance().insertFavourite(event);
                if (AlarmHelper.isEnabled())
                    AlarmHelper.setAlarms(event);
                return null;
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
    
    public static void insertFavouriteSong(final Song song) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... arg0) {
                DataSource.getInstance().insertFavouriteSong(song);
                return null;
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public static void removeFavouriteSong(final Song song) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... arg0) {
                DataSource.getInstance().removeFavouriteSong(song.getDbId());
                return null;
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

}
