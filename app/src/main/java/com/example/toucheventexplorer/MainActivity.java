package com.example.toucheventexplorer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Switch;

import com.example.toucheventexplorer.databinding.ActivityMainBinding;
import com.example.toucheventexplorer.databinding.SwitchLayoutIncludeBinding;

import javax.annotation.Nonnull;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.transition.AutoTransition;
import androidx.transition.Transition;
import androidx.transition.TransitionListenerAdapter;
import androidx.transition.TransitionManager;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding mBinding;

    // true to clear log at the start of a gesture (ACTION_DOWN)
    private boolean mClearOnGestureStart;

    // True if we are inside a gesture.
    private boolean mInGesture;

    // Determines overrides on touch flow - interfaces with the switches.
    private TouchController mTouchController;

    // On-screen log control.
    private EventLogger mLogger;

    // Bottom of the on-screen log.
    private int mLogBottom;

    // Switches of the switch area on the screen.
    private Switch[] mIsHandledSwitches;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        mLogger = new EventLogger(mBinding.onScreenLog);
        mBinding.viewGroupA.setLogger(mLogger);
        mBinding.viewGroupB.setLogger(mLogger);
        mBinding.textView.setLogger(mLogger);

        // Switches that override whether an event is reported as handled or not.
        SwitchLayoutIncludeBinding switchTable = mBinding.switchTable;
        mIsHandledSwitches = new Switch[]{
            switchTable.activityDispatch,
            new Switch(this), // Activity doesn't have OnInterceptTouchEvent, so dummy one up.
            new Switch(this), // Activity doesn't have a listener, so dummy one up.
            switchTable.activityOnTouchEvent,

            switchTable.groupADispatch,
            switchTable.groupAIntercept,
            switchTable.groupAOnTouch,
            switchTable.groupAOnTouchEvent,

            switchTable.groupBDispatch,
            switchTable.groupBIntercept,
            switchTable.groupBOnTouch,
            switchTable.groupBOnTouchEvent,

            switchTable.viewDispatch,
            new Switch(this), // Straight Views don't have OnInterceptTouchEvent, so dummy one up.
            switchTable.viewOnTouch,
            switchTable.viewOnTouchEvent};

        // TouchController does the actual inquiry of the switch table.
        mTouchController = new TouchController(mIsHandledSwitches, mLogger);
        mBinding.viewGroupA.setTouchController(mTouchController);
        mBinding.viewGroupB.setTouchController(mTouchController);
        mBinding.textView.setTouchController(mTouchController);

        mBinding.onScreenLog.setLayoutManager(new LinearLayoutManager(this));
        OnScreenLogAdapter adapter = new OnScreenLogAdapter(mLogger.getLog());
        adapter.setHasStableIds(true);
        mBinding.onScreenLog.setAdapter(adapter);

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        mClearOnGestureStart = sharedPref.getBoolean(CLEAR_ON_GESTURE_START, true);

        mBinding.constraintLayout.getViewTreeObserver()
            .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    int[] logLocation = new int[2];
                    mBinding.onScreenLog.getLocationOnScreen(logLocation);
                    mLogBottom = logLocation[1] + mBinding.onScreenLog.getHeight();
                    // mLogBottom comes up as zero if the layout doesn't display immediately
                    // (e.g., "recents" pressed as app is starting and before display). So, if
                    // mLogBottom == 0, wait for the layout to really complete before
                    // removing the listener.
                    if (mLogBottom != 0) {
                        mBinding.constraintLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                }
            });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        menu.findItem(R.id.clearOnGestureStart).setChecked(mClearOnGestureStart);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.resize_log:
                resizeOnScreenLog();
                return true;

            case R.id.clearOnGestureStart:
                mClearOnGestureStart = !item.isChecked();
                item.setChecked(mClearOnGestureStart);
                SharedPreferences.Editor sharedEditor =
                    PreferenceManager.getDefaultSharedPreferences(this).edit();
                sharedEditor.putBoolean(CLEAR_ON_GESTURE_START, mClearOnGestureStart).apply();
                return true;

            case R.id.clear_log:
                mLogger.clearLog();
                return true;

            case R.id.send_log:
                sendLog();
                return true;

            case R.id.reset_switches:
                resetAllSwitches(null);
                return true;

            case R.id.help:
                Intent intent = new Intent(this, HelpActivity.class);
                intent.putExtra(HelpActivity.HELP_FILE, HELP_FILE);
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        boolean[] savedSwitches = savedInstanceState.getBooleanArray(SWITCHES_KEY);
        if (savedSwitches != null) {
            restoreSwitches(savedSwitches);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        savedInstanceState.putBooleanArray(SWITCHES_KEY, saveSwitches());
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        boolean handled;

        // We are only tracking touches in the lower part of the screen. If the touch point in
        // above that area, ignore it.
        if (isEventToBeLogged(event)) {
            if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                if (mClearOnGestureStart) {
                    mLogger.clearLog();
                }
            }
            EventLogEntry logEntry =
                new EventLogEntry(event, "Activity", "dispatchTouchEvent", ACTIVITY_LOG_COLOR);
            mLogger.logEvent(logEntry);
            handled = mTouchController.isHandled(TouchController.VIEW_TYPE_ACTIVITY,
                                                 TouchController.METHOD_DISPATCH_TOUCH_EVENT,
                                                 event)
                || super.dispatchTouchEvent(event);
            mLogger.logEvent(new EventLogEntry(logEntry, handled));

            // Wrap up this MotionEvent.
            mLogger.onEndOfMotionEvent();

            // Check for gesture end.
            if (event.getActionMasked() == MotionEvent.ACTION_UP
                || event.getActionMasked() == MotionEvent.ACTION_CANCEL) {
                mInGesture = false;
            }
        } else {
            handled = super.dispatchTouchEvent(event);
        }
        return handled;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean handled;

        if (mInGesture) {
            EventLogEntry logEntry =
                new EventLogEntry(event, "Activity", "onTouchEvent", ACTIVITY_LOG_COLOR);
            mLogger.logEvent(logEntry);
            handled = mTouchController.isHandled(TouchController.VIEW_TYPE_ACTIVITY,
                                                 TouchController.METHOD_ON_TOUCH_EVENT, event)
                || super.onTouchEvent(event);
            logEntry = new EventLogEntry(logEntry, handled);
            mLogger.logEvent(logEntry);
        } else {
            handled = super.onTouchEvent(event);
        }
        return handled;
    }

    private boolean isEventToBeLogged(@NonNull MotionEvent event) {

        // getY() is relative to the top of the screen, Compare it to the absolute
        // bottom of the log to check if the touch is in the allowed area.
        if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
            mInGesture = (event.getY() > mLogBottom)
                && (mBinding.viewGroupA.getVisibility() == View.VISIBLE);
            if (mInGesture) {
                mTouchController.newGesture();
            }
        }
        return mInGesture;
    }

    private void resizeOnScreenLog() {

        // End any in-process transitions in case of overzealous clicking that
        // will leave the screen in a curious state.
        TransitionManager.endTransitions(mBinding.constraintLayout);

        // Make sure we are not scrolling or we may crash during animation with error
        // java.lang.IllegalArgumentException: Scrapped or attached views may not be recycled.
        mBinding.onScreenLog.stopScroll();

        AutoTransition transition = new AutoTransition();

        // Kill all interactions with the on-screen log until transition animation is done.
        // If we don't, odd crashes happen with the recycling of view or adding of views
        // (java.lang.IllegalArgumentException: Scrapped or attached views may not be recycled).
        // This just avoids the whole mess. May be related to:
        // https://github.com/airbnb/epoxy/issues/405
        transition.addListener(new TransitionListenerAdapter() {
            @Override
            public void onTransitionStart(@NonNull Transition transition) {
                mBinding.onScreenLog.setOnTouchListener((v, event) -> true);
            }

            @Override
            public void onTransitionEnd(@NonNull Transition transition) {
                mBinding.onScreenLog.setOnTouchListener(null);
            }

            @Override
            public void onTransitionCancel(@NonNull Transition transition) {
                mBinding.onScreenLog.setOnTouchListener(null);
            }
        });

        TransitionManager.beginDelayedTransition(mBinding.constraintLayout, transition);
        if (mBinding.viewGroupA.getVisibility() == View.VISIBLE) {
            mBinding.switchTable.switchConstraintLayout.setVisibility(View.GONE);
            mBinding.viewGroupA.setVisibility(View.GONE);
            mBinding.activityLabel.setVisibility(View.GONE);
        } else {
            mBinding.switchTable.switchConstraintLayout.setVisibility(View.VISIBLE);
            mBinding.viewGroupA.setVisibility(View.VISIBLE);
            mBinding.activityLabel.setVisibility(View.VISIBLE);
        }
    }

    private void sendLog() {
        Intent shareIntent = new Intent();

        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.setType("text/*");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            shareIntent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"text/plain", "text/html"});
        }
        shareIntent.putExtra(Intent.EXTRA_TITLE, getString(R.string.sent_log_title));
        String logString = mLogger.getLogEntriesString();

        // Write out the log with HTML formatting.
        shareIntent.putExtra(Intent.EXTRA_HTML_TEXT, logString);

        // Strip out the formatting for straight text.
        shareIntent.putExtra(Intent.EXTRA_TEXT, Html.fromHtml(logString).toString());

        startActivity(Intent.createChooser(shareIntent, getString(R.string.send_log_to)));
    }

    @SuppressWarnings({"SameParameterValue", "unused"})
    private void resetAllSwitches(@Nullable View view) {
        for (Switch sw : mIsHandledSwitches) {
            sw.setChecked(false);
        }
    }

    private boolean[] saveSwitches() {
        boolean[] switches = new boolean[mIsHandledSwitches.length];

        for (int i = 0; i < switches.length; i++) {
            switches[i] = mIsHandledSwitches[i].isChecked();
        }
        return switches;
    }

    private void restoreSwitches(@Nonnull boolean[] switches) {
        for (int i = 0; i < switches.length; i++) {
            mIsHandledSwitches[i].setChecked(switches[i]);
        }
    }

    @SuppressWarnings("unused")
    private static final String TAG = "MainActivity";
    private static final int ACTIVITY_LOG_COLOR = Color.BLACK;
    private static final String HELP_FILE = "file:///android_asset/help_pages/help.html";
    private static final String CLEAR_ON_GESTURE_START = "clear_on_gesture_start";
    private static final String SWITCHES_KEY = "switches";
}