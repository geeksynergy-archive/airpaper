package com.geeksynergy.airpaper;

/**
 * Created by Sachin Anchan on 22-09-2015.
 */
public class buffer_packet {

       // create a data for this container
       public short[] payload;
       private int payloadlength;
       buffer_packet()
       {
           this.payloadlength = 8192;
           this.payload=  new short[payloadlength];
       }


}
