package com.geeksynergy.airpaper;

/**
* Created by Sachin Anchan on 24-09-2015.
*/
import android.net.Uri;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.util.Base64;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;


public class JsonFileWriter  {

    public static String convertStreamToString(InputStream is) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }
        reader.close();
        return sb.toString();
    }

    public static String getStringFromFile (String filePath) throws Exception {
        File fl = new File(filePath);
        FileInputStream fin = new FileInputStream(fl);
        String ret = convertStreamToString(fin);
        //Make sure you close all streams.
        fin.close();
        return ret;
    }

    @SuppressWarnings("unchecked")
    public static void putJSONData(String category,String title,String info,String img,String date,String time, Boolean Uni) throws IOException {

        if(!Uni)
        Latest.latesttext.append("category:" + category + "\n" +
                        "title: " + title + "\n" +
                        "date: " + date + "\n" +
                        "time: " + time + "\n" +
                        "info: " + info + "\n" +
                        "uni: " + "False" + "\n" +
                        "img64: " + img + "\n\n\n"
        );
        else
            Latest.latesttext.append("category:" + category + "\n" +
                    "title: " + new String(Base64.decode(title, Base64.DEFAULT)) + "\n" +
                    "date: " + date + "\n" +
                    "time: " + time + "\n" +
                    "info: " + new String(Base64.decode(info, Base64.DEFAULT)) + "\n" +
                    "uni: " + "True" + "\n" +
                    "img64: " + img + "\n\n\n");

        String cat_filename = "technology";
        if(category.contains("1000"))
            cat_filename = "technology";
        if(category.contains("2000"))
            cat_filename = "sports";
        if(category.contains("3000"))
            cat_filename = "healthcare";
        if(category.contains("4000"))
            cat_filename = "business";
        if(category.contains("5000"))
            cat_filename = "agriculture";

        FileWriter file;
        try {
            JSONObject jsonRootObject = new JSONObject(getStringFromFile(Environment.getExternalStorageDirectory() + "/AiRpaper/database/" + cat_filename +".json"));
            file = new FileWriter(Environment.getExternalStorageDirectory() + "/AiRpaper/database/" + cat_filename +".json");
            JSONArray jsonArray = jsonRootObject.optJSONArray(cat_filename);
            Boolean existing_record = false;
            int record_pos = -1;
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                if (jsonObject.optString("title").toString().compareTo(title) == 0) {
                    existing_record =true;
                    record_pos = i;
                    break;
                }
            }

            if(existing_record)
                {
                    JSONObject json_obj = jsonArray.getJSONObject(record_pos);
                    json_obj.put("title", title);
                    json_obj.put("date", date);
                    json_obj.put("time", time);
                    json_obj.put("info", info.substring(0, info.length() - 5));
                    json_obj.put("img64", img);
                    json_obj.put("uni", Uni?"True":"False");
                    jsonArray.put(json_obj);
                    jsonRootObject.put(cat_filename, jsonArray);
                    file.write(jsonRootObject.toString());
                    System.out.println("Successfully Updated JSON Object to File...");
                    file.flush();
                    file.close();
                }
            else
                {
                    JSONObject json_obj = new JSONObject();
                    json_obj.put("title", title);
                    json_obj.put("date", date);
                    json_obj.put("time", time);
                    json_obj.put("info", info.substring(0, info.length() - 5));
                    json_obj.put("img64", img);
                    json_obj.put("uni", Uni?"True":"False");
                    jsonArray.put(json_obj);
                    jsonRootObject.put(cat_filename, jsonArray);
                    file.write(jsonRootObject.toString());
                    System.out.println("Successfully Updated JSON Object to File...");
                    file.flush();
                    file.close();
                }
        }
        catch (JSONException e) {
            e.printStackTrace();
        }

        catch (IOException e) {
            e.printStackTrace();

        }
        catch (Exception e) {
            e.printStackTrace();

        }
//        finally {
//            file.flush();
//            file.close();
//        }
    }
}
