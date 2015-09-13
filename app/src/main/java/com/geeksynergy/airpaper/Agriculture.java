package com.geeksynergy.airpaper;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class Agriculture extends Fragment {

    private List<Person> persons;
    private RecyclerView rv;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.agriculture,container,false);

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

    private void initializeData(){
        persons = new ArrayList<>();
        persons.add(new Person("Tomato Harvesting", "Often confused as a vegetable tomato...", R.drawable.ic_launcher));
        persons.add(new Person("Grape Cultivation", "The creepy climber grapes ...", R.drawable.grape));
        persons.add(new Person("Mixed Farming", "Looking for increasing the yield ....", R.drawable.mixed));
    }

    private void initializeAdapter(){
        RecyclerViewAdapter adapter = new RecyclerViewAdapter(persons);
        rv.setAdapter(adapter);
    }

}