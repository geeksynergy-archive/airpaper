package com.geeksynergy.airpaper;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.method.ScrollingMovementMethod;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.larvalabs.svgandroid.SVG;
import com.larvalabs.svgandroid.SVGParser;

import java.util.Arrays;


public class Latest extends Fragment {

    static public TextView latesttext;
    static public ImageView latestimg;

    static private String base_64_android =
            "PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0idXRmLTgiPz4NCjwhLS0gR2VuZXJhdG9yOiBBZG9iZSBJbGx1c3RyYXRvciAx" +
            "My4wLjAsIFNWRyBFeHBvcnQgUGx1Zy1JbiAuIFNWRyBWZXJzaW9uOiA2LjAwIEJ1aWxkIDE0OTQ4KSAgLS0+DQo8IURPQ1RZUEUg" +
            "c3ZnIFBVQkxJQyAiLS8vVzNDLy9EVEQgU1ZHIDEuMS8vRU4iICJodHRwOi8vd3d3LnczLm9yZy9HcmFwaGljcy9TVkcvMS4xL0RU" +
            "RC9zdmcxMS5kdGQiPg0KPHN2ZyB2ZXJzaW9uPSIxLjEiIGlkPSJMYXllcl8xIiB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAw" +
            "MC9zdmciIHhtbG5zOnhsaW5rPSJodHRwOi8vd3d3LnczLm9yZy8xOTk5L3hsaW5rIiB4PSIwcHgiIHk9IjBweCINCgkgd2lkdGg9" +
            "IjUwMHB4IiBoZWlnaHQ9IjUwMHB4IiB2aWV3Qm94PSIwIDAgNTAwIDUwMCIgZW5hYmxlLWJhY2tncm91bmQ9Im5ldyAwIDAgNTAw" +
            "IDUwMCIgeG1sOnNwYWNlPSJwcmVzZXJ2ZSI+DQo8ZyBpZD0ibWF4X3dpZHRoX194MkZfX2hlaWdodCIgZGlzcGxheT0ibm9uZSI+" +
            "DQoJPHBhdGggZGlzcGxheT0iaW5saW5lIiBkPSJNNDk5LjAwMSwxdjQ5OEgxVjFINDk5LjAwMSBNNTAwLjAwMSwwSDB2NTAwaDUw" +
            "MC4wMDFWMEw1MDAuMDAxLDB6Ii8+DQo8L2c+DQo8ZyBpZD0iYW5kcm9kIj4NCgk8cGF0aCBmaWxsPSIjOUZCRjNCIiBkPSJNMzAx" +
            "LjMxNCw4My4yOThsMjAuMTU5LTI5LjI3MmMxLjE5Ny0xLjc0LDAuODk5LTQuMDI0LTAuNjY2LTUuMTA0Yy0xLjU2My0xLjA3NC0z" +
            "LjgwNS0wLjU0My00Ljk5MywxLjE5OQ0KCQlMMjk0Ljg2Myw4MC41M2MtMTMuODA3LTUuNDM5LTI5LjEzOS04LjQ3LTQ1LjI5OS04" +
            "LjQ3Yy0xNi4xNiwwLTMxLjQ5NiwzLjAyOC00NS4zMDIsOC40N2wtMjAuOTQ4LTMwLjQxDQoJCWMtMS4yMDEtMS43NC0zLjQzOS0y" +
            "LjI3My01LjAwMy0xLjE5OWMtMS41NjQsMS4wNzctMS44NjEsMy4zNjItMC42NjQsNS4xMDRsMjAuMTY2LDI5LjI3Mg0KCQljLTMy" +
            "LjA2MywxNC45MTYtNTQuNTQ4LDQzLjI2LTU3LjQxMyw3Ni4zNGgyMTguMzE2QzM1NS44NjEsMTI2LjU1NywzMzMuMzc1LDk4LjIx" +
            "NCwzMDEuMzE0LDgzLjI5OCIvPg0KCTxwYXRoIGZpbGw9IiNGRkZGRkYiIGQ9Ik0yMDMuOTU2LDEyOS40MzhjLTYuNjczLDAtMTIu" +
            "MDgtNS40MDctMTIuMDgtMTIuMDc5YzAtNi42NzEsNS40MDQtMTIuMDgsMTIuMDgtMTIuMDgNCgkJYzYuNjY4LDAsMTIuMDczLDUu" +
            "NDA3LDEyLjA3MywxMi4wOEMyMTYuMDMsMTI0LjAzLDIxMC42MjQsMTI5LjQzOCwyMDMuOTU2LDEyOS40MzgiLz4NCgk8cGF0aCBm" +
            "aWxsPSIjRkZGRkZGIiBkPSJNMjk1LjE2MSwxMjkuNDM4Yy02LjY2OCwwLTEyLjA3NC01LjQwNy0xMi4wNzQtMTIuMDc5YzAtNi42" +
            "NzMsNS40MDYtMTIuMDgsMTIuMDc0LTEyLjA4DQoJCWM2LjY3NSwwLDEyLjA3OSw1LjQwOSwxMi4wNzksMTIuMDhDMzA3LjI0LDEy" +
            "NC4wMywzMDEuODM0LDEyOS40MzgsMjk1LjE2MSwxMjkuNDM4Ii8+DQoJPHBhdGggZmlsbD0iIzlGQkYzQiIgZD0iTTEyNi4zODMs" +
            "Mjk3LjU5OGMwLDEzLjQ1LTEwLjkwNCwyNC4zNTQtMjQuMzU1LDI0LjM1NGwwLDBjLTEzLjQ1LDAtMjQuMzU0LTEwLjkwNC0yNC4z" +
            "NTQtMjQuMzU0VjE5OS4wOQ0KCQljMC0xMy40NSwxMC45MDQtMjQuMzU0LDI0LjM1NC0yNC4zNTRsMCwwYzEzLjQ1MSwwLDI0LjM1" +
            "NSwxMC45MDQsMjQuMzU1LDI0LjM1NFYyOTcuNTk4eiIvPg0KCTxwYXRoIGZpbGw9IiM5RkJGM0IiIGQ9Ik0xNDAuMzk2LDE3NS40" +
            "ODl2MTc3LjkxNWMwLDEwLjU2Niw4LjU2NiwxOS4xMzMsMTkuMTM1LDE5LjEzM2gyMi42MzN2NTQuNzQ0DQoJCWMwLDEzLjQ1MSwx" +
            "MC45MDMsMjQuMzU0LDI0LjM1NCwyNC4zNTRjMTMuNDUxLDAsMjQuMzU1LTEwLjkwMywyNC4zNTUtMjQuMzU0di01NC43NDRoMzcu" +
            "MzcxdjU0Ljc0NA0KCQljMCwxMy40NTEsMTAuOTAyLDI0LjM1NCwyNC4zNTQsMjQuMzU0czI0LjM1NC0xMC45MDMsMjQuMzU0LTI0" +
            "LjM1NHYtNTQuNzQ0aDIyLjYzM2MxMC41NjksMCwxOS4xMzctOC41NjIsMTkuMTM3LTE5LjEzM1YxNzUuNDg5DQoJCUgxNDAuMzk2" +
            "eiIvPg0KCTxwYXRoIGZpbGw9IiM5RkJGM0IiIGQ9Ik0zNzIuNzM0LDI5Ny41OThjMCwxMy40NSwxMC45MDMsMjQuMzU0LDI0LjM1" +
            "NCwyNC4zNTRsMCwwYzEzLjQ1LDAsMjQuMzU0LTEwLjkwNCwyNC4zNTQtMjQuMzU0VjE5OS4wOQ0KCQljMC0xMy40NS0xMC45MDQt" +
            "MjQuMzU0LTI0LjM1NC0yNC4zNTRsMCwwYy0xMy40NTEsMC0yNC4zNTQsMTAuOTA0LTI0LjM1NCwyNC4zNTRWMjk3LjU5OHoiLz4N" +
            "CjwvZz4NCjwvc3ZnPg0K";

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v =  inflater.inflate(R.layout.latest, container, false);
        latesttext = (TextView) v.findViewById(R.id.decoder_tvz);
        latesttext.setMovementMethod(new ScrollingMovementMethod());

        // disable hw acceleration in higher android versions
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB) {
            v.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }


        byte[] data = Base64.decode(base_64_android, Base64.DEFAULT);
        latestimg = (ImageView) v.findViewById(R.id.decoder_imz);
//        SVG svg = SVGParser.getSVGFromResource(getResources(), R.raw.android);        //Parse the SVG file from the resource

        SVG svg = SVGParser.getSVGFromString(new String(data));        //Parse the SVG file from the resource
        latestimg.setImageDrawable(svg.createPictureDrawable());        //Get a drawable from the parsed SVG and apply to ImageView
        return v;
    }
}