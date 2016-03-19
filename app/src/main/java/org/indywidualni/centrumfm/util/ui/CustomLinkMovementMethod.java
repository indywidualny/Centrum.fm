package org.indywidualni.centrumfm.util.ui;

import android.content.Context;
import android.text.Layout;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.util.Log;
import android.view.MotionEvent;

import org.indywidualni.centrumfm.activity.MainActivity;

public class CustomLinkMovementMethod extends LinkMovementMethod {

    private static CustomLinkMovementMethod linkMovementMethod = new CustomLinkMovementMethod();
    private static Context context;

    public boolean onTouchEvent(android.widget.TextView widget, android.text.Spannable buffer,
                                android.view.MotionEvent event) {
        int action = event.getAction();

        if (action == MotionEvent.ACTION_UP) {
            int x = (int) event.getX();
            int y = (int) event.getY();

            x -= widget.getTotalPaddingLeft();
            y -= widget.getTotalPaddingTop();

            x += widget.getScrollX();
            y += widget.getScrollY();

            Layout layout = widget.getLayout();
            int line = layout.getLineForVertical(y);
            int off = layout.getOffsetForHorizontal(line, x);

            URLSpan[] link = buffer.getSpans(off, off, URLSpan.class);

            if (link.length != 0) {
                String url = link[0].getURL();

                Log.d("Link", url);

                // open a custom tab
                if (context instanceof MainActivity)
                    ((MainActivity) context).openCustomTab(url);

                return true;
            }
        }

        return super.onTouchEvent(widget, buffer, event);
    }

    public static android.text.method.MovementMethod getInstance(Context handlingActivity) {
        context = handlingActivity;
        return linkMovementMethod;
    }

}