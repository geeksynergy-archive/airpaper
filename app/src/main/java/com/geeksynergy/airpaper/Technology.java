package com.geeksynergy.airpaper;

import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class Technology extends Fragment {

    public static BufferedReader bufferedReader = null;
    StringBuilder builder = new StringBuilder();
    private List<Recycler_preview_Template> techItems;
    private RecyclerView rv;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.technology, container, false);

        rv = (RecyclerView) v.findViewById(R.id.rv);
        RecyclerView.ItemDecoration itemDecoration = new DividerItemDecoration(getActivity(), LinearLayoutManager.VERTICAL);
        rv.addItemDecoration(itemDecoration);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        rv.setLayoutManager(layoutManager);
        rv.setHasFixedSize(true);

        initializeData();
        initializeAdapter();

        return v;
    }

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

    private void initializeData() {
        techItems = new ArrayList<>();
        try {
            bufferedReader = new BufferedReader(new InputStreamReader(this.getResources().openRawResource(R.raw.technology)));

            for (String line = null; (line = bufferedReader.readLine()) != null; ) {
                builder.append(line).append("\n");
            }
            File dir = new File(Environment.getExternalStorageDirectory() + "/AiRpaper/database/");
            File file = new File(Environment.getExternalStorageDirectory() + "/AiRpaper/database/" + "technology.json");
            if(dir.exists()==false)
            {
                dir.mkdirs();
            }
            if (!file.exists()) {
                try {
                    file.createNewFile();
                    FileWriter filewriter = new FileWriter(Environment.getExternalStorageDirectory() + "/AiRpaper/database/technology.json");
                    filewriter.write(builder.toString());
                    filewriter.close();
                    System.out.println("Successfully Copied JSON Object to File...");
                    //System.out.println("\nJSON Object: " + builder.toString());
                    JSONObject jsonRootObject = new JSONObject(builder.toString());
                    JSONArray jsonArray = jsonRootObject.optJSONArray("technology");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        String listTitle = jsonObject.optString("title").toString();
                        String listDate = jsonObject.optString("date").toString();
                        String listTime = jsonObject.optString("time").toString();
                        String listImg = jsonObject.optString("img64").toString();
                        Boolean listuni = Boolean.valueOf(jsonObject.optString("uni").toString().equals("True"));
                        techItems.add(new Recycler_preview_Template(listTitle, listDate + "  " + listTime, listImg, listuni));
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else
            {
                JSONObject jsonRootObject = new JSONObject(getStringFromFile(Environment.getExternalStorageDirectory() + "/AiRpaper/database/" + "technology" + ".json"));
                JSONArray jsonArray = jsonRootObject.optJSONArray("technology");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    String listTitle = jsonObject.optString("title").toString();
                    String listDate = jsonObject.optString("date").toString();
                    String listTime = jsonObject.optString("time").toString();
                    String listImg = jsonObject.optString("img64").toString();
                    Boolean listuni = Boolean.valueOf(jsonObject.optString("uni").toString().equals("True"));
                    techItems.add(new Recycler_preview_Template(listTitle, listDate + "  " + listTime, listImg, listuni));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initializeAdapter() {
        TechnologyRVAdapter adapter = new TechnologyRVAdapter(techItems);
        rv.setAdapter(adapter);
    }

}