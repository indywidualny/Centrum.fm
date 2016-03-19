package org.indywidualni.centrumfm.util.ui;

import android.content.Context;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;

import java.lang.reflect.Field;

public class MarqueeToolbar extends Toolbar {

    private static final String TAG = MarqueeToolbar.class.getSimpleName();
    private TextView title;

    public MarqueeToolbar(Context context) {
        super(context);
    }

    public MarqueeToolbar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MarqueeToolbar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setTitle(CharSequence title) {
        if (!reflected) {
            reflected = reflectTitle();
        }
        super.setTitle(title);
        selectTitle();
    }

    @Override
    public void setTitle(int resId) {
        if (!reflected) {
            reflected = reflectTitle();
        }
        super.setTitle(resId);
        selectTitle();
    }

    boolean reflected = false;
    private boolean reflectTitle() {
        try {
            Field field = Toolbar.class.getDeclaredField("mTitleTextView");
            field.setAccessible(true);
            title = (TextView) field.get(this);
            title.setEllipsize(TextUtils.TruncateAt.MARQUEE);
            title.setMarqueeRepeatLimit(-1);
            return true;
        } catch (NoSuchFieldException e) {
            Log.e(TAG, "" + e.getMessage());
            return false;
        } catch (IllegalAccessException e) {
            Log.e(TAG, "" + e.getMessage());
            return false;
        } catch (NullPointerException e) {
            Log.e(TAG, "" + e.getMessage());
            return false;
        }
    }

    public void selectTitle() {
        if (title != null)
            title.setSelected(true);
    }
    
}