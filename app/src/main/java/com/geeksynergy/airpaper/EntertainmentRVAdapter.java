package com.geeksynergy.airpaper;

import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class EntertainmentRVAdapter extends RecyclerView.Adapter<EntertainmentRVAdapter.ETViewHolder> {

    List<Person> persons;

    EntertainmentRVAdapter(List<Person> persons) {
        this.persons = persons;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public ETViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.row_view, viewGroup, false);
        ETViewHolder pvh = new ETViewHolder(v);
        return pvh;
    }

    @Override
    public void onBindViewHolder(ETViewHolder etViewHolder, int i) {
        etViewHolder.listTitle.setText(persons.get(i).title);
        etViewHolder.listDate.setText(persons.get(i).date);
        etViewHolder.listPhoto.setImageResource(persons.get(i).photoId);
    }

    @Override
    public int getItemCount() {
        return persons.size();
    }

    public static class ETViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        CardView cv;
        TextView listTitle;
        TextView listDate;
        ImageView listPhoto;


        ETViewHolder(View itemView) {
            super(itemView);
            itemView.setClickable(true);
            itemView.setOnClickListener(this);
            cv = (CardView) itemView.findViewById(R.id.cv);
            listTitle = (TextView) itemView.findViewById(R.id.list_title);
            listDate = (TextView) itemView.findViewById(R.id.list_date);
            listPhoto = (ImageView) itemView.findViewById(R.id.list_photo);
        }

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(v.getContext(), ContentViewer.class);
            CharSequence titleText = listTitle.getText();
            CharSequence dateText = listDate.getText();
            intent.putExtra("titleInfo", titleText.toString());
            intent.putExtra("dateInfo", dateText.toString());
            intent.putExtra("pageTitleInfo", "entertainment");
            v.getContext().startActivity(intent);
        }

    }
}