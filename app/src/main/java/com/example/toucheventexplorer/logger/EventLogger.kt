package com.example.toucheventexplorer.logger

import androidx.recyclerview.widget.RecyclerView
import com.example.toucheventexplorer.utility.Util
import java.util.*
import javax.annotation.Nonnull

class EventLogger(@param:Nonnull private val mOnScreenLog: RecyclerView) {
    private lateinit var mLog: MutableList<EventLogEntry>

    // Log entries are staged here before being transferred to the primary log (dup check.)
    private val mLogStaged: MutableList<EventLogEntry> = ArrayList()
    private var mDuplicateCount = 0
    private var mDigest = ""
    private var mLastDigest = ""

    val log: List<EventLogEntry>
        get() = mLog

    val logEntriesString: String
        get() {
            val sb = StringBuilder()
            if (mLog.size == 0) return ""
            for (logEntry in mLog) {
                sb.append(logEntry.htmlText)
            }
            return sb.toString()
        }

    fun logEvent(@Nonnull eventLogEntry: EventLogEntry) {
        mLogStaged.add(eventLogEntry)
        Util.hash("$mDigest${eventLogEntry.signature}")?.apply {
            mDigest = this
        }
    }

    fun onEndOfMotionEvent() {
        val pointers = mLogStaged[0].pointerIdText
        if (mDigest == mLastDigest) {
            ++mDuplicateCount
            logDuplicates(mLogStaged[0].actionText, pointers)
        } else {
            mDuplicateCount = 0
            if (mLog.size != 0) {
                mLog.add(EventLogEntry(""))
            }
            mLog.add(
                EventLogEntry("=====> ${mLogStaged[0].actionText} $pointers")
            )
            mLog.addAll(mLogStaged)
            mOnScreenLog.adapter?.notifyItemRangeInserted(
                mLog.size - mLogStaged.size,
                mLog.size - 1
            )
        }
        mOnScreenLog.scrollToPosition(mLog.size - 1)
        mLastDigest = mDigest
        mDigest = ""
        mLogStaged.clear()
    }

    private fun logDuplicates(action: String, pointers: String) {
        if (mDuplicateCount == 1) {
            mLog.add(EventLogEntry(""))
            mLog.add(EventLogEntry("$action $pointers Duplicates"))
            mLog.add(EventLogEntry("."))
            mOnScreenLog.adapter?.notifyItemRangeInserted(mLog.size - 2, 2)
        } else {
            mLog[mLog.size - 1].appendToText(" .")
            mOnScreenLog.adapter?.notifyItemChanged(mLog.size - 1)
        }
        mOnScreenLog.scrollToPosition(mLog.size - 1)
    }

    fun clearLog() {
        mLog.clear()
        mLogStaged.clear()
        mOnScreenLog.adapter?.notifyDataSetChanged()
    }

    fun setEventLog(log: MutableList<EventLogEntry>) {
        mLog = log
    }
}