package com.example.toucheventexplorer.activity

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.toucheventexplorer.R
import com.example.toucheventexplorer.databinding.ActivityHelpBinding
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity

class HelpActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        val helpFile: String = intent?.extras?.getString(HELP_FILE) ?: run { finish(); return }
        val webView: WebView
        super.onCreate(savedInstanceState)
        val binding: ActivityHelpBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_help)
        val actionBar = supportActionBar
        actionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = getString(R.string.help)
        }
        webView = binding.webViewHelp
        // Let them zoom in and out.
        webView.settings.builtInZoomControls = true
        webView.settings.displayZoomControls = false
        webView.loadUrl(helpFile)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_help, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
            R.id.show_open_source_licenses -> {
                startActivity(Intent(this, OssLicensesMenuActivity::class.java))
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        @Suppress("unused")
        private val TAG = HelpActivity::class.java.simpleName
        const val HELP_FILE = "HELP_FILE"
    }
}