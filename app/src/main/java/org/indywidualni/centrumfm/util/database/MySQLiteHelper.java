package org.indywidualni.centrumfm.util.database;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.indywidualni.centrumfm.MyApplication;

public class MySQLiteHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "centrum.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_NEWS = "News";
    public static final String COLUMN_NEWS_GUID = "guid";
    public static final String COLUMN_NEWS_LINK = "link";
    public static final String COLUMN_NEWS_TITLE = "title";
    public static final String COLUMN_NEWS_DATE = "pubDate";
    public static final String COLUMN_NEWS_DESCRIPTION = "description";
    public static final String COLUMN_NEWS_CATEGORY = "category";
    public static final String COLUMN_NEWS_ENCLOSURE = "enclosure";

    public static final String TABLE_SCHEDULE = "Schedule";
    public static final String TABLE_FAVOURITE = "Favourite";
    public static final String COLUMN_SCHEDULE_ID = "id";
    public static final String COLUMN_SCHEDULE_NAME = "name";
    public static final String COLUMN_SCHEDULE_BAND = "band";
    public static final String COLUMN_SCHEDULE_DAYS = "weekdays";
    public static final String COLUMN_SCHEDULE_DATE = "start";
    public static final String COLUMN_SCHEDULE_LENGTH = "length";

    public MySQLiteHelper() {
        super(MyApplication.getContextOfApplication(), DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        Log.v("SQLiteDatabase", "Creating database");
        database.execSQL("CREATE TABLE " + TABLE_NEWS + " (" +
                COLUMN_NEWS_GUID + " TEXT PRIMARY KEY, " +
                COLUMN_NEWS_LINK + " TEXT, " +
                COLUMN_NEWS_TITLE + " TEXT, " +
                COLUMN_NEWS_DATE + " TEXT, " +
                COLUMN_NEWS_DESCRIPTION + " TEXT, " +
                COLUMN_NEWS_CATEGORY + " TEXT, " +
                COLUMN_NEWS_ENCLOSURE + " TEXT" +
                ");");
        database.execSQL("CREATE TABLE " + TABLE_SCHEDULE + " (" +
                COLUMN_SCHEDULE_ID + " INTEGER PRIMARY KEY, " +
                COLUMN_SCHEDULE_NAME + " TEXT, " +
                COLUMN_SCHEDULE_BAND + " TEXT, " +
                COLUMN_SCHEDULE_DAYS + " TEXT, " +
                COLUMN_SCHEDULE_DATE + " TEXT, " +
                COLUMN_SCHEDULE_LENGTH + " INTEGER" +
                ");");
        database.execSQL("CREATE TABLE " + TABLE_FAVOURITE + " AS SELECT * FROM " +
                TABLE_SCHEDULE + ";");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(MySQLiteHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NEWS + ";");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SCHEDULE + ";");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FAVOURITE + ";");
        onCreate(db);
    }

}