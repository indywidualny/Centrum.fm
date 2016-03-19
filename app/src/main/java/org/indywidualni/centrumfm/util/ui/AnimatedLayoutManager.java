package org.indywidualni.centrumfm.util.ui;

import android.app.Activity;
import android.support.v7.widget.LinearLayoutManager;

public class AnimatedLayoutManager extends LinearLayoutManager {

    public AnimatedLayoutManager(Activity activity) {
        super(activity);
    }

    @Override
    public boolean supportsPredictiveItemAnimations() {
        return true;
    }

}
