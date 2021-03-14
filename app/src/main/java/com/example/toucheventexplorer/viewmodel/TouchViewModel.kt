package com.example.toucheventexplorer.viewmodel

import androidx.lifecycle.ViewModel
import com.example.toucheventexplorer.logger.EventLogEntry

class TouchViewModel : ViewModel() {
    private val mEventLog: MutableList<EventLogEntry> = ArrayList()

    val eventLog
        get() = mEventLog
}