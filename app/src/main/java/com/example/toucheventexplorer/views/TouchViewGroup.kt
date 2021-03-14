package com.example.toucheventexplorer.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.text.TextPaint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.toucheventexplorer.R
import com.example.toucheventexplorer.controllers.TouchController
import com.example.toucheventexplorer.logger.EventLogEntry
import com.example.toucheventexplorer.logger.EventLogger
import com.example.toucheventexplorer.logger.EventMethods
import javax.annotation.Nonnull

class TouchViewGroup @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr), OnTouchListener {
    private var mGroupName: String = "Unspecified"
    private lateinit var mTouchController: TouchController
    private var mLogColor = 0
    private lateinit var mLogger: EventLogger
    private var mViewType = 0
    private val mTextPaint = TextPaint().apply {
        isAntiAlias = true
        color = Color.BLACK
        textSize = context.resources.displayMetrics.density * 32
    }
    private val mLabelMargin = context.resources.displayMetrics.density * LABEL_MARGIN_DP
    private val mLabelVerticalShift by lazy {
        -mTextPaint.fontMetrics.ascent + mLabelMargin
    }

    init {
        val a = context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.TouchViewGroup,
            0, 0
        )
        try {
            a.getString(R.styleable.TouchViewGroup_groupName)?.apply {
                mGroupName = this
            }
            @Suppress("DEPRECATION")
            mLogColor = a.getColor(
                R.styleable.TouchViewGroup_logColor,
                context.resources.getColor(android.R.color.black)
            )
        } finally {
            a.recycle()
        }

        mTextPaint.textSize = context.resources.getDimension(R.dimen.view_label_size)
        mViewType = when (tag as String) {
            "A" -> TouchController.VIEW_TYPE_VIEWGROUP_A
            "B" -> TouchController.VIEW_TYPE_VIEWGROUP_B
            else -> throw IllegalStateException("ViewGroup tag must be either 'A' or 'B'")
        }
        setOnTouchListener(this)
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        var logEntry =
            EventLogEntry(
                event,
                mGroupName,
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

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        var logEntry =
            EventLogEntry(event, mGroupName, EventMethods.onInterceptTouchEvent, mLogColor)
        mLogger.logEvent(logEntry)
        val handled =
            mTouchController.isHandled(
                true,
                mViewType,
                TouchController.METHOD_ON_INTERCEPT_TOUCH_EVENT,
                event
            )
                || super.onInterceptTouchEvent(event)
                || mTouchController.isHandled(
                false,
                mViewType,
                TouchController.METHOD_ON_INTERCEPT_TOUCH_EVENT,
                event
            )
        logEntry = EventLogEntry(logEntry, handled)
        mLogger.logEvent(logEntry)
        return handled
    }

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        var logEntry =
            EventLogEntry(event, mGroupName, EventMethods.onTouch, mLogColor)
        mLogger.logEvent(logEntry)
        val handled =
            mTouchController.isHandled(true, mViewType, TouchController.METHOD_ON_TOUCH, event)
        logEntry = EventLogEntry(logEntry, handled)
        mLogger.logEvent(logEntry)
        return handled
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        var logEntry =
            EventLogEntry(event, mGroupName, EventMethods.onTouchEvent, mLogColor)
        mLogger.logEvent(logEntry)
        val handled = super.onTouchEvent(event) ||
            mTouchController.isHandled(
                true,
                mViewType,
                TouchController.METHOD_ON_TOUCH_EVENT,
                event
            )
        logEntry = EventLogEntry(logEntry, handled)
        mLogger.logEvent(logEntry)
        return handled
    }

    public override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.save()
        canvas.translate(mLabelMargin, mLabelVerticalShift)
        canvas.drawText(mGroupName, 0f, 0f, mTextPaint)
        canvas.restore()
    }

    fun setTouchController(@Nonnull controller: TouchController) {
        mTouchController = controller
    }

    fun setLogger(logger: EventLogger) {
        mLogger = logger
    }

    companion object {
        private const val LABEL_MARGIN_DP = 8 // 8dp
    }
}