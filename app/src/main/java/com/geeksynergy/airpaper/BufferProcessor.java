package com.geeksynergy.airpaper;


public abstract class BufferProcessor {


    static {
        System.loadLibrary("decomon");
    }

    public BufferProcessor() {
        super();
    }

    native void init();

    native void processBuffer(float[] buf, int length);

    native void processBuffer2(byte[] buf);

    abstract void read();

}