package com.example.toucheventexplorer;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import javax.annotation.Nonnull;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;

public class TouchView extends AppCompatTextView implements View.OnTouchListener {
    private String mViewName;
    private int mLogColor;
    private EventLogger mLogger;
    private TouchController mTouchController;
    private final int mViewType = TouchController.VIEW_TYPE_VIEW;

    public TouchView(Context context) {
        super(context);
        init(context, null);
    }

    public TouchView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public TouchView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, @Nullable AttributeSet attrs) {
        mViewName = getText().toString();

        TypedArray a = context.getTheme().obtainStyledAttributes(
            attrs,
            R.styleable.TouchView,
            0, 0);

        try {
            mLogColor = a.getColor(R.styleable.TouchView_logColor,
                                   context.getResources().getColor(android.R.color.black));
        } finally {
            a.recycle();
        }

        setOnTouchListener(this);
    }

    @SuppressWarnings("UnnecessaryLocalVariable")
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        EventLogEntry logEntry = new EventLogEntry(event, mViewName,
                                                   "dispatchTouchEvent",
                                                   mLogColor);
        mLogger.logEvent(logEntry);
        boolean handled = mTouchController
            .isHandled(mViewType, TouchController.METHOD_DISPATCH_TOUCH_EVENT, event)
            || super.dispatchTouchEvent(event);
        logEntry = new EventLogEntry(logEntry, handled);
        mLogger.logEvent(logEntry);

        return handled;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        EventLogEntry logEntry = new EventLogEntry(event, mViewName, "onTouch", mLogColor);
        mLogger.logEvent(logEntry);
        boolean handled =
            mTouchController.isHandled(mViewType, TouchController.METHOD_ON_TOUCH, event);
        logEntry = new EventLogEntry(logEntry, handled);
        mLogger.logEvent(logEntry);

        return handled;
    }

    @SuppressWarnings("UnnecessaryLocalVariable")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        EventLogEntry logEntry = new EventLogEntry(event, mViewName, "onTouchEvent", mLogColor);
        mLogger.logEvent(logEntry);
        boolean handled =
            mTouchController.isHandled(mViewType, TouchController.METHOD_ON_TOUCH_EVENT, event)
                || super.onTouchEvent(event);
        logEntry = new EventLogEntry(logEntry, handled);
        mLogger.logEvent(logEntry);

        return handled;
    }

    public void setTouchController(@Nonnull TouchController controller) {
        mTouchController = controller;
    }

    void setLogger(EventLogger logger) {
        mLogger = logger;
    }
}
