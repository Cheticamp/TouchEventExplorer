package com.example.toucheventexplorer;

import android.util.Log;
import android.view.MotionEvent;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

class EventLogger {
    private final List<EventLogEntry> mLog = new ArrayList<>();

    private final RecyclerView mOnScreenLog;

    // Log entries are staged here before being transferred to the primary log (dup check.)
    private final List<EventLogEntry> mLogStaged = new ArrayList<>();
    private int mDuplicateCount;
    private String mDigest = "";
    private String mLastDigest = "";

    EventLogger(@Nonnull RecyclerView onScreenLog) {
        mOnScreenLog = onScreenLog;
    }

    void onEndOfMotionEvent() {
        String pointers = mLogStaged.get(0).getPointerIdText();

        if (mDigest.equals(mLastDigest)) {
            ++mDuplicateCount;
            logDuplicates(mLogStaged.get(0).getActionText(), pointers);
        } else {
            mDuplicateCount = 0;
            if (mLog.size() != 0) {
                mLog.add(new EventLogEntry(""));
            }
            mLog.add(new EventLogEntry("=====> " + mLogStaged.get(0).getActionText() + " (" + pointers + ")"));
            mLog.addAll(mLogStaged);
            mOnScreenLog.getAdapter().notifyItemRangeInserted(mLog.size() - mLogStaged.size(), mLog.size() - 1);
        }
        mOnScreenLog.scrollToPosition(mLog.size() - 1);
        mLastDigest = mDigest;
        mDigest = "";
        mLogStaged.clear();
    }

    private void logDuplicates(@NonNull String action, @NonNull String pointers) {
        if (mDuplicateCount == 1) {
            mLog.add(new EventLogEntry(""));
            mLog.add(new EventLogEntry(action + " (" + pointers + ")" + " Duplicates"));
            mLog.add(new EventLogEntry("."));
            mOnScreenLog.getAdapter().notifyItemRangeInserted(mLog.size() - 2, 2);
        } else {
            mLog.get(mLog.size() - 1).appendToText(" .");
            mOnScreenLog.getAdapter().notifyItemChanged(mLog.size() - 1);
        }
        mOnScreenLog.scrollToPosition(mLog.size() - 1);
    }

    void clearLog() {
        mLog.clear();
        mLogStaged.clear();
        //noinspection ConstantConditions
        mOnScreenLog.getAdapter().notifyDataSetChanged();
    }

    void logEvent(@Nonnull EventLogEntry eventLogEntry) {
        mLogStaged.add(eventLogEntry);
        String toHash = eventLogEntry.getHashText();
        if (toHash != null) {
            mDigest = Util.hash(mDigest + toHash);
        }
    }

    List<EventLogEntry> getLog() {
        return mLog;
    }

    String getLogEntriesString() {
        StringBuilder sb = new StringBuilder();

        if (mLog.size() == 0) {
            return "";
        }

        for (EventLogEntry logEntry : mLog) {
            sb.append(logEntry.getHtmlText());
        }

        return sb.toString();
    }

    // This method will place the history of a move event into logcat .
    @SuppressWarnings("unused")
    void logHistory(@Nonnull MotionEvent event) {
        final int historySize = event.getHistorySize();
        final int pointerCount = event.getPointerCount();

        Log.d(TAG, String.format("Start history log... %d historical entries", historySize));
        for (int h = 0; h < historySize; h++) {
            Log.d(TAG, String.format("At time %d:", event.getHistoricalEventTime(h)));
            for (int p = 0; p < pointerCount; p++) {
                Log.d(TAG, String.format("    pointer %d: (%d,%d)", event.getPointerId(p),
                                         Math.round(event.getHistoricalX(p, h)),
                                         Math.round(event.getHistoricalY(p, h))));
            }
        }
        Log.d(TAG, String.format("At time %d:", event.getEventTime()));
        for (int p = 0; p < pointerCount; p++) {
            Log.d(TAG, String.format("    pointer %d: (%d,%d)",
                                     event.getPointerId(p), Math.round(event.getX(p)),
                                     Math.round(event.getY(p))));
        }
        Log.d(TAG, "End history log");
    }

    private static final String TAG = "EventLogger";
}
