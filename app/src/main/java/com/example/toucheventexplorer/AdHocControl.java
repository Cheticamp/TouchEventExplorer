package com.example.toucheventexplorer;

import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * This class can inspect a MotionEvent to determine if it should be reported as handled or not
 * depending upon ad hoc criteria to be coded here.
 * <p>
 * Called from TouchController.
 */
@SuppressWarnings("unused")
class AdHocControl {
    @SuppressWarnings("FieldCanBeLocal")
    private final EventLogger mLogger;

    AdHocControl(@NonNull EventLogger logger) {
        mLogger = logger;
    }

    /**
     * Determine if the handled status for a MotionEvent should be set true or false depending
     * upon an ad hoc set of criteria.
     *
     * @param viewType          Type of view as described in TouchController (VIEW_TYPE_*)
     * @param method            One of three methods that handle MotionEvents; dispatchTouchevent(),
     *                          interceptTouchEvent(), onTouch() (listener) and onTouchEvent().
     * @param event             The MotionEvent.
     * @param handledOverridden True if handled status of method handling event is overridden
     *                          to "true."
     * @return null if this method will not overridden processing. true if the event should be
     * reported as "handled." False if the event should be reported as "not handled."
     */
    @SuppressWarnings("SameReturnValue")
    @Nullable
    Boolean isHandledOverridden(int viewType, int method, @NonNull MotionEvent event,
                                boolean handledOverridden) {
        return null;
    }

    /**
     * Reset any internal state set for ad hoc processing.
     */
    @SuppressWarnings("EmptyMethod")
    void newGesture() {
    }

    /*
    Example: What happens if we handle all events up to and including the first move event but
    start reporting events as "not handled" after that?

    The following code will help answer this question focusing on the onTouchEvent method of
    of the View.
    */

//    Boolean isHandled(int viewType, int method, @NonNull MotionEvent event,
//                                boolean handledOverridden) {
//        Log.d("override", String.format("<<<<ad hoc= %d %d %d", viewType, method, event.getActionMasked()));
//        if (viewType == TouchController.VIEW_TYPE_VIEW
//            && method == TouchController.METHOD_ON_TOUCH_EVENT) {
//            if (mMoveAccepted) {
//                EventLogEntry logEntry = new EventLogEntry("************* Ad hoc override...");
//                mLogger.logEvent(logEntry);
//                return false;
//            }
//            if (event.getActionMasked() == MotionEvent.ACTION_MOVE) {
//                mMoveAccepted = true;
//            }
//            return true;
//        }
//        return null;
//    }
//
//    void newGesture() {
//        mMoveAccepted = false;
//    }
}
