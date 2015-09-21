package com.geeksynergy.airpaper;


import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * <h1>RF Analyzer - Demodulator</h1>
 * <p/>
 * Module:      Demodulator.java
 * Description: This class implements demodulation of various analog radio modes (FM, AM, SSB).
 * It runs as a separate thread. It will read raw complex samples from a queue,
 * process them (channel selection, filtering, demodulating) and forward the to
 * an AudioSink thread.
 *
 * @author Dennis Mantz
 *         <p/>
 *         Copyright (C) 2014 Dennis Mantz
 *         License: http://www.gnu.org/licenses/gpl.html GPL version 2 or higher
 *         <p/>
 *         This library is free software; you can redistribute it and/or
 *         modify it under the terms of the GNU General Public
 *         License as published by the Free Software Foundation; either
 *         version 2 of the License, or (at your option) any later version.
 *         <p/>
 *         This library is distributed in the hope that it will be useful,
 *         but WITHOUT ANY WARRANTY; without even the implied warranty of
 *         MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *         General Public License for more details.
 *         <p/>
 *         You should have received a copy of the GNU General Public
 *         License along with this library; if not, write to the Free Software
 *         Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 */
public class Demodulator extends Thread {
    public static final int INPUT_RATE = 1000000;    // Expected rate of the incoming samples
    public static final int DEMODULATION_OFF = 0;
    public static final int DEMODULATION_AM = 1;
    public static final int DEMODULATION_NFM = 2;
    public static final int DEMODULATION_WFM = 3;
    public static final int DEMODULATION_LSB = 4;
    public static final int DEMODULATION_USB = 5;
    public static final int DEMODULATION_FSK = 6;
    private static final String LOGTAG = "Demodulator";
    private static final int AUDIO_RATE = 31250;    // Even though this is not a proper audio rate, the Android system can
    // handle it properly and it is a integer fraction of the input rate (1MHz).
    // The quadrature rate is the sample rate that is used for the demodulation:
    private static final int[] QUADRATURE_RATE = {1,                // off; this value is not 0 to avoid divide by zero errors!
            2 * AUDIO_RATE,    // AM
            2 * AUDIO_RATE,    // nFM
            8 * AUDIO_RATE,    // wFM
            2 * AUDIO_RATE,    // LSB
            2 * AUDIO_RATE, // USB
            8 * AUDIO_RATE // FSK
    };
    // FILTERING (This is the channel filter controlled by the user)
    private static final int USER_FILTER_ATTENUATION = 20;
    private static final int[] MIN_USER_FILTER_WIDTH = {0,        // off
            3000,    // AM
            3000,    // nFM
            50000,    // wFM
            1500,    // LSB
            1500,   // USB
            50000};    // USB
    private static final int[] MAX_USER_FILTER_WIDTH = {0,        // off
            15000,    // AM
            15000,    // nFM
            120000,    // wFM
            5000,    // LSB
            5000,  // USB
            120000};  // FSK
    private static final int BAND_PASS_ATTENUATION = 40;
    static private String Stream_Data = "";
    public int demodulationMode;
    private boolean stopRequested = true;
    // DECIMATION
    private Decimator decimator;    // will do INPUT_RATE --> QUADRATURE_RATE
    private FirFilter userFilter = null;
    private int userFilterCutOff = 0;
    private SamplePacket quadratureSamples;
    // DEMODULATION
    private SamplePacket demodulatorHistory;    // used for FM demodulation
    private ComplexFirFilter mybandPassFilter = null;    // used for SSB demodulation
    private float lastMax = 0;    // used for gain control in AM / SSB demodulation
    private ComplexFirFilter bandPassFilter = null;    // used for SSB demodulation
    // AUDIO OUTPUT
    private AudioSink audioSink = null;        // Will do QUADRATURE_RATE --> AUDIO_RATE and audio output
    // My band pass filter in the code
    private ComplexFirFilter fm_bandPassfilter = null;    // used for SSB demodulation
    private String Stream_Bin_Data = "0100000101101001010100" +
            "1001110000011000010111000001100101011100100010000001" +
            "1010010111001100100000011000010010000001100100011010" +
            "0101100111011010010111010001100001011011000010000001" +
            "1001000110000101110100011000010010000001100100011010" +
            "0101110011011101000111001001101001011000100111010101" +
            "1101000110100101101111011011100010000001110000011011" +
            "0001100001011101000110011001101111011100100110110100" +
            "1000000110001001110101011010010110110001110100001000" +
            "0001101111011011100010000001110100011010000110010100" +
            "1000000111000001110010011001010110110101101001011100" +
            "1101100101001000000110111101100110001000000100100101" +
            "1011100110010001101001011000010111001100100000010100" +
            "0001101111011101110110010101110010011001100111010101" +
            "1011000010000001110010011000010110010001101001011011" +
            "1100100000011011100110010101110100011101110110111101" +
            "1100100110101100100000011001100110111101110010001000" +
            "0001100111011011110111011001100101011100100110111001" +
            "1011010110010101101110011101000010000001110100011011" +
            "1100100000011100100110010101100001011000110110100000" +
            "1000000110001101101001011101000110100101111010011001" +
            "0101101110011100110010000001100101011001100110011001" +
            "1001010110001101110100011010010111011001100101011011" +
            "0001111001001011100010000001001001011101000010000001" +
            "1011000110010101110110011001010111001001100001011001" +
            "1101100101011100110010000001101111011011100010000001" +
            "1101000110100001100101001000000111010101110011011000" +
            "0101100111011001010010000001101111011001100010000001" +
            "1101010110111001110101011100110110010101100100001000" +
            "0001110011011100000110010101100011011101000111001001" +
            "1101010110110100100000011011110110011000100000011101" +
            "0001101000011001010010000001000110010011010010000001" +
            "1000100111001001101111011000010110010001100011011000" +
            "0101110011011101000010000001110100011011110010000001" +
            "1000110110000101110010011100100111100100100000011101" +
            "0001100101011110000111010001110101011000010110110000" +
            "1000000110000101101110011001000010000001110000011010" +
            "0101100011011101000110111101110010011010010110000101" +
            "1011000010000001100100011000010111010001100001001000" +
            "0001101001011011100010000001110010011001010110011101" +
            "1010010110111101101110011000010110110000100000011000" +
            "0101101110011001000010000001101110011000010111010001" +
            "1010010110111101101110011000010110110000100000011011" +
            "0001100001011011100110011101110101011000010110011101" +
            "1001010111001100100000011101000110111100100000011101" +
            "0001101000011001010010000001100011011011110110110101" +
            "1011010110111101101110001000000110110101100001011011" +
            "1000101110000011010000101000001101000010100100000101" +
            "1010010101001001110000011000010111000001100101011100" +
            "1000100000011000010110100101101101011100110010000001" +
            "1101000110111100100000011010110110010101100101011100" +
            "0000100000011101000110100001100101001000000110001101" +
            "1010010111010001101001011110100110010101101110011100" +
            "1100100000011011110110011000100000010010010110111001" +
            "1001000110100101100001001000000110100101101110011001" +
            "1001101111011100100110110101100101011001000010000001" +
            "1011110110111000100000011101100110000101110010011010" +
            "0101101111011101010111001100100000011001110110111101" +
            "1101100111010000100000011100110110001101101000011001" +
            "0101101101011001010111001100101100001000000111000001" +
            "1011110110110001101001011000110110100101100101011100" +
            "1100100000011000010110111001100100001000000110010001" +
            "1001010111011001100101011011000110111101110000011011" +
            "0101100101011011100111010000100000011000010110001101" +
            "1101000110100101110110011010010111010001101001011001" +
            "0101110011001011000010000001110100011010000110000101" +
            "1101000010000001110100011010000110010100100000011001" +
            "1101101111011101100110010101110010011011100110110101" +
            "1001010110111001110100001000000110100001100001011100" +
            "1100100000011100100110111101101100011011000110010101" +
            "1001000010000001101111011101010111010000100000011010" +
            "0101101110001000000111001001100101011000010110110000" +
            "1000000111010001101001011011010110010100101100001000" +
            "0001100001011011100110010000100000011000010110110001" +
            "1011000010000001110100011010000110010101110011011001" +
            "0100100000011010010110111001100110011011110111001001" +
            "1011010110000101110100011010010110111101101110001000" +
            "0001101001011100110010000001110000011100100110111101" +
            "1101100110100101100100011001010110010000100000011001" +
            "1001101111011100100010000001100110011100100110010101" +
            "1001010010000001110100011011110010000001110100011010" +
            "0001100101001000000110001101101001011101000110100101" +
            "1110100110010101101110011100110010000001101001011011" +
            "1000100000011001010110000101100011011010000010000001" +
            "1000010110111001100100001000000110010101110110011001" +
            "0101110010011110010010000001100011011011110111001001" +
            "1011100110010101110010001000000110111101100110001000" +
            "0001110100011010000110010100100000011000110110111101" +
            "1101010110111001110100011100100111100100100000011101" +
            "1101101001011101000110100001101111011101010111010000" +
            "1000000110100101101110011101000110010101110010011011" +
            "100110010101110100001011100000110100001010";


    /**
     * Constructor. Creates a new demodulator block reading its samples from the given input queue and
     * returning the buffers to the given output queue. Expects input samples to be at baseband (mixing
     * is done by the scheduler)
     *
     * @param inputQueue  Queue that delivers received baseband signals
     * @param outputQueue Queue to return used buffers from the inputQueue
     * @param packetSize  Size of the packets in the input queue
     */
    public Demodulator(ArrayBlockingQueue<SamplePacket> inputQueue, ArrayBlockingQueue<SamplePacket> outputQueue, int packetSize) {
        // Create internal sample buffers:
        // Note that we create the buffers for the case that there is no downsampling necessary
        // All other cases with input decimation > 1 are also possible because they only need
        // smaller buffers.
        this.quadratureSamples = new SamplePacket(packetSize);

        // Create Audio Sink
        this.audioSink = new AudioSink(packetSize, AUDIO_RATE);

        // Create Decimator block
        // Note that the decimator directly reads from the inputQueue and also returns processed packets to the
        // output queue.
        this.decimator = new Decimator(QUADRATURE_RATE[demodulationMode], packetSize, inputQueue, outputQueue);
    }

    public static String bytesToString(String bytes) {
        int i = bytes.length() / 8;
        int pos = 0;
        String result = "";
        byte[] buffer = new byte[i];
        for (int j = 0; j < i; j++) {
            String temp = bytes.substring(pos, pos + 8);
            buffer[j] = (byte) Integer.parseInt(temp, 2);
            pos += 8;
        }
        try {
            result = new String(buffer, "ISO-8859-1");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        System.out.println("Result: " + result);
        return result;
    }

    /**
     * @return Demodulation Mode (DEMODULATION_OFF, *_AM, *_NFM, *_WFM, ...)
     */
    public int getDemodulationMode() {
        return demodulationMode;
    }

    /**
     * Sets a new demodulation mode. This can be done while the demodulator is running!
     * Will automatically adjust internal sample rate conversions and the user filter
     * if necessary
     *
     * @param demodulationMode Demodulation Mode (DEMODULATION_OFF, *_AM, *_NFM, *_WFM, ...)
     */
    public void setDemodulationMode(int demodulationMode) {
        if (demodulationMode > 6 || demodulationMode < 0) {
            Log.e(LOGTAG, "setDemodulationMode: invalid mode: " + demodulationMode);
            return;
        }
        this.decimator.setOutputSampleRate(QUADRATURE_RATE[demodulationMode]);
        this.demodulationMode = demodulationMode;
        this.userFilterCutOff = (MAX_USER_FILTER_WIDTH[demodulationMode] + MIN_USER_FILTER_WIDTH[demodulationMode]) / 2;
    }

    /**
     * Will set the cut off frequency of the user filter
     *
     * @param channelWidth channel width (single side) in Hz
     * @return true if channel width is valid, false if out of range
     */
    public boolean setChannelWidth(int channelWidth) {
        if (channelWidth < MIN_USER_FILTER_WIDTH[demodulationMode] || channelWidth > MAX_USER_FILTER_WIDTH[demodulationMode])
            return false;
        this.userFilterCutOff = channelWidth;
        return true;
    }

    /**
     * @return Current width (cut-off frequency - one sided) of the user filter
     */
    public int getChannelWidth() {
        return userFilterCutOff;
    }

    /**
     * Starts the thread. This thread will start 2 more threads for decimation and audio output.
     * These threads are managed by the Demodulator and terminated, when the Demodulator thread
     * terminates.
     */
    @Override
    public synchronized void start() {
        stopRequested = false;
        super.start();
    }

    /**
     * Stops the thread
     */
    public void stopDemodulator() {
        stopRequested = true;
    }

    @Override
    public void run() {
        SamplePacket inputSamples = null;
        SamplePacket audioBuffer = null;
        SamplePacket zaudioBuffer = null;

        Log.i(LOGTAG, "Demodulator started. (Thread: " + this.getName() + ")");

        // Start the audio sink thread:
        audioSink.start();

        // Start decimator thread:
        decimator.start();

        boolean continue_while = false;
        while (!stopRequested) {

            // Get downsampled packet from the decimator:
            inputSamples = decimator.getDecimatedPacket(1000);

            // Verify the input sample packet is not null:
            if (inputSamples == null) {
                //Log.d(LOGTAG, "run: Decimated sample is null. skip this round...");
                continue;
            }

            // filtering		[sample rate is QUADRATURE_RATE]
            applyUserFilter(inputSamples, quadratureSamples);        // The result from filtering is stored in quadratureSamples

            // return input samples to the decimator block:
            decimator.returnDecimatedPacket(inputSamples);

            // get buffer from audio sink
            audioBuffer = audioSink.getPacketBuffer(1000);
            zaudioBuffer = new SamplePacket(new float[audioBuffer.size()], new float[audioBuffer.size()], 250000, audioBuffer.size());

            // demodulate		[sample rate is QUADRATURE_RATE]
            switch (demodulationMode) {
                case DEMODULATION_OFF:
                    break;

                case DEMODULATION_AM:
                    demodulateAM(quadratureSamples, audioBuffer);
                    break;

                case DEMODULATION_NFM:
                    demodulateFM(quadratureSamples, audioBuffer, 5000);
                    break;

                case DEMODULATION_WFM:
                    demodulateFM(quadratureSamples, audioBuffer, 75000);
                    // mydemodulateFM(audioBuffer, zaudioBuffer);
                    /* no-op */
                    break;

                case DEMODULATION_FSK:
                    demodulateFM_FSK(quadratureSamples, audioBuffer, 75000);
                    /* no-op */
                    // continue_while=true;
                    break;

                case DEMODULATION_LSB:
                    demodulateSSB(quadratureSamples, audioBuffer, false);
                    break;

                case DEMODULATION_USB:
                    demodulateSSB(quadratureSamples, audioBuffer, true);
                    break;

                default:
                    Log.e(LOGTAG, "run: invalid demodulationMode: " + demodulationMode);
            }

            if (continue_while)
                continue; // If FSK don't Play the Sound


            // lets band pass filter this
            // We have to (re-)create the band pass filter:
            // play audio		[sample rate is QUADRATURE_RATE]
            audioSink.enqueuePacket(audioBuffer);
        }

        // Stop the audio sink thread:
        audioSink.stopSink();

        // Stop the decimator thread:
        decimator.stopDecimator();

        this.stopRequested = true;
        Log.i(LOGTAG, "Demodulator stopped. (Thread: " + this.getName() + ")");
    }

    /**
     * Will filter the samples in input according to the user filter settings.
     * Filtered samples are stored in output. Note: All samples in output
     * will always be overwritten!
     *
     * @param input  incoming (unfiltered) samples
     * @param output outgoing (filtered) samples
     */
    private void applyUserFilter(SamplePacket input, SamplePacket output) {
        // Verify that the filter is still correct configured:
        if (userFilter == null || ((int) userFilter.getCutOffFrequency()) != userFilterCutOff) {
            // We have to (re-)create the user filter:
            this.userFilter = FirFilter.createLowPass(1,
                    1,
                    input.getSampleRate(),
                    userFilterCutOff,
                    input.getSampleRate() * 0.10f,
                    USER_FILTER_ATTENUATION);
            if (userFilter == null)
                return;    // This may happen if input samples changed rate or demodulation was turned off. Just skip the filtering.
            Log.d(LOGTAG, "applyUserFilter: created new user filter with " + userFilter.getNumberOfTaps()
                    + " taps. Decimation=" + userFilter.getDecimation() + " Cut-Off=" + userFilter.getCutOffFrequency()
                    + " transition=" + userFilter.getTransitionWidth());
        }
        output.setSize(0);    // mark buffer as empty
        if (userFilter.filter(input, output, 0, input.size()) < input.size()) {
            Log.e(LOGTAG, "applyUserFilter: could not filter all samples from input packet.");
        }
    }

    /**
     * FSK Direct Demodulation
     */
    private void demodulateFM_FSK(SamplePacket input, SamplePacket output, int maxDeviation) {

        float[] reIn = input.re();
        float[] imIn = input.im();
        float[] reOut = output.re();
        float[] imOut = output.im();
        int inputSize = input.size();
        float quadratureGain = QUADRATURE_RATE[demodulationMode] / (2 * (float) Math.PI * maxDeviation);

        if (demodulatorHistory == null) {
            demodulatorHistory = new SamplePacket(1);
            demodulatorHistory.re()[0] = reIn[0];
            demodulatorHistory.im()[0] = reOut[0];
        }

        // Quadrature demodulation:
        reOut[0] = reIn[0] * demodulatorHistory.re(0) + imIn[0] * demodulatorHistory.im(0);
        imOut[0] = imIn[0] * demodulatorHistory.re(0) - reIn[0] * demodulatorHistory.im(0);
        reOut[0] = quadratureGain * (float) Math.atan2(imOut[0], reOut[0]);


        for (int i = 1; i < inputSize; i++) {
            reOut[i] = reIn[i] * reIn[i - 1] + imIn[i] * imIn[i - 1];
            imOut[i] = imIn[i] * reIn[i - 1] - reIn[i] * imIn[i - 1];
            reOut[i] = quadratureGain * (float) Math.atan2(imOut[i], reOut[i]);
            imOut[i] = 0;
//			Log.e(LOGTAG, (Float.toString(reOut[i])) + " " + Float.toString(imOut[i]));
        }
        demodulatorHistory.re()[0] = reIn[inputSize - 1];
        demodulatorHistory.im()[0] = imIn[inputSize - 1];
        output.setSize(inputSize);
        output.setSampleRate(QUADRATURE_RATE[demodulationMode]);


//        Acknowledge last cut off and create non-drifting window for the averaging to calculate
//        the bit value in the stream...
        // while zero crossing start the window and take samples...
        // decimate - lpf the values
        // store the bit streams
        // decode the data back...

        int i;
        byte[] stream_bits_smp = new byte[inputSize];

        for (i = 0; i < inputSize; i++) {
            if (output.re()[i] > 0)
                stream_bits_smp[i] = 1;
            else
                stream_bits_smp[i] = 0;
        }

//        int startpoint = 0;
//        // resample data to represent just right amount of bits and their values.
//        for (i = 5; i < inputSize - 5; i++) {
//            if (stream_bits_smp[i - 5] & stream_bits_smp[i - 4] & stream_bits_smp[i - 3] & stream_bits_smp[i - 2] & stream_bits_smp[i - 1])
//                if (!stream_bits_smp[i + 5] & !stream_bits_smp[i + 4] & !stream_bits_smp[i + 3] & !stream_bits_smp[i + 2] & !stream_bits_smp[i + 1]) {
//                    startpoint = i;
//                    break;
//                }
//            if (!stream_bits_smp[i - 5] & !stream_bits_smp[i - 4] & !stream_bits_smp[i - 3] & !stream_bits_smp[i - 2] & !stream_bits_smp[i - 1])
//                if (stream_bits_smp[i + 5] & stream_bits_smp[i + 4] & stream_bits_smp[i + 3] & stream_bits_smp[i + 2] & stream_bits_smp[i + 1]) {
//                    startpoint = i;
//                    break;
//                }
//
//        }

//        Log.e(LOGTAG, "Start Point in the stream : " + Arrays.toString(stream_bits_smp) + " .");

        Stream_Data = "";
//        int j;
//        for(j=0;j<Stream_Bin_Data.length();j=j+8)
//        {
//            byte charCode = (byte) Integer.parseInt(Stream_Bin_Data.substring(j, j + 7), 2);
//            Stream_Data = Stream_Data + new Character((char)charCode).toString();
//        }
//
//        Log.e(LOGTAG, "Decrypted Text : " + Stream_Data + " .");
//
//        byte[] bytes = "Any String you want".getBytes();
//
//        String text = "empty";
//        try {
//            text = new String(bytes, 0, bytes.length, "ASCII");
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }
//
////        Stream_Data += "";


//        Stream_Data = bytesToString(Stream_Bin_Data);
        try {
            Stream_Data = new String(stream_bits_smp, "ASCII"); // for UTF-8 encoding
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        if (true) {
            try {
                String sFileName = "DumpedVal_FSK.txt";

                File root = new File(Environment.getExternalStorageDirectory(), "Notes");
                if (!root.exists()) {
                    root.mkdirs();
                }

                File myFile = new File(root, sFileName);

                if (myFile.exists()) {
                    try {
                        FileOutputStream fOut = new FileOutputStream(myFile);
                        OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
//                        myOutWriter.append(",output.size:"+output.size()+",output.capacity:"+output.capacity()+
//                                "="+Arrays.toString(output.re()));

                        myOutWriter.append(Arrays.toString(stream_bits_smp));
//                    myOutWriter.append(Stream_Data);
//                    myOutWriter.append(text);

                        myOutWriter.close();
                        fOut.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    myFile.createNewFile();
                    try {
                        FileOutputStream fOut = new FileOutputStream(myFile);
                        OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
//                        myOutWriter.append(",output.size:"+output.size()+",output.capacity:"+output.capacity()+
//                                "="+Arrays.toString(output.re()));

                        myOutWriter.append(Arrays.toString(stream_bits_smp));
                        myOutWriter.close();
                        fOut.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }

    /**
     * Will FM demodulate the samples in input. Use ~75000 deviation for wide band FM
     * and ~3000 deviation for narrow band FM.
     * Demodulated samples are stored in the real array of output. Note: All samples in output
     * will always be overwritten!
     *
     * @param input  incoming (modulated) samples
     * @param output outgoing (demodulated) samples
     */
    private void demodulateFM(SamplePacket input, SamplePacket output, int maxDeviation) {

        float[] reIn = input.re();
        float[] imIn = input.im();
        float[] reOut = output.re();
        float[] imOut = output.im();
        int inputSize = input.size();
        float quadratureGain = QUADRATURE_RATE[demodulationMode] / (2 * (float) Math.PI * maxDeviation);

        if (demodulatorHistory == null) {
            demodulatorHistory = new SamplePacket(1);
            demodulatorHistory.re()[0] = reIn[0];
            demodulatorHistory.im()[0] = reOut[0];
        }

        // Quadrature demodulation:
        reOut[0] = reIn[0] * demodulatorHistory.re(0) + imIn[0] * demodulatorHistory.im(0);
        imOut[0] = imIn[0] * demodulatorHistory.re(0) - reIn[0] * demodulatorHistory.im(0);
        reOut[0] = quadratureGain * (float) Math.atan2(imOut[0], reOut[0]);


        for (int i = 1; i < inputSize; i++) {
            reOut[i] = reIn[i] * reIn[i - 1] + imIn[i] * imIn[i - 1];
            imOut[i] = imIn[i] * reIn[i - 1] - reIn[i] * imIn[i - 1];
            reOut[i] = quadratureGain * (float) Math.atan2(imOut[i], reOut[i]);
            imOut[i] = 0;
//			Log.e(LOGTAG, (Float.toString(reOut[i])) + " " + Float.toString(imOut[i]));
        }
        demodulatorHistory.re()[0] = reIn[inputSize - 1];
        demodulatorHistory.im()[0] = imIn[inputSize - 1];
        output.setSize(inputSize);
        output.setSampleRate(QUADRATURE_RATE[demodulationMode]);

    }

    private void mydemodulateFM(SamplePacket input, SamplePacket output) {

        // Take the Input and apply a band pass filter

        // complex band pass:
        if (mybandPassFilter == null) {

            // We have to (re-)create the band pass filter:
            mybandPassFilter = ComplexFirFilter.createBandPass(1,        // Decimate by 2; => AUDIO_RATE
                    1,
                    input.getSampleRate(),
                    58000f,
                    78000f,
                    input.getSampleRate() * 0.01f,
                    10);
            if (mybandPassFilter == null) {
                Log.d(LOGTAG, "input samples changed rate or demodulation was turned off. ");
                return;    // This may happen if input samples changed rate or demodulation was turned off. Just skip the filtering.
            }
            Log.d(LOGTAG, "FSK_Demod: created new band pass filter with " + mybandPassFilter.getNumberOfTaps()
                    + " taps. Decimation=" + mybandPassFilter.getDecimation() + " Low-Cut-Off=" + mybandPassFilter.getLowCutOffFrequency()
                    + " High-Cut-Off=" + mybandPassFilter.getHighCutOffFrequency() + " transition=" + mybandPassFilter.getTransitionWidth());
        }
        output.setSize(0);    // mark buffer as empty
        if (mybandPassFilter.filter(input, output, 0, input.size()) < input.size()) {
            Log.e(LOGTAG, "FSK_Demod: could not filter  all samples from input packet.");
        }


        output = input;

        try {
            String sFileName = "DumpFilteredData";

            File root = new File(Environment.getExternalStorageDirectory(), "Notes");
            if (!root.exists()) {
                root.mkdirs();
            }

            File myFile = new File(root, "DumpedVal.csv");

            if (myFile.exists()) {
                try {
                    FileOutputStream fOut = new FileOutputStream(myFile);
                    OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
//                        myOutWriter.append(",output.size:"+output.size()+",output.capacity:"+output.capacity()+
//                                "="+Arrays.toString(output.re()));

                    myOutWriter.append(Arrays.toString(output.re()));
                    myOutWriter.close();
                    fOut.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                myFile.createNewFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    /**
     * Will AM demodulate the samples in input.
     * Demodulated samples are stored in the real array of output. Note: All samples in output
     * will always be overwritten!
     *
     * @param input  incoming (modulated) samples
     * @param output outgoing (demodulated) samples
     */
    private void demodulateAM(SamplePacket input, SamplePacket output) {
        float[] reIn = input.re();
        float[] imIn = input.im();
        float[] reOut = output.re();
        float avg = 0;
        lastMax *= 0.95;    // simplest AGC

        // Complex to magnitude
        for (int i = 0; i < input.size(); i++) {
            reOut[i] = (reIn[i] * reIn[i] + imIn[i] * imIn[i]);
            avg += reOut[i];
            if (reOut[i] > lastMax)
                lastMax = reOut[i];
        }
        avg = avg / input.size();

        // normalize values:
        float gain = 0.75f / lastMax;
        for (int i = 0; i < output.size(); i++)
            reOut[i] = (reOut[i] - avg) * gain;

        output.setSize(input.size());
        output.setSampleRate(QUADRATURE_RATE[demodulationMode]);
    }

    /**
     * Will SSB demodulate the samples in input.
     * Demodulated samples are stored in the real array of output. Note: All samples in output
     * will always be overwritten!
     *
     * @param input     incoming (modulated) samples
     * @param output    outgoing (demodulated) samples
     * @param upperBand if true: USB; if false: LSB
     */
    private void demodulateSSB(SamplePacket input, SamplePacket output, boolean upperBand) {
        float[] reOut = output.re();

        // complex band pass:
        if (bandPassFilter == null
                || (upperBand && (((int) bandPassFilter.getHighCutOffFrequency()) != userFilterCutOff))
                || (!upperBand && (((int) bandPassFilter.getLowCutOffFrequency()) != -userFilterCutOff))) {
            // We have to (re-)create the band pass filter:
            this.bandPassFilter = ComplexFirFilter.createBandPass(2,        // Decimate by 2; => AUDIO_RATE
                    1,
                    input.getSampleRate(),
                    upperBand ? 200f : -userFilterCutOff,
                    upperBand ? userFilterCutOff : -200f,
                    input.getSampleRate() * 0.01f,
                    BAND_PASS_ATTENUATION);
            if (bandPassFilter == null)
                return;    // This may happen if input samples changed rate or demodulation was turned off. Just skip the filtering.
            Log.d(LOGTAG, "demodulateSSB: created new band pass filter with " + bandPassFilter.getNumberOfTaps()
                    + " taps. Decimation=" + bandPassFilter.getDecimation() + " Low-Cut-Off=" + bandPassFilter.getLowCutOffFrequency()
                    + " High-Cut-Off=" + bandPassFilter.getHighCutOffFrequency() + " transition=" + bandPassFilter.getTransitionWidth());
        }
        output.setSize(0);    // mark buffer as empty
        if (bandPassFilter.filter(input, output, 0, input.size()) < input.size()) {
            Log.e(LOGTAG, "demodulateSSB: could not filter all samples from input packet.");
        }

        // gain control: searching for max:
        lastMax *= 0.95;    // simplest AGC
        for (int i = 0; i < output.size(); i++) {
            if (reOut[i] > lastMax)
                lastMax = reOut[i];
        }
        // normalize values:
        float gain = 0.75f / lastMax;
        for (int i = 0; i < output.size(); i++)
            reOut[i] *= gain;
    }
}
