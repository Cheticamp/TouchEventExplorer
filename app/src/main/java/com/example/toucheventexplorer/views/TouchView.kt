package com.example.toucheventexplorer.views

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import androidx.appcompat.widget.AppCompatTextView
import com.example.toucheventexplorer.R
import com.example.toucheventexplorer.controllers.TouchController
import com.example.toucheventexplorer.logger.EventLogEntry
import com.example.toucheventexplorer.logger.EventLogger
import com.example.toucheventexplorer.logger.EventMethods
import javax.annotation.Nonnull

class TouchView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr), OnTouchListener {
    private var mViewName: String = text.toString()
    private var mLogColor = 0
    private lateinit var mLogger: EventLogger
    private lateinit var mTouchController: TouchController
    private val mViewType = TouchController.VIEW_TYPE_VIEW

    init {
        val a = context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.TouchView,
            0, 0
        )
        mLogColor = try {
            @Suppress("DEPRECATION")
            a.getColor(
                R.styleable.TouchView_logColor,
                context.resources.getColor(android.R.color.black)
            )
        } finally {
            a.recycle()
        }
        setOnTouchListener(this)
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        var logEntry = EventLogEntry(
            event,
            mViewName,
            EventMethods.dispatchTouchEvent,
            mLogColor
        )
        mLogger.logEvent(logEntry)
        val handled =
            mTouchController.isHandled(
                true,
                mViewType,
                TouchController.METHOD_DISPATCH_TOUCH_EVENT,
                event
            )
                || super.dispatchTouchEvent(event)
                || mTouchController.isHandled(
                false,
                mViewType,
                TouchController.METHOD_DISPATCH_TOUCH_EVENT,
                event
            )
        logEntry = EventLogEntry(logEntry, handled)
        mLogger.logEvent(logEntry)
        return handled
    }

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        var logEntry =
            EventLogEntry(event, mViewName, EventMethods.onTouch, mLogColor)
        mLogger.logEvent(logEntry)
        val handled =
            mTouchController.isHandled(true, mViewType, TouchController.METHOD_ON_TOUCH, event)
        logEntry = EventLogEntry(logEntry, handled)
        mLogger.logEvent(logEntry)
        return handled
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        var logEntry = EventLogEntry(
            event, mViewName, EventMethods.onTouchEvent, mLogColor
        )
        mLogger.logEvent(logEntry)
        val handled =
            mTouchController.isHandled(
                true,
                mViewType,
                TouchController.METHOD_ON_TOUCH_EVENT,
                event
            )
                || super.onTouchEvent(event)
        logEntry = EventLogEntry(
            logEntry, handled
        )
        mLogger.logEvent(logEntry)
        return handled
    }

    fun setTouchController(@Nonnull controller: TouchController) {
        mTouchController = controller
    }

    fun setLogger(logger: EventLogger) {
        mLogger = logger
    }
}