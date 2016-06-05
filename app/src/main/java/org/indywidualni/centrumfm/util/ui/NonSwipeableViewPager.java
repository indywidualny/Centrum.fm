package org.indywidualni.centrumfm.util.ui;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class NonSwipeableViewPager extends ViewPager {

    private boolean swipeEnabled = true;

    public NonSwipeableViewPager(Context context) {
        super(context);
    }

    public NonSwipeableViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        return swipeEnabled && super.onInterceptTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return swipeEnabled && super.onTouchEvent(event);
    }

    public void setSwipeEnabled(boolean swipeEnabled) {
        this.swipeEnabled = swipeEnabled;
    }

}