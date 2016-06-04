package org.indywidualni.centrumfm.fragment;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.widget.DatePicker;

import java.util.Calendar;
import java.util.TimeZone;

public class DatePickerFragment extends DialogFragment
        implements DatePickerDialog.OnDateSetListener {

    public static String TAG_FRAGMENT = "date_picker";

    private static final int DATABASE_EARLIEST_YEAR = 2016;
    private static final int DATABASE_EARLIEST_MONTH = Calendar.MAY;
    private static final int DATABASE_EARLIEST_DAY = 23;

    private final Calendar c = Calendar.getInstance(TimeZone.getTimeZone("Europe/Warsaw"));
    private int year = c.get(Calendar.YEAR);
    private int month = c.get(Calendar.MONTH);
    private int day = c.get(Calendar.DAY_OF_MONTH);

    private OnDateSetSpecialListener listener;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        DatePickerDialog dialog = new DatePickerDialog(getActivity(), this, this.year, this.month, this.day);
        final Calendar startDate = (Calendar) c.clone();
        startDate.set(Calendar.YEAR, DATABASE_EARLIEST_YEAR);
        startDate.set(Calendar.MONTH, DATABASE_EARLIEST_MONTH);
        startDate.set(Calendar.DAY_OF_MONTH, DATABASE_EARLIEST_DAY);
        dialog.getDatePicker().setMinDate(startDate.getTimeInMillis());
        dialog.getDatePicker().setMaxDate(c.getTimeInMillis());
        dialog.setTitle("");
        return dialog;
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int day) {
        if (listener != null)
            listener.onDateSet(year, month, day, this.year, this.month, this.day);
    }

    public void setSpecialListener(OnDateSetSpecialListener listener) {
        this.listener = listener;
    }

    public interface OnDateSetSpecialListener {
        void onDateSet(int year, int month, int day, int currYear, int currMonth, int currDay);
    }

}