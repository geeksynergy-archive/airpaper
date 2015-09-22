package com.geeksynergy.airpaper;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Movie;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.View;

import java.io.InputStream;


public class GIFView extends View {

    private InputStream gifInputStream;
    private Movie gifMovie;
    private int movieWidth, movieHeight;
    private long movieDuration;
    private long movieStart;

    public GIFView(Context context) {
        super(context);
        init(context);
    }

    public GIFView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        init(context);
    }

    public GIFView(Context context, AttributeSet attributeSet, int defStyleAttr) {
        super(context, attributeSet, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        setFocusable(true);
        gifInputStream = context.getResources().openRawResource(R.raw.airpaper);
        gifMovie = Movie.decodeStream(gifInputStream);

//        movieWidth = gifMovie.width();
//        movieHeight = gifMovie.height();
//        movieDuration = gifMovie.duration();

    }

//    @Override
//    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        setMeasuredDimension(movieWidth, movieHeight);
//    }
//
//    public int getMovieWidth() {
//        return movieWidth;
//    }
//
//    public int getMovieHeight() {
//        return movieHeight;
//    }
//
//    public long getMovieDuration() {
//        return movieDuration;
//    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(Color.WHITE);
        super.onDraw(canvas);
        long now = SystemClock.uptimeMillis();

        if(movieStart == 0) {
            movieStart = now;
        }

        if(gifMovie != null) {

            int relTime = (int)((now - movieStart) % gifMovie.duration());

            gifMovie.setTime(relTime);

            gifMovie.draw(canvas, this.getWidth()/2-20,this.getHeight()/2-40);
            this.invalidate();
        }
    }
}
