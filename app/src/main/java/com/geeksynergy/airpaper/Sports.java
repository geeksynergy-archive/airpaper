package com.geeksynergy.airpaper;

import android.os.Bundle;
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
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class Sports extends Fragment {
    BufferedReader bufferedReader = null;
    StringBuilder builder = new StringBuilder();
    private List<Person> sportsItems;
    private RecyclerView rv;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.sports, container, false);
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

    private void initializeData() {
        sportsItems = new ArrayList<>();
        try {
            bufferedReader = new BufferedReader(new InputStreamReader(this.getResources().openRawResource(R.raw.sports)));

            for (String line = null; (line = bufferedReader.readLine()) != null; ) {
                builder.append(line).append("\n");
            }

            JSONObject jsonRootObject = new JSONObject(builder.toString());
            JSONArray jsonArray = jsonRootObject.optJSONArray("sports");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String listTitle = jsonObject.optString("title").toString();
                String listDate = jsonObject.optString("date").toString();
                String listTime = jsonObject.optString("time").toString();
                sportsItems.add(new Person(listTitle, listDate + "  " + listTime, R.mipmap.ic_launcher));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initializeAdapter() {
        SportsRVAdapter adapter = new SportsRVAdapter(sportsItems);
        rv.setAdapter(adapter);
    }


}