package com.geeksynergy.airpaper;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class ContentViewer extends AppCompatActivity implements View.OnClickListener {

    TextView infoText;
    TextView titleText;
    TextView dateText;

    Toolbar toolbar;
    Toolbar adBar;
    TextView adLink;

    BufferedReader bufferedReader = null;
    StringBuilder stringBuilder = new StringBuilder();

    RatingBar ratingBar;
    ConnectivityManager connectivityManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content_viewer);

        titleText = (TextView) findViewById(R.id.titleText);
        dateText = (TextView) findViewById(R.id.dateTimeText);
        infoText = (TextView) findViewById(R.id.info_text);

        Intent intent = getIntent();
        String mTitle = intent.getStringExtra("titleInfo");
        String mDate = intent.getStringExtra("dateInfo");
        String mPageTitle = intent.getStringExtra("pageTitleInfo");

        String listTitle;

        titleText.setText(mTitle);
        dateText.setText(mDate);

        ratingBar = (RatingBar) findViewById(R.id.ratingBar);
        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {

            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

                if(networkInfo != null && networkInfo.isConnected()) {
                    ratingBar.setRating(rating);
                } else {
                    ratingBar.setRating(0.0f);
                    Toast.makeText(ContentViewer.this, "Please check your Internet Connection and rate again", Toast.LENGTH_SHORT).show();
                }

            }
        });

        try {
            bufferedReader = new BufferedReader(new InputStreamReader(this.getResources().openRawResource(getResources().getIdentifier(mPageTitle, "raw", getPackageName()))));
            for (String line = null; (line = bufferedReader.readLine()) != null; ) {
                stringBuilder.append(line).append("\n");
            }

            JSONObject jsonRootObject = new JSONObject(stringBuilder.toString());
            JSONArray jsonArray = jsonRootObject.optJSONArray(mPageTitle);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                listTitle = jsonObject.optString("title").toString();
                if (listTitle.compareTo(mTitle) == 0) {
                    infoText.setText(jsonObject.optString("info").toString());
                    break;
                }
            }


        } catch (Exception e) {
            e.printStackTrace();
        }

        adLink = (TextView) findViewById(R.id.ad_link);
        adBar = (Toolbar) findViewById(R.id.ad_bar);
        adBar.setOnClickListener(this);


        toolbar = (Toolbar) findViewById(R.id.tool_bar);
        toolbar.setTitle(titleText.getText());
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_content_viewer, menu);

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
