package com.geeksynergy.airpaper;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class Latest extends Fragment {

    static public TextView latesttext;
    static public ImageView latestimg;


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v =  inflater.inflate(R.layout.latest, container, false);
        latesttext = (TextView) v.findViewById(R.id.decoder_tvz);
        latesttext.setMovementMethod(new ScrollingMovementMethod());

        latestimg = (ImageView) v.findViewById(R.id.decoder_ivz);

        return v;
    }
}