package com.example.toucheventexplorer;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import javax.annotation.Nonnull;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.constraintlayout.widget.ConstraintLayout;

@SuppressWarnings("UnnecessaryLocalVariable")
public class TouchViewGroup extends ConstraintLayout implements View.OnTouchListener {
    private AppCompatTextView mGroupTitle;
    private String mGroupName;
    private TouchController mTouchController;
    private int mLogColor;
    private EventLogger mLogger;
    private int mViewType;

    public TouchViewGroup(Context context) {
        super(context);
        init(context, null);
    }

    public TouchViewGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public TouchViewGroup(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, @Nullable AttributeSet attrs) {
        TypedArray a = context.getTheme().obtainStyledAttributes(
            attrs,
            R.styleable.TouchViewGroup,
            0, 0);

        try {
            mGroupName = a.getString(R.styleable.TouchViewGroup_groupName);
            mLogColor = a.getColor(R.styleable.TouchViewGroup_logColor,
                                   context.getResources().getColor(android.R.color.black));
        } finally {
            a.recycle();
        }

        mGroupTitle = new AppCompatTextView(this.getContext(), null, R.style.AppTheme_ViewLabelStyle);
        mGroupTitle.setTextAppearance(context, R.style.AppTheme_ViewLabelStyle);
        mGroupTitle.setText(mGroupName);
        switch ((String) getTag()) {
            case "A":
                mViewType = TouchController.VIEW_TYPE_VIEWGROUP_A;
                break;
            case "B":
                mViewType = TouchController.VIEW_TYPE_VIEWGROUP_B;
                break;
            default:
                throw new IllegalStateException("ViewGroup tag must be either 'A' or 'B'");
        }

        setOnTouchListener(this);
    }

    @Override
    public void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        int widthSpec = View.MeasureSpec.makeMeasureSpec(getWidth(), MeasureSpec.AT_MOST);
        int heightSpec = View.MeasureSpec.makeMeasureSpec(getHeight(), MeasureSpec.AT_MOST);
        mGroupTitle.measure(widthSpec, heightSpec);
        mGroupTitle.layout(0, 0, getWidth(), getHeight());
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        EventLogEntry logEntry =
            new EventLogEntry(event, mGroupName, "dispatchTouchEvent", mLogColor);
        mLogger.logEvent(logEntry);
        boolean handled =
            mTouchController
                .isHandled(mViewType, TouchController.METHOD_DISPATCH_TOUCH_EVENT, event)
                || super.dispatchTouchEvent(event);
        logEntry = new EventLogEntry(logEntry, handled);
        mLogger.logEvent(logEntry);

        return handled;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        EventLogEntry logEntry =
            new EventLogEntry(event, mGroupName, "onInterceptTouchEvent", mLogColor);
        mLogger.logEvent(logEntry);
        boolean handled = mTouchController
            .isHandled(mViewType, TouchController.METHOD_ON_INTERCEPT_TOUCH_EVENT, event)
            || super.onTouchEvent(event);
        logEntry = new EventLogEntry(logEntry, handled);
        mLogger.logEvent(logEntry);

        return handled;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        EventLogEntry logEntry =
            new EventLogEntry(event, mGroupName, "onTouch", mLogColor);
        mLogger.logEvent(logEntry);
        boolean handled =
            mTouchController.isHandled(mViewType, TouchController.METHOD_ON_TOUCH, event);
        logEntry = new EventLogEntry(logEntry, handled);
        mLogger.logEvent(logEntry);

        return handled;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        EventLogEntry logEntry =
            new EventLogEntry(event, mGroupName, "onTouchEvent", mLogColor);
        mLogger.logEvent(logEntry);
        boolean handled = mTouchController
            .isHandled(mViewType, TouchController.METHOD_ON_TOUCH_EVENT, event)
            || super.onTouchEvent(event);
        logEntry = new EventLogEntry(logEntry, handled);
        mLogger.logEvent(logEntry);

        return handled;
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.save();
        canvas.translate(16, 16);
        mGroupTitle.draw(canvas);
        canvas.restore();
    }

    public void setTouchController(@Nonnull TouchController controller) {
        mTouchController = controller;
    }

    void setLogger(EventLogger logger) {
        mLogger = logger;
    }
}
