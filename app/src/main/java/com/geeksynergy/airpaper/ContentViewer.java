package com.geeksynergy.airpaper;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ContentViewer extends AppCompatActivity implements View.OnClickListener {

    TextView infoText;
    TextView titleText;
    TextView dateText;

    EditText commentText;
    Button commentButton;

    Toolbar toolbar;
    Toolbar adBar;
    TextView adLink;

    BufferedReader bufferedReader = null;
    StringBuilder stringBuilder = new StringBuilder();

    RatingBar ratingBar;
    ConnectivityManager connectivityManager;

    private String device_id;
    private String comment;
    String rate_value;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content_viewer);
        StrictMode.ThreadPolicy threadPolicy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(threadPolicy);

        //Comment Section EditText and Button initialization
        commentText = (EditText) findViewById(R.id.commentText);
        commentButton = (Button) findViewById(R.id.commentButton);

        titleText = (TextView) findViewById(R.id.titleText);
        dateText = (TextView) findViewById(R.id.dateTimeText);
        infoText = (TextView) findViewById(R.id.info_text);

        Intent intent = getIntent();
        final String mTitle = intent.getStringExtra("titleInfo");
        String mDate = intent.getStringExtra("dateInfo");
        final String mPageTitle = intent.getStringExtra("pageTitleInfo");

        String listTitle;

        titleText.setText(mTitle);
        dateText.setText(mDate);

        ratingBar = (RatingBar) findViewById(R.id.ratingBar);
        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {

            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                rate_value = String.valueOf(rating);
                Toast.makeText(getApplicationContext(), "You rated " + String.valueOf(rating) + ". Add a comment to this article.", Toast.LENGTH_SHORT).show();
                commentText.setVisibility(View.VISIBLE);
                commentButton.setVisibility(View.VISIBLE);
                ratingBar.setVisibility(View.INVISIBLE);
            }

        });

        commentButton.setOnClickListener(new View.OnClickListener() {

            InputStream inputStream = null;

            @Override
            public void onClick(View v) {
                connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

                Calendar now = Calendar.getInstance();
                int year = now.get(Calendar.YEAR);
                int month = now.get(Calendar.MONTH) + 1; // Note: zero based!
                int day = now.get(Calendar.DAY_OF_MONTH);
                int hour = now.get(Calendar.HOUR_OF_DAY);
                int minute = now.get(Calendar.MINUTE);

                if (networkInfo != null && networkInfo.isConnected())

                {
                    device_id = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
                    String programme_category = mPageTitle;
                    String programme_title = mTitle;

                    List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
                    nameValuePairs.add(new BasicNameValuePair("device_id", device_id.toString()));
                    nameValuePairs.add(new BasicNameValuePair("programme_category", programme_category));
                    nameValuePairs.add(new BasicNameValuePair("programme_title", programme_title));
                    nameValuePairs.add(new BasicNameValuePair("rating", rate_value));
                    nameValuePairs.add(new BasicNameValuePair("date", String.valueOf(day) + "-" + String.valueOf(month) + "-" + String.valueOf(year)));
                    nameValuePairs.add(new BasicNameValuePair("time", String.valueOf(hour) + ":" + String.valueOf(minute)));
                    nameValuePairs.add(new BasicNameValuePair("comment", commentText.getText().toString()));

                    try {
                        HttpClient httpClient = new DefaultHttpClient();
                        HttpPost httpPost = new HttpPost("http://192.168.8.100/insert_data.php");
                        httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                        HttpResponse response = httpClient.execute(httpPost);
                        HttpEntity httpEntity = response.getEntity();
                        inputStream = httpEntity.getContent();

                        String message = "Thanks for your valuable feedback!!";
                        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                        commentText.setText("");

                    } catch (ClientProtocolException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
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
