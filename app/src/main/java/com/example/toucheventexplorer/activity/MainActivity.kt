package com.example.toucheventexplorer.activity

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.view.doOnNextLayout
import androidx.databinding.DataBindingUtil
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.transition.AutoTransition
import androidx.transition.Transition
import androidx.transition.TransitionListenerAdapter
import androidx.transition.TransitionManager
import com.example.toucheventexplorer.R
import com.example.toucheventexplorer.controllers.TouchController
import com.example.toucheventexplorer.databinding.ActivityMainBinding
import com.example.toucheventexplorer.logger.EventLogEntry
import com.example.toucheventexplorer.logger.EventLogger
import com.example.toucheventexplorer.logger.EventMethods
import com.example.toucheventexplorer.logger.OnScreenLogAdapter
import com.example.toucheventexplorer.viewmodel.TouchViewModel
import com.example.toucheventexplorer.viewmodel.TouchViewModelFactory

class MainActivity : AppCompatActivity() {
    private lateinit var mBinding: ActivityMainBinding
    private val mTouchViewModel by viewModels<TouchViewModel> {
        TouchViewModelFactory()
    }

    // true to clear log at the start of a gesture (ACTION_DOWN)
    private var mClearOnGestureStart = false

    // True if View should be child of ViewGroup A instead of ViewGroup B
    private var mViewChildOfA = false

    // True if we are inside a gesture.
    private var mInGesture = false

    // Determines overrides on touch flow - interface to the switches.
    private lateinit var mTouchController: TouchController

    // On-screen log control.
    private lateinit var mLogger: EventLogger

    // Bottom of the on-screen log.
    private var mLogBottom = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        mLogger = EventLogger(mBinding.onScreenLog)
        mLogger.setEventLog(mTouchViewModel.eventLog)
        with(mBinding) {
            viewGroupA.setLogger(mLogger)
            viewGroupB.setLogger(mLogger)
            viewChildOfGroupA.setLogger(mLogger)
            viewChildOfGroupB.setLogger(mLogger)
        }

        // Switches that override whether an event is reported as handled or not.
        mBinding.switchTable.run {
            arrayOf(
                activityDispatch,
                SwitchCompat(this@MainActivity),  // Activity doesn't have OnInterceptTouchEvent, so dummy one up.
                SwitchCompat(this@MainActivity),  // Activity doesn't have a listener, so dummy one up.
                activityOnTouchEvent,
                groupADispatch,
                groupAIntercept,
                groupAOnTouch,
                groupAOnTouchEvent,
                groupBDispatch,
                groupBIntercept,
                groupBOnTouch,
                groupBOnTouchEvent,
                viewDispatch,
                SwitchCompat(this@MainActivity),  // Straight Views don't have OnInterceptTouchEvent, so dummy one up.
                viewOnTouch,
                viewOnTouchEvent
            )
        }.let {
            // TouchController does the actual inquiry of the switch table
            mTouchController = TouchController(it, mLogger)
        }

        with(mBinding) {
            viewGroupA.setTouchController(mTouchController)
            viewGroupB.setTouchController(mTouchController)
            viewChildOfGroupA.setTouchController(mTouchController)
            viewChildOfGroupB.setTouchController(mTouchController)
            onScreenLog.layoutManager = LinearLayoutManager(this@MainActivity)
        }
        val adapter = OnScreenLogAdapter(mLogger.log)
        adapter.setHasStableIds(true)
        mBinding.onScreenLog.adapter = adapter

        val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        mClearOnGestureStart = sharedPref.getBoolean(CLEAR_ON_GESTURE_START, true)
        mViewChildOfA = sharedPref.getBoolean(VIEW_CHILD_OF_GROUP_A, false)

        setViewParent(mViewChildOfA)
        mBinding.constraintLayout.doOnNextLayout {
            val logLocation = IntArray(2)
            mBinding.onScreenLog.getLocationOnScreen(logLocation)
            mLogBottom = logLocation[1] + mBinding.onScreenLog.height
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        menu.findItem(R.id.clearOnGestureStart).isChecked = mClearOnGestureStart
        menu.findItem(R.id.makeViewChildOfGroupA).isChecked = mViewChildOfA
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.resize_log -> resizeOnScreenLog()

            R.id.clearOnGestureStart -> {
                mClearOnGestureStart = !item.isChecked
                item.isChecked = mClearOnGestureStart
                val sharedEditor = PreferenceManager.getDefaultSharedPreferences(this).edit()
                sharedEditor.putBoolean(CLEAR_ON_GESTURE_START, mClearOnGestureStart).apply()
            }

            R.id.makeViewChildOfGroupA -> {
                mViewChildOfA = !item.isChecked
                item.isChecked = mViewChildOfA
                setViewParent(mViewChildOfA)
                val sharedEditor = PreferenceManager.getDefaultSharedPreferences(this).edit()
                sharedEditor.putBoolean(VIEW_CHILD_OF_GROUP_A, mViewChildOfA).apply()
            }

            R.id.clear_log -> mLogger.clearLog()

            R.id.send_log -> sendLog()

            R.id.reset_switches -> mTouchController.resetAllSwitches()

            R.id.help -> {
                val intent = Intent(this, HelpActivity::class.java)
                intent.putExtra(HelpActivity.HELP_FILE, HELP_FILE)
                startActivity(intent)
            }

            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    public override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        savedInstanceState.getBooleanArray(SWITCHES_KEY)
            ?.let { mTouchController.restoreSwitches(it) }
    }

    public override fun onSaveInstanceState(savedInstanceState: Bundle) {
        super.onSaveInstanceState(savedInstanceState)
        savedInstanceState.putBooleanArray(SWITCHES_KEY, mTouchController.getAllSwitches())
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        // We are only tracking touches in the lower part of the screen. If the touch point in
        // above that area, ignore it.
        if (!isEventToBeLogged(event)) {
            return super.dispatchTouchEvent(event)
        }
        if (event.actionMasked == MotionEvent.ACTION_DOWN) {
            if (mClearOnGestureStart) mLogger.clearLog()
            if (mInGesture) mTouchController.newGesture()
        }
        val logEntry =
            EventLogEntry(event, ACTIVITY, EventMethods.dispatchTouchEvent, ACTIVITY_LOG_COLOR)
        mLogger.logEvent(logEntry)

        // Call the super. If true is returned, we are done else force true if the corresponding
        // switch specifies it.
        val handled: Boolean =
            mTouchController.isHandled(
                true,
                TouchController.VIEW_TYPE_ACTIVITY,
                TouchController.METHOD_DISPATCH_TOUCH_EVENT,
                event
            )
                || super.dispatchTouchEvent(event)
                || mTouchController.isHandled(
                false,
                TouchController.VIEW_TYPE_ACTIVITY,
                TouchController.METHOD_DISPATCH_TOUCH_EVENT,
                event
            )


        // Close out this portion of the event.
        mLogger.logEvent(EventLogEntry(logEntry, handled))

        // Wrap up the entire MotionEvent.
        mLogger.onEndOfMotionEvent()

        // Check for gesture end.
        if (event.actionMasked == MotionEvent.ACTION_UP
            || event.actionMasked == MotionEvent.ACTION_CANCEL
        ) {
            mInGesture = false
        }
        return handled
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!mInGesture) {
            return super.onTouchEvent(event)
        }

        val logEntry =
            EventLogEntry(event, ACTIVITY, EventMethods.onTouchEvent, ACTIVITY_LOG_COLOR)
        mLogger.logEvent(logEntry)
        val handled: Boolean =
            mTouchController.isHandled(
                true,
                TouchController.VIEW_TYPE_ACTIVITY,
                TouchController.METHOD_ON_TOUCH_EVENT,
                event
            )
                || super.onTouchEvent(event)
                || mTouchController.isHandled(
                false,
                TouchController.VIEW_TYPE_ACTIVITY,
                TouchController.METHOD_ON_TOUCH_EVENT,
                event
            )
        mLogger.logEvent(EventLogEntry(logEntry, handled))
        return handled
    }

    // Swap out the top-level view that is a child of group B for one that is a child of group A
    // and vice versa.
    private fun setViewParent(viewChildOfA: Boolean) {
        if (viewChildOfA) {
            findViewById<View>(R.id.viewChildOfGroupA).visibility = View.VISIBLE
            findViewById<View>(R.id.viewChildOfGroupB).visibility = View.GONE
        } else {
            findViewById<View>(R.id.viewChildOfGroupA).visibility = View.GONE
            findViewById<View>(R.id.viewChildOfGroupB).visibility = View.VISIBLE
        }
    }

    private fun isEventToBeLogged(event: MotionEvent): Boolean {
        // getY() is relative to the top of the screen, Compare it to the absolute
        // bottom of the log to check if the touch is in the allowed area.
        if (event.actionMasked == MotionEvent.ACTION_DOWN) {
            mInGesture = (event.y > mLogBottom
                && mBinding.viewGroupA.visibility == View.VISIBLE)
        }
        return mInGesture
    }

    private fun resizeOnScreenLog() {

        // End any in-process transitions in case of overzealous clicking that
        // will leave the screen in a curious state.
        TransitionManager.endTransitions(mBinding.constraintLayout)

        // Make sure we are not scrolling or we may crash during animation with error
        // java.lang.IllegalArgumentException: Scrapped or attached views may not be recycled.
        mBinding.onScreenLog.stopScroll()
        val transition = AutoTransition()

        // Kill all interactions with the on-screen log until transition animation is done.
        // If we don't, odd crashes happen with the recycling of view or adding of views
        // (java.lang.IllegalArgumentException: Scrapped or attached views may not be recycled).
        // This just avoids the whole mess. May be related to:
        // https://github.com/airbnb/epoxy/issues/405
        transition.addListener(object : TransitionListenerAdapter() {
            override fun onTransitionStart(transition: Transition) {
                mBinding.onScreenLog.setOnTouchListener { _: View?, _: MotionEvent? -> true }
            }

            override fun onTransitionEnd(transition: Transition) {
                mBinding.onScreenLog.setOnTouchListener(null)
            }

            override fun onTransitionCancel(transition: Transition) {
                mBinding.onScreenLog.setOnTouchListener(null)
            }
        })
        TransitionManager.beginDelayedTransition(mBinding.constraintLayout, transition)
        if (mBinding.viewGroupA.visibility == View.VISIBLE) {
            mBinding.switchTable.switchConstraintLayout.visibility = View.GONE
            mBinding.viewGroupA.visibility = View.GONE
            mBinding.activityLabel.visibility = View.GONE
        } else {
            mBinding.switchTable.switchConstraintLayout.visibility = View.VISIBLE
            mBinding.viewGroupA.visibility = View.VISIBLE
            mBinding.activityLabel.visibility = View.VISIBLE
        }
    }

    private fun sendLog() {
        val shareIntent = Intent()
        shareIntent.action = Intent.ACTION_SEND
        shareIntent.type = "text/*"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            shareIntent.putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("text/plain", "text/html"))
        }
        shareIntent.putExtra(Intent.EXTRA_TITLE, getString(R.string.sent_log_title))
        val logString = mLogger.logEntriesString

        // Write out the log with HTML formatting.
        shareIntent.putExtra(Intent.EXTRA_HTML_TEXT, logString)

        // Strip out the formatting for straight text.
        shareIntent.putExtra(
            Intent.EXTRA_TEXT,
            logString
            /*HtmlCompat.fromHtml(logString, HtmlCompat.FROM_HTML_MODE_COMPACT).toString()*/
        )
        startActivity(Intent.createChooser(shareIntent, getString(R.string.send_log_to)))
    }

    companion object {
        @Suppress("unused")
        private val TAG = MainActivity::class.simpleName
        private const val ACTIVITY_LOG_COLOR = Color.BLACK
        private const val HELP_FILE = "file:///android_asset/help_pages/help.html"
        private const val CLEAR_ON_GESTURE_START = "clear_on_gesture_start"
        private const val VIEW_CHILD_OF_GROUP_A = "view_child_of_group_a"
        private const val SWITCHES_KEY = "switches"

        private const val ACTIVITY = "Activity"
    }
}