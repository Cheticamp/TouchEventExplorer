package com.example.toucheventexplorer.logger

import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.MotionEvent

/**
 * Log entries for the on-screen log.
 */
class EventLogEntry {

    // The text for a pure text entry.
    private var mText: String? = null

    // Action from the MotionEvent
    private var mAction = 0

    // Encoded pointer bitmap
    private var mPointerIds = 0

    // Name of the view which is considering the MotionEvent.
    private var mViewName: String? = null

    // Name of the method with the view identified by mViewName.
    private var mMethodName: String? = null

    // True if this entry relates to an earlier entry. This is used to help close out MotionEvents.
    private var mHasRelatedLogEntry = false

    // True if the method reported that it handled the event (returned true).
    private var mHandled = false

    // #RGB color to use to display the text in the log.
    private val mDisplayColor: Int

    // Log the start of a MotionEvent.
    constructor(motionEvent: MotionEvent, viewName: String, methodName: String, displayColor: Int) {
        mAction = motionEvent.actionMasked
        mPointerIds = encodePointers(motionEvent)
        mViewName = viewName
        mMethodName = methodName
        mDisplayColor = displayColor
        mHandled = false
        mHasRelatedLogEntry = false
    }

    // Called just before exit from event method to report whether the event was handled or not.
    constructor(relatedLogEntry: EventLogEntry, handled: Boolean) {
        mHandled = handled

        // Copy over what we need from the related log entry.
        mDisplayColor = relatedLogEntry.mDisplayColor
        mViewName = relatedLogEntry.mViewName
        mMethodName = relatedLogEntry.mMethodName
        mAction = relatedLogEntry.mAction
        mPointerIds = relatedLogEntry.mPointerIds
        mHasRelatedLogEntry = true
    }

    // Log straight text.
    constructor(text: String) {
        mText = text
        mDisplayColor = Color.BLACK
    }

    val actionText: String
        get() =
            if (mAction < MOTION_EVENT_NAMES.size) MOTION_EVENT_NAMES[mAction]
            else String.format("Unknown ACTION: %d", mAction)

    fun appendToText(s: String) {
        mText += s
    }

    // Return text in HTML format: <font color='#xxxxxx'>text</font><br/>
    val htmlText : java.lang.StringBuilder
        get() = logText.insert(0, "'>")
            .insert(0, String.format("%06X", mDisplayColor and 0xFFFFFF))
            .insert(0, "<font color='#")
            .append("</font><br/>")

    // Return text with color span.
    val spannableString
        get() =
            SpannableString(logText).apply {
                setSpan(
                    ForegroundColorSpan(mDisplayColor),
                    0, length, Spannable.SPAN_INCLUSIVE_INCLUSIVE
                )
            }

    // Build the display text from the log entry.
    private val logText
        get() =
            StringBuilder().apply {
                if (mText != null) {
                    append(mText)
                } else {
                    append(mViewName)
                        .append(": ")
                        .append(mMethodName)
                        .append(" $pointerIdText")
                        .append(" $actionText")
                    if (mHasRelatedLogEntry) {
                        append(" handled=").append(mHandled.toString())
                    }
                }
            }

    // Signature identifies this log entry for MotionEvent duplicate checking.
    val signature
        get() = "$mViewName$mMethodName$mAction$mPointerIds"

    // Encode pointer from the MotionEvent to a bit-mapped Int
    private fun encodePointers(motionEvent: MotionEvent): Int {
        var pointerIds = 0
        val action = motionEvent.actionMasked
        return if (action == MotionEvent.ACTION_POINTER_DOWN ||
            action == MotionEvent.ACTION_POINTER_UP
        ) {
            0x01 shl motionEvent.getPointerId(motionEvent.actionIndex)
        } else {
            val pointerCount = motionEvent.pointerCount
            for (p in 0 until pointerCount) {
                pointerIds = pointerIds or (0x01 shl motionEvent.getPointerId(p))
            }
            pointerIds
        }
    }

    // Decode pointer bitmap to string "[p1,p2,...]"
    private fun decodePointers(pointerBitmap: Int): String {
        var pointers = pointerBitmap
        val sb = StringBuilder()
        var count = 0
        while (pointers != 0) {
            if (pointers and 0x01 == 0x01) {
                sb.append(",").append(count)
            }
            pointers = pointers shr 1
            count++
        }
        return "[${sb.substring(1, sb.length)}]"
    }

    // Translate encoded pointer ids to String [px,py...]
    val pointerIdText
        get() = decodePointers(mPointerIds)

    companion object {
        private val MOTION_EVENT_NAMES = arrayOf(
            // First pointer contacts the screen.
            "ACTION_DOWN",
            // Last pointer leaves the screen.
            "ACTION_UP",
            // Pointer moves on screen. These may be batched, so more than one pointer may be identified.
            "ACTION_MOVE",
            // A parent has taken over touches and this view should cancel its involvement
            // in the gesture.
            "ACTION_CANCEL",
            // A movement has happened outside of the normal bounds of the UI element.
            "ACTION_OUTSIDE",
            // A second, third, etc. pointer has touched the screen.
            "ACTION_POINTER_DOWN",
            // A pointer has left the screen while at least one other pointer remains.
            "ACTION_POINTER_UP",
            // From API 14. A button has been pressed. This action is not a touch event so it is
            // delivered to View.onGenericMotionEvent(MotionEvent) rather than
            // View.onTouchEvent(MotionEvent). (Not part of this app.)
            "ACTION_HOVER_MOVE",
            // From API 12. This action is not a touch event so it is delivered to
            // View.onGenericMotionEvent(MotionEvent) rather than View.onTouchEvent(MotionEvent).
            // (Not part of this app.)
            "ACTION_SCROLL",
            // From API 14. A button has been pressed. This action is not a touch event so it is
            // delivered to View.onGenericMotionEvent(MotionEvent) rather than
            // View.onTouchEvent(MotionEvent). (Not part of this app.)
            "ACTION_HOVER_ENTER",
            // From API 14. A button has been pressed. This action is not a touch event so it is
            // delivered to View.onGenericMotionEvent(MotionEvent) rather than
            // View.onTouchEvent(MotionEvent). (Not part of this app.)
            "ACTION_HOVER_EXIT",
            // From API 23. A button has been pressed. This action is not a touch event so it is
            // delivered to View.onGenericMotionEvent(MotionEvent) rather than
            // View.onTouchEvent(MotionEvent). (Not part of this app.)
            "ACTION_BUTTON_PRESS",
            // A button has been released. his action is not a touch event so it is
            // delivered to View.onGenericMotionEvent(MotionEvent) rather than
            // View.onTouchEvent(MotionEvent). (Not part of thie app.)
            "ACTION_BUTTON_RELEASE"
        )
    }
}