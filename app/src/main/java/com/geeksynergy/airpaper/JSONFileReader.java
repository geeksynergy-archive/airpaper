package com.geeksynergy.airpaper;

import android.app.Activity;
import android.content.Context;
import android.widget.TextView;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class JSONFileReader {

    BufferedReader bufferedReader = null;
    StringBuilder builder = new StringBuilder();

    TextView displayTemperature;
    TextView displayStatus;
    TextView displayDate;
    TextView displayDay;
    TextView displayCity;
    TextView displayState;
    TextView displayTime;


    public void getJSONData(Activity view, Context context) {
        try {

            displayTemperature = (TextView) view.findViewById(R.id.temperature_degree);
            displayStatus = (TextView) view.findViewById(R.id.temp_status);
            displayDate = (TextView) view.findViewById(R.id.date);
            displayDay = (TextView) view.findViewById(R.id.day);
            displayCity = (TextView) view.findViewById(R.id.City);
            displayState = (TextView) view.findViewById(R.id.State);
            displayTime = (TextView) view.findViewById(R.id.time);

            bufferedReader = new BufferedReader(new InputStreamReader(context.getResources().openRawResource(R.raw.weather)));

            for (String line = null; (line = bufferedReader.readLine()) != null; ) {
                builder.append(line).append("\n");
            }

            JSONObject jsnObject = new JSONObject(builder.toString());
            JSONObject weatherinfo = jsnObject.getJSONObject("weatherinfo");
            String temperature = weatherinfo.getString("temperature");
            String status = weatherinfo.getString("status");
            String date = weatherinfo.getString("date");
            String day = weatherinfo.getString("day");
            String time = weatherinfo.getString("time");
            String city = weatherinfo.getString("city");
            String state = weatherinfo.getString("state");


            displayTemperature.setText(temperature);
            displayStatus.setText(status);
            displayDate.setText(date);
            displayDay.setText(day);
            displayTime.setText(time);
            displayCity.setText(city);
            displayState.setText(state);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
