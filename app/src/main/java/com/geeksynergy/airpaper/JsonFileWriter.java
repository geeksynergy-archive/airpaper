package com.geeksynergy.airpaper;

/**
* Created by Sachin Anchan on 24-09-2015.
*/
import android.net.Uri;
import android.os.Environment;
import android.support.v4.app.Fragment;

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
    public static void putJSONData(String category,String title,String info,String img,String date,String time) throws IOException {

        Latest.latesttext.append("category:" + category + "\n" +
                        "title: " + title + "\n" +
                        "date: " + date + "\n" +
                        "time: " + time + "\n" +
                        "info: " + info + "\n" +
                        "img: " + img + "\n\n\n"
        );
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
            JSONObject jsobj = jsonArray.getJSONObject(0);
//            obj.put("Name", "technology");
//            jsonArray.put("title:" + title);
//            jsonArray.put("date:" + date);
//            jsonArray.put("time:" + time);
//            jsonArray.put("info:" + info);
            jsobj.put("title" ,title);
            jsobj.put("date" , date);
            jsobj.put("time" , time);
            jsobj.put("info" , info.substring(0,info.length()-5));
            jsonRootObject.put(cat_filename, jsonArray);
            file.write(jsonRootObject.toString());
            System.out.println("Successfully Updated JSON Object to File...");
            file.flush();
            file.close();
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
