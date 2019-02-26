package com.example.toucheventexplorer;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;

import com.example.toucheventexplorer.databinding.ActivityHelpBinding;
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

public class HelpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        @SuppressWarnings("ConstantConditions") final String helpFile = getIntent().getExtras().getString(HELP_FILE);
        WebView webView;
        ActivityHelpBinding binding;

        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_help);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(getString(R.string.help));
        }
        webView = binding.webViewHelp;
        // Let them zoom in and out.
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setDisplayZoomControls(false);
        webView.loadUrl(helpFile);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_help, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                finish();
                return true;

            case R.id.show_open_source_licenses:
                startActivity(new Intent(this, OssLicensesMenuActivity.class));
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("unused")
    private static final String TAG = HelpActivity.class.getSimpleName();
    public static final String HELP_FILE = "HELP_FILE";
}