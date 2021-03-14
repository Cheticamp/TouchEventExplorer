package com.example.toucheventexplorer.controllers

import android.view.MotionEvent
import com.example.toucheventexplorer.logger.EventLogger

/**
 * This class can inspect a MotionEvent to determine if it should be reported as handled or not
 * depending upon ad hoc criteria to be coded here.
 *
 * Called from TouchController.
 */
class AdHocControl(logger: EventLogger) {
    @Suppress("unused")
    private var mLogger = logger

    /**
     * Determine if the handled status for a MotionEvent should be set true or false depending
     * upon an ad hoc set of criteria.
     *
     * @param beforeSuper       true if call is prior to super call; false if after super call
     * @param viewType          Type of view as described in TouchController (VIEW_TYPE_*)
     * @param method            One of three methods that handle MotionEvents; dispatchTouchevent(),
     * interceptTouchEvent(), onTouch() (listener) and onTouchEvent().
     * @param event             The MotionEvent.
     * @param handledOverridden True if handled status of method handling event is overridden
     * to "true."
     * @return null if this method will not overridden processing. true if the event should be
     * reported as "handled." False if the event should be reported as "not handled."
     */
    @Suppress("UNUSED_PARAMETER")
    fun isHandledOverridden(
        beforeSuper: Boolean,
        viewType: Int, method: Int,
        event: MotionEvent,
        handledOverridden: Boolean
    ): Boolean? {
        return null
    }

    /**
     * Reset any internal state set for ad hoc processing.
     */
    fun newGesture() {} /*
    Example: What happens if we handle all events up to and including the first move event but
    start reporting events as "not handled" after that?

    The following code will help answer this question focusing on the onTouchEvent method of
    of the View.
    */
}