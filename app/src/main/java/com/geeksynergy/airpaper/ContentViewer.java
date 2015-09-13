package com.geeksynergy.airpaper;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

public class ContentViewer extends AppCompatActivity implements View.OnClickListener {

    TextView infoText;
    Toolbar toolbar;
    Toolbar adBar;
    TextView adLink;

    private ShareActionProvider shareActionProvider = null;
    Intent intent = new Intent(Intent.ACTION_SEND);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content_viewer);

        intent.setType("text/plain");

        adLink = (TextView) findViewById(R.id.ad_link);
        adBar = (Toolbar) findViewById(R.id.ad_bar);
        adBar.setOnClickListener(this);

        infoText = (TextView) findViewById(R.id.info_text);
        infoText.setText(Html.fromHtml(getString(R.string.information_text)));
        toolbar = (Toolbar) findViewById(R.id.tool_bar);
        toolbar.setTitle("Intent");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_content_viewer, menu);
        MenuItem menuItem = menu.findItem(R.id.action_share);
        shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case R.id.action_settings:
                return true;
            case R.id.action_share:
                return true;
            case android.R.id.home:
                this.onNavigateUp();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        this.finish();
    }

    @Override
    public boolean onNavigateUp() {
        this.finish();
        return true;
    }

    @Override
    public void onClick(View v) {
        Uri uri = Uri.parse(adLink.getText().toString());
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }
}
