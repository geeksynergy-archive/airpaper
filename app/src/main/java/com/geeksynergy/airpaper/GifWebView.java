package com.geeksynergy.airpaper;

import android.content.Context;
import android.webkit.WebView;

/**
 * Created by Sachin Anchan on 23-09-2015.
 */
public class GifWebView extends WebView {

    public GifWebView(Context context, String path) {
        super(context);
        loadUrl(path);
    }
}