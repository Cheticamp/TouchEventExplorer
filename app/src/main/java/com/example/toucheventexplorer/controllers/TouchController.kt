package com.example.toucheventexplorer.controllers

import android.view.MotionEvent
import androidx.appcompat.widget.SwitchCompat
import com.example.toucheventexplorer.logger.EventLogger
import javax.annotation.Nonnull

class TouchController(
    @param:Nonnull private val mIsHandledSwitches: Array<SwitchCompat>,
    mLogger: EventLogger
) {
    private val mAdHocControl: AdHocControl = AdHocControl(mLogger)
    fun isHandled(
        beforeSuper: Boolean,
        viewType: Int,
        method: Int,
        @Nonnull event: MotionEvent
    ): Boolean {
        val isHandled = mIsHandledSwitches[viewType * TOUCH_METHOD_COUNT + method].isChecked
        val adHocOverride =
            mAdHocControl.isHandledOverridden(beforeSuper, viewType, method, event, isHandled)
        return adHocOverride ?:  mIsHandledSwitches[viewType * TOUCH_METHOD_COUNT + method].isChecked
    }

    fun newGesture() {
        mAdHocControl.newGesture()
    }

    fun resetAllSwitches() {
        for (sw in mIsHandledSwitches) {
            sw.isChecked = false
        }
    }

    fun getAllSwitches(): BooleanArray {
        val switches = BooleanArray(mIsHandledSwitches.size)
        for (i in switches.indices) {
            switches[i] = mIsHandledSwitches[i].isChecked
        }
        return switches
    }

    fun restoreSwitches(@Nonnull switches: BooleanArray) {
        for (i in switches.indices) {
            mIsHandledSwitches[i].isChecked = switches[i]
        }
    }

    companion object {
        const val VIEW_TYPE_ACTIVITY = 0
        const val VIEW_TYPE_VIEWGROUP_A = 1
        const val VIEW_TYPE_VIEWGROUP_B = 2
        const val VIEW_TYPE_VIEW = 3

        const val METHOD_DISPATCH_TOUCH_EVENT = 0
        const val METHOD_ON_INTERCEPT_TOUCH_EVENT = 1
        const val METHOD_ON_TOUCH = 2
        const val METHOD_ON_TOUCH_EVENT = 3
        private const val TOUCH_METHOD_COUNT = 4
    }
}