package com.example.toucheventexplorer;

import android.view.MotionEvent;
import android.widget.Switch;

import javax.annotation.Nonnull;

import androidx.annotation.NonNull;

class TouchController {
    private final Switch[] mIsHandledSwitches;
    private final AdHocControl mAdHocControl;
    @SuppressWarnings("FieldCanBeLocal")
    private final EventLogger mLogger;

    TouchController(@Nonnull Switch[] handledSwitches, @NonNull EventLogger logger) {
        mIsHandledSwitches = handledSwitches;
        mLogger = logger;
        mAdHocControl = new AdHocControl(mLogger);
    }

    @SuppressWarnings("unused")
    boolean isHandled(int viewType, int method, @Nonnull MotionEvent event) {
        boolean isHandled =
            mIsHandledSwitches[viewType * TOUCH_METHOD_COUNT + method].isChecked();
        Boolean adHocOverride =
            mAdHocControl.isHandledOverridden(viewType, method, event, isHandled);
        return (adHocOverride != null) ? adHocOverride : isHandled;
    }

    @SuppressWarnings({"EmptyMethod", "unused"})
    void newGesture() {
        mAdHocControl.newGesture();
    }

    static final int VIEW_TYPE_ACTIVITY = 0;
    static final int VIEW_TYPE_VIEWGROUP_A = 1;
    static final int VIEW_TYPE_VIEWGROUP_B = 2;
    static final int VIEW_TYPE_VIEW = 3;

    static final int METHOD_DISPATCH_TOUCH_EVENT = 0;
    static final int METHOD_ON_INTERCEPT_TOUCH_EVENT = 1;
    static final int METHOD_ON_TOUCH = 2;
    static final int METHOD_ON_TOUCH_EVENT = 3;

    private static final int TOUCH_METHOD_COUNT = 4;
}
