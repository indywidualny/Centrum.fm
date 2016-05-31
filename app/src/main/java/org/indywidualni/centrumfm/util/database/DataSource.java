package org.indywidualni.centrumfm.util.database;

import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;

import org.indywidualni.centrumfm.MyApplication;
import org.indywidualni.centrumfm.rest.model.Channel;
import org.indywidualni.centrumfm.rest.model.Schedule;
import org.indywidualni.centrumfm.rest.model.Song;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class DataSource {

    private static final String[] NEWS_COLUMNS = {
            MySQLiteHelper.COLUMN_NEWS_GUID, MySQLiteHelper.COLUMN_NEWS_LINK,
            MySQLiteHelper.COLUMN_NEWS_TITLE, MySQLiteHelper.COLUMN_NEWS_DATE,
            MySQLiteHelper.COLUMN_NEWS_DESCRIPTION, MySQLiteHelper.COLUMN_NEWS_CATEGORY,
            MySQLiteHelper.COLUMN_NEWS_ENCLOSURE
    };
    private static final String[] SCHEDULE_COLUMNS = {
            MySQLiteHelper.COLUMN_SCHEDULE_ID, MySQLiteHelper.COLUMN_SCHEDULE_NAME,
            MySQLiteHelper.COLUMN_SCHEDULE_BAND, MySQLiteHelper.COLUMN_SCHEDULE_DAYS,
            MySQLiteHelper.COLUMN_SCHEDULE_DATE, MySQLiteHelper.COLUMN_SCHEDULE_LENGTH
    };
    private static final String[] SONGS_COLUMNS = {
            MySQLiteHelper.COLUMN_SONGS_ID, MySQLiteHelper.COLUMN_SONGS_TITLE,
            MySQLiteHelper.COLUMN_SONGS_ARTIST, MySQLiteHelper.COLUMN_SONGS_DURATION
    };
    private static volatile DataSource instance;
    private SQLiteDatabase database;
    private Random random = new Random();
    private SharedPreferences preferences = PreferenceManager
            .getDefaultSharedPreferences(MyApplication.getContextOfApplication());

    private DataSource() {
        MySQLiteHelper dbHelper = new MySQLiteHelper();
        database = dbHelper.getWritableDatabase();
    }

    public static DataSource getInstance() {
        if (instance == null) {
            synchronized (DataSource.class) {
                if (instance == null)
                    instance = new DataSource();
            }
        }
        return instance;
    }

    public List<Channel.Item> getAllNews() {
        List<Channel.Item> allNews = new ArrayList<>();
        Cursor cursor = database.query(MySQLiteHelper.TABLE_NEWS,
                NEWS_COLUMNS, null, null, null, null, null);
        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            Channel.Item item = cursorToNews(cursor);
            allNews.add(item);
            cursor.moveToNext();
        }

        cursor.close();
        Collections.sort(allNews);
        return allNews;
    }

    private Channel.Item cursorToNews(Cursor cursor) {
        Channel.Item item = new Channel.Item();
        item.setGuid(cursor.getString(cursor.getColumnIndex(MySQLiteHelper.COLUMN_NEWS_GUID)));
        item.setLink(cursor.getString(cursor.getColumnIndex(MySQLiteHelper.COLUMN_NEWS_LINK)));
        item.setTitle(cursor.getString(cursor.getColumnIndex(MySQLiteHelper.COLUMN_NEWS_TITLE)));
        item.setDate(convertDate(cursor.getString(cursor.getColumnIndex(MySQLiteHelper
                .COLUMN_NEWS_DATE))));
        item.setDescription(cursor.getString(cursor.getColumnIndex(MySQLiteHelper
                .COLUMN_NEWS_DESCRIPTION)));
        item.setCategory(cursor.getString(cursor.getColumnIndex(MySQLiteHelper
                .COLUMN_NEWS_CATEGORY)));
        item.setEnclosureUrl(cursor.getString(cursor.getColumnIndex(MySQLiteHelper
                .COLUMN_NEWS_ENCLOSURE)));
        return item;
    }

    public void insertNews(List<Channel.Item> items) {
        for (Channel.Item item : items) {
            ContentValues cv = new ContentValues();
            cv.put(MySQLiteHelper.COLUMN_NEWS_GUID, item.getGuid());
            cv.put(MySQLiteHelper.COLUMN_NEWS_LINK, item.getLink());
            cv.put(MySQLiteHelper.COLUMN_NEWS_TITLE, item.getTitle());
            cv.put(MySQLiteHelper.COLUMN_NEWS_DATE, item.getPubDate());
            cv.put(MySQLiteHelper.COLUMN_NEWS_DESCRIPTION, item.getDescription());
            cv.put(MySQLiteHelper.COLUMN_NEWS_CATEGORY, categoryChooser(item.getCategories()));
            cv.put(MySQLiteHelper.COLUMN_NEWS_ENCLOSURE, item.getEnclosureUrl());
            database.insertWithOnConflict(MySQLiteHelper.TABLE_NEWS, null, cv,
                    SQLiteDatabase.CONFLICT_IGNORE);
        }
        trimNews();
    }

    private void trimNews() {
        int maxNews = 10;
        try {  // just in case
            maxNews = Integer.parseInt(preferences.getString("news_keep_max", "10"));
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        // delete the oldest rows leaving the latest maxNews rows
        database.execSQL("DELETE FROM " + MySQLiteHelper.TABLE_NEWS + " WHERE ROWID IN (" +
                "SELECT ROWID FROM " + MySQLiteHelper.TABLE_NEWS +
                " ORDER BY ROWID DESC LIMIT -1 OFFSET " + maxNews + ");");
    }

    /**
     * Get rid of all the strings starting with the lower case, they're just tags.
     * Pick a random category then.
     */
    private String categoryChooser(List<String> list) {
        List<String> categories = new ArrayList<>();
        for (String s : list)
            if (!Character.isLowerCase(s.charAt(0)))
                categories.add(s);
        return categories.get(random.nextInt(categories.size()));
    }

    private Date convertDate(String dateString) {
        DateFormat df = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.US);
        Date date = new Date();
        try {
            date = df.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    public void insertSchedule(List<Schedule.Event> items) {
        database.delete(MySQLiteHelper.TABLE_SCHEDULE, null, null);
        for (Schedule.Event item : items)
            insertSingleEvent(item, MySQLiteHelper.TABLE_SCHEDULE);
    }

    public void insertFavourite(Schedule.Event item) {
        insertSingleEvent(item, MySQLiteHelper.TABLE_FAVOURITE);
    }

    private void insertSingleEvent(Schedule.Event item, String table) {
        ContentValues cv = new ContentValues();
        cv.put(MySQLiteHelper.COLUMN_SCHEDULE_ID, item.getId());
        cv.put(MySQLiteHelper.COLUMN_SCHEDULE_NAME, item.getName());
        cv.put(MySQLiteHelper.COLUMN_SCHEDULE_BAND, item.getBand());
        cv.put(MySQLiteHelper.COLUMN_SCHEDULE_DAYS, item.getWeekdays());
        cv.put(MySQLiteHelper.COLUMN_SCHEDULE_DATE, item.getStartDate());
        cv.put(MySQLiteHelper.COLUMN_SCHEDULE_LENGTH, item.getEventLength());
        database.insertWithOnConflict(table, null, cv, SQLiteDatabase.CONFLICT_ROLLBACK);
    }

    public List<Schedule.Event> getScheduleNormal() {
        return getSchedule(MySQLiteHelper.TABLE_SCHEDULE);
    }

    public List<Schedule.Event> getScheduleFavourite() {
        return getSchedule(MySQLiteHelper.TABLE_FAVOURITE);
    }

    private List<Schedule.Event> getSchedule(String tableName) {
        List<Schedule.Event> schedule = new ArrayList<>();
        Cursor cursor = database.query(tableName,
                SCHEDULE_COLUMNS, null, null, null, null, null);
        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            Schedule.Event item = cursorToSchedule(cursor);
            schedule.add(item);
            cursor.moveToNext();
        }

        cursor.close();
        return schedule;
    }

    private Schedule.Event cursorToSchedule(Cursor cursor) {
        Schedule.Event item = new Schedule.Event();
        item.setId(cursor.getInt(cursor.getColumnIndex(MySQLiteHelper.COLUMN_SCHEDULE_ID)));
        item.setName(cursor.getString(cursor.getColumnIndex(MySQLiteHelper.COLUMN_SCHEDULE_NAME)));
        item.setBand(cursor.getString(cursor.getColumnIndex(MySQLiteHelper.COLUMN_SCHEDULE_BAND)));
        item.setWeekdays(cursor.getString(cursor.getColumnIndex(MySQLiteHelper
                .COLUMN_SCHEDULE_DAYS)));
        item.setStartDate(cursor.getString(cursor.getColumnIndex(MySQLiteHelper
                .COLUMN_SCHEDULE_DATE)));
        item.setEventLength(cursor.getInt(cursor.getColumnIndex(MySQLiteHelper
                .COLUMN_SCHEDULE_LENGTH)));
        item.setFavourite(isEventFavourite(item.getId()));
        return item;
    }

    public boolean isEventFavourite(int id) {
        Cursor cursor = database.rawQuery("SELECT " + MySQLiteHelper.COLUMN_SCHEDULE_ID + " FROM " +
                MySQLiteHelper.TABLE_FAVOURITE + " WHERE " + MySQLiteHelper.COLUMN_SCHEDULE_ID +
                "=?", new String[]{String.valueOf(id)});
        boolean isFavourite = cursor.getCount() > 0;
        cursor.close();
        return isFavourite;
    }

    public boolean removeFavourite(int id) {
        String whereClause = MySQLiteHelper.COLUMN_SCHEDULE_ID + "=?";
        String[] whereArgs = new String[]{String.valueOf(id)};
        return database.delete(MySQLiteHelper.TABLE_FAVOURITE, whereClause, whereArgs) > 0;
    }

    public void insertFavouriteSong(Song item) {
        ContentValues cv = new ContentValues();
        cv.put(MySQLiteHelper.COLUMN_SONGS_TITLE, item.getTitle());
        cv.put(MySQLiteHelper.COLUMN_SONGS_ARTIST, item.getArtist());
        cv.put(MySQLiteHelper.COLUMN_SONGS_DURATION, item.getDuration());
        database.insertWithOnConflict(MySQLiteHelper.TABLE_FAVOURITE_SONGS, null, cv,
                SQLiteDatabase.CONFLICT_REPLACE);
    }

    public boolean removeFavouriteSong(int id) {
        String whereClause = MySQLiteHelper.COLUMN_SONGS_ID + "=?";
        String[] whereArgs = new String[]{String.valueOf(id)};
        return database.delete(MySQLiteHelper.TABLE_FAVOURITE_SONGS, whereClause, whereArgs) > 0;
    }

    public List<Song> getFavouriteSongs() {
        List<Song> songs = new ArrayList<>();
        Cursor cursor = database.query(MySQLiteHelper.TABLE_FAVOURITE_SONGS,
                SONGS_COLUMNS, null, null, null, null, null);
        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {

            Song item = cursorToGetFavouriteSongs(cursor);
            songs.add(item);
            cursor.moveToNext();
        }

        cursor.close();
        return songs;
    }

    private Song cursorToGetFavouriteSongs(Cursor cursor) {
        Song item = new Song();
        item.setDbId(cursor.getInt(cursor.getColumnIndex(MySQLiteHelper.COLUMN_SONGS_ID)));
        item.setTitle(cursor.getString(cursor.getColumnIndex(MySQLiteHelper.COLUMN_SONGS_TITLE)));
        item.setArtist(cursor.getString(cursor.getColumnIndex(MySQLiteHelper.COLUMN_SONGS_ARTIST)));
        item.setDuration(cursor.getString(cursor.getColumnIndex(MySQLiteHelper
                .COLUMN_SONGS_DURATION)));
        return item;
    }

}
