package com.example.toucheventexplorer;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.MotionEvent;

import androidx.annotation.NonNull;

/**
 * Log entries for the on-screen log.
 */

class EventLogEntry {
    private String mText;
    private int mAction;
    private int mPointerIds;
    private String mViewName;
    private String mMethodName;
    private boolean mHasRelatedLogEntry;
    private boolean mHandled;
    private final int mDisplayColor;

    private static final String MOTION_EVENT_NAMES[] = {

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
        "ACTION_BUTTON_RELEASE",
    };

    // Log the start of a MotionEvent.
    EventLogEntry(@NonNull MotionEvent motionEvent, @NonNull String viewName,
                  @NonNull String methodName, int displayColor) {

        mAction = motionEvent.getActionMasked();
        if (mAction == MotionEvent.ACTION_MOVE) {
            int pointerCount = motionEvent.getPointerCount();
            for (int p = 0; p < pointerCount; p++) {
                mPointerIds |= 0x01 << motionEvent.getPointerId(p);
            }
        } else {
            mPointerIds = 0x01 << motionEvent.getPointerId(motionEvent.getActionIndex());
        }
        mViewName = viewName;
        mMethodName = methodName;
        mDisplayColor = displayColor;

        mHandled = false;
        mHasRelatedLogEntry = false;
    }

    // Called just before exit from event method to report whether the event was handled or not.
    EventLogEntry(@NonNull EventLogEntry relatedLogEntry, boolean handled) {
        mHandled = handled;

        // Copy over what we need from the related log entry.
        mDisplayColor = relatedLogEntry.mDisplayColor;
        mViewName = relatedLogEntry.mViewName;
        mMethodName = relatedLogEntry.mMethodName;
        mAction = relatedLogEntry.mAction;
        mPointerIds = relatedLogEntry.mPointerIds;
        mHasRelatedLogEntry = true;
    }

    // Log straight text.
    EventLogEntry(@NonNull String text) {
        mText = text;
        mDisplayColor = Color.BLACK;
    }

    @SuppressLint("DefaultLocale")
    String getActionText() {
        return (mAction < MOTION_EVENT_NAMES.length)
            ? MOTION_EVENT_NAMES[mAction] :
            String.format("Unknown ACTION: %d", mAction);
    }

    @SuppressWarnings("SameParameterValue")
    void appendToText(@NonNull String s) {
        mText += s;
    }

    // Return text in HTML format: <font color='#xxxxxx'>text</font><br/>
    String getHtmlText() {
        return getText().insert(0, "'>")
            .insert(0, String.format("%06X", mDisplayColor & 0xFFFFFF))
            .insert(0, "<font color='#")
            .append("</font><br/>").toString();
    }

    // Return text with color span.
    SpannableString getSpannableString() {
        SpannableString ss = new SpannableString(getText());
        ss.setSpan(new ForegroundColorSpan(mDisplayColor),
                   0, ss.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        return ss;
    }

    // Build the display text from the log entry.
    private StringBuilder getText() {
        StringBuilder sb = new StringBuilder();

        if (mText != null) {
            sb.append(mText);
        } else {
            sb.append(mViewName).append(": ").append(mMethodName);
            if (mHasRelatedLogEntry) {
                sb.append(" handled=").append(String.valueOf(mHandled));
            }
        }

        return sb;
    }

    String getHashText() {
        return mViewName + mMethodName + mAction + mPointerIds;
    }

    // Translate encoded pointer ids to String x,y...
    String getPointerIdText() {
        StringBuilder sb = new StringBuilder();
        int pointers = mPointerIds;
        int count = 0;

        while (pointers != 0) {
            if ((pointers & 0x01) == 0x01) {
                sb.append(",").append(count);
            }
            pointers >>= 1;
            count++;
        }
        return sb.substring(1, sb.length());
    }
}
