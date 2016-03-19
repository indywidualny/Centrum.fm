package org.indywidualni.centrumfm.util;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.indywidualni.centrumfm.AlarmReceiver;
import org.indywidualni.centrumfm.MyApplication;
import org.indywidualni.centrumfm.fragment.SettingsFragment;
import org.indywidualni.centrumfm.rest.model.Schedule;
import org.indywidualni.centrumfm.util.database.DataSource;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public abstract class AlarmHelper {

    private static Context context = MyApplication.getContextOfApplication();
    public static final String EVENT_TITLE = "event_title";
    public static final String TIME_TO_START = "time_to_start";

    private static SharedPreferences preferences = PreferenceManager
            .getDefaultSharedPreferences(context);

    public static boolean isEnabled() {
        return Integer.parseInt(preferences.getString("reminders_mode",
                SettingsFragment.DEFAULT_REMINDER_OFFSET)) != 0;
    }

    public static void setAlarms(Schedule.Event event) {
        int offset = Integer.parseInt(preferences.getString("reminders_mode",
                SettingsFragment.DEFAULT_REMINDER_OFFSET));
      
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        
        List<Integer> weekdays = getWeekdays(event);

        for (int i = 0; i < weekdays.size(); ++i) {
            long time = generateEventDateMillis(event.getStartDate(), weekdays.get(i));

            PendingIntent alarmIntent = generatePendingIntent(event, weekdays.get(i));

            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, time + offset,
                AlarmManager.INTERVAL_DAY * 7, alarmIntent);
        }
    }

    public static void cancelAlarms(Schedule.Event event) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        List<Integer> weekdays = getWeekdays(event);

        for (int i = 0; i < weekdays.size(); ++i) {
            PendingIntent pendingIntent = generatePendingIntent(event, weekdays.get(i));
            alarmManager.cancel(pendingIntent);
        }
    }

    public static void setAllAlarms() {
        List<Schedule.Event> events = obtainFavourites();
        for (Schedule.Event event : events) {
            setAlarms(event);
        }
    }

    public static void cancelAllAlarms() {
        List<Schedule.Event> events = obtainFavourites();
        for (Schedule.Event event : events) {
            cancelAlarms(event);
        }
    }
    
    private static List<Schedule.Event> obtainFavourites() {
        return DataSource.getInstance().getScheduleFavourite();
    }

    private static PendingIntent generatePendingIntent(Schedule.Event event, int uniqueWeekday) {
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra(EVENT_TITLE, event.getName());
        intent.putExtra(TIME_TO_START, preferences.getString("reminders_mode",
                SettingsFragment.DEFAULT_REMINDER_OFFSET));
        int uniqueId = event.getId() * 10 + uniqueWeekday;
        return PendingIntent.getBroadcast(context, uniqueId, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private static List<Integer> getWeekdays(Schedule.Event event) {
        List<Integer> weekdays = new ArrayList<>();

        for (int i = 0; i < 7; ++i)
            if (event.getWeekdays().contains(Integer.toString(i)))
                weekdays.add(i + 1);

        return weekdays;
    }
    
    private static long generateEventDateMillis(String time, int weekday) {
        String[] timeSplit = time.split(":");
        Calendar date = Calendar.getInstance();
        date.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timeSplit[0]));
        date.set(Calendar.MINUTE, Integer.parseInt(timeSplit[1]));
        date.set(Calendar.SECOND, Integer.parseInt(timeSplit[2]));
        date.set(Calendar.MILLISECOND, 0);
        date.set(Calendar.DAY_OF_WEEK, weekday);

        Calendar nowWithOffset = Calendar.getInstance();
        int offset = Integer.parseInt(preferences.getString("reminders_mode",
                SettingsFragment.DEFAULT_REMINDER_OFFSET));
        nowWithOffset.add(Calendar.MILLISECOND, offset);

        if (date.before(nowWithOffset))
            date.add(Calendar.WEEK_OF_YEAR, 1);

        return date.getTimeInMillis();
    }

}
