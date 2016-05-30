package com.geeksynergy.airpaper;

import android.content.Intent;
import android.os.Build;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.larvalabs.svgandroid.SVGParser;

import java.util.List;

public class HealthCareRVAdapter extends RecyclerView.Adapter<HealthCareRVAdapter.HCViewHolder> {

    List<Recycler_preview_Template> recyclerpreviewTemplates;

    HealthCareRVAdapter(List<Recycler_preview_Template> recyclerpreviewTemplates) {
        this.recyclerpreviewTemplates = recyclerpreviewTemplates;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public HCViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.row_view, viewGroup, false);
        HCViewHolder pvh = new HCViewHolder(v);
        return pvh;
    }

    @Override
    public void onBindViewHolder(HCViewHolder hcViewHolder, int i) {
        try{
            if(recyclerpreviewTemplates.get(i).uni) {
                hcViewHolder.listTitle.setText( new String(Base64.decode(recyclerpreviewTemplates.get(i).title, Base64.DEFAULT)));
                hcViewHolder.listDate.setText(recyclerpreviewTemplates.get(i).date);
                hcViewHolder.listPhoto.setImageDrawable(SVGParser.getSVGFromString(new String(Base64.decode(recyclerpreviewTemplates.get(i).photo_string64, Base64.DEFAULT))).createPictureDrawable());
            }
            else {
                hcViewHolder.listTitle.setText(recyclerpreviewTemplates.get(i).title);
                hcViewHolder.listDate.setText(recyclerpreviewTemplates.get(i).date);
                hcViewHolder.listPhoto.setImageDrawable(SVGParser.getSVGFromString(new String(Base64.decode(recyclerpreviewTemplates.get(i).photo_string64, Base64.DEFAULT))).createPictureDrawable());
            }
        }
    catch (Exception ez)
    {

    }
}

    @Override
    public int getItemCount() {
        return recyclerpreviewTemplates.size();
    }

    public static class HCViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView listTitle;
        TextView listDate;
        ImageView listPhoto;


        HCViewHolder(View itemView) {
            super(itemView);
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB) {
                itemView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            }
            itemView.setClickable(true);
            itemView.setOnClickListener(this);
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
            intent.putExtra("pageTitleInfo", "healthcare");
            v.getContext().startActivity(intent);
        }

    }
}