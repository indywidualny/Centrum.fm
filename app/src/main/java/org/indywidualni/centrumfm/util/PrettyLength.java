package org.indywidualni.centrumfm.util;

import android.content.Context;

import org.indywidualni.centrumfm.MyApplication;
import org.indywidualni.centrumfm.R;

public abstract class PrettyLength {

    public static String get(int hours) {
        Context context = MyApplication.getContextOfApplication();
        if (hours == 1)  // one hour
            return context.getResources().getQuantityString(R.plurals.eventPrettyLength, hours);
        else if (hours > 1 && hours < 5)  // 2 to 4
            return context.getResources().getQuantityString(R.plurals.eventPrettyLength, hours, hours);
        return context.getResources().getQuantityString(R.plurals.eventPrettyLength, hours, hours);
    }

}
