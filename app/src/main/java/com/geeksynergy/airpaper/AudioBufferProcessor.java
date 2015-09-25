package com.geeksynergy.airpaper;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder.AudioSource;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.concurrent.LinkedBlockingQueue;

public class AudioBufferProcessor extends Thread {

    public final static String sdrPATH = "/data/data/com.geeksynergy.airpaper/sdr_pipe";
    public final static String TAG = "SDR_PIPE";

    static {
        System.loadLibrary("decomon");
    }

    private final LinkedBlockingQueue<short[]> queue;
    private final LinkedBlockingQueue<char[]> charqueue;
    float[] fbuf = new float[54000]; //    float[] fbuf = new float[16384];

    int _dumpCount = 1024;
    // for debugging the caputured samples
    // sox -e signed -r 22050 -b 16 sambombo.raw output2.wav
    FileOutputStream _fos;
    File _f = new File("/sdcard/PacketDroidSamples.raw");
    private AudioIn audioIn = new AudioIn();
    private PacketCallback callback;
    private boolean inited = false;
    private int fbuf_cnt = 0;
    private int overlap = 18; // overlap 18 for AFSK DEMOD (FREQSAMP / BAUDRATE) //AFSK needs 18
    private boolean writeAudioBuffer = false; // for debug
    private boolean direct_audio = false; //for raw debug
    private boolean sdr_demod = true; //for SDR debug

    public AudioBufferProcessor(PacketCallback cb) {
        super("AudioBufferProcessor");
        queue = new LinkedBlockingQueue<short[]>();
        charqueue = new LinkedBlockingQueue<char[]>();

        callback = cb;

        if (writeAudioBuffer) {
            try {
                _fos = new FileOutputStream(_f);
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    native void init();

    native void processBuffer(float[] buf, int length);

    native void processBuffer2(byte[] buf);


//	public void read() {
//		if (!inited) { inited = true; init(); } // init native demodulators
//		if (!audioIn.isAlive()) audioIn.start();
//		
//		while (true) {
//			try {
//				decode(queue.take());
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
//	}

    @Override
    public void run() {
        if (!inited) {
            inited = true;
            init();
        } // init native demodulators
        if (!audioIn.isAlive()) audioIn.start();

        while (true) {
            try {
                decode(queue.take());
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public void startRecording() {
        audioIn.recorder.startRecording();
    }

    public void stopRecording() {
        audioIn.close();
        queue.clear();
        charqueue.clear();
    }


    void decode(short[] s) {
        //  Log.d(MainActivity.LOG_TAG, "CALLBACK!: " + s.length);
        for (int i = 0; i < s.length; i++) {
            if (writeAudioBuffer) {
                try {
                    _fos.write(s[i] & 0xFF);
                    _fos.write((s[i] >> 8) & 0xFF);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            fbuf[fbuf_cnt++] = s[i] * (1.0f / 32768.0f);
        }


        if (fbuf_cnt > overlap) {
            processBuffer(fbuf, fbuf_cnt - overlap);
            // Log.d(MainActivity.LOG_TAG, "processBuffer called!");
            System.arraycopy(fbuf, fbuf_cnt - overlap, fbuf, 0, overlap);
            fbuf_cnt = overlap;
        }
    }

    public void callback(byte[] data) {
        // Log.d(MainActivity.LOG_TAG, "called callback: " + new String(data));
        callback.received(data);
    }

    // taken from: http://stackoverflow.com/questions/4525206/android-audiorecord-class-process-live-mic-audio-quickly-set-up-callback-func
    public class AudioIn extends Thread {
        private AudioRecord recorder;

        private short[][] buffers = new short[256][8192];
        private byte[][] bytes_buffers = new byte[256][8192 * 2];
        private char[][] chars_buffers = new char[256][8192 * 2];

        public AudioIn() {
            super("AudioIn");
            android.os.Process
                    .setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
            recorder = new AudioRecord(AudioSource.MIC, 22050,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT, 16384);
        }

        @Override
        public void run() {

            int ix = 0;

            if (direct_audio) {
                try {
                    recorder.startRecording();
                    Log.w(MainActivity.LOG_TAG, "Transcoding Mic Data Now");

                    while (true) {
                        if (recorder.getRecordingState() == AudioRecord.RECORDSTATE_STOPPED) {
                            Thread.sleep(100);
                            continue;
                        }


                        int nRead = 0;
                        short[] buffer = buffers[ix++ % buffers.length];

                        nRead = recorder.read(buffer, 0, buffer.length);
                        // Log.d(MainActivity.LOG_TAG, Arrays.toString(buffer).length() + ":  " + Arrays.toString(buffer));

                        queue.put(buffer);
                        //process(buffer);
                    }
//                    Log.w(MainActivity.LOG_TAG, "Data Transcoding Ended");

                } catch (Throwable x) {
                    Log.w(MainActivity.LOG_TAG, "Error reading audio", x);
                }
            } else if (sdr_demod) {


                try {

                    recorder.startRecording();

                    Log.w(MainActivity.LOG_TAG, "sdr_direct_decodeStarted");
                    File sdcard = Environment.getExternalStorageDirectory();
                    FileChannel out = new FileOutputStream(new File(sdcard, "airpaper_rx_direct.raw")).getChannel();


                    int concat_packets = 0;
                    while (true) {

                        if (!MainActivity.bQueue.isEmpty() & MainActivity.decod_string)
                        {
                            buffer_packet samp_pack = MainActivity.bQueue.take();
                            queue.put(samp_pack.payload);
                        }
                        else
                        {
                            continue;
                        }

                        if (false) // never happening
                            break;
                    }
                    out.close();


                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            else {


                recorder.startRecording();

                try {
                    Log.w(MainActivity.LOG_TAG, "Transcoding Raw Data Now");

                   //recorder.startRecording();
                    File sdcard = Environment.getExternalStorageDirectory();
                    //Get the file currently static
                    //File file = new File(sdcard, "poc1200_triple_quick.raw"); // this goes to storage/emulated/0/poc1200_triple_quick.raw  other files AFSK_1200_baud.raw , speedcall-dtmf.raw
                    File file = new File(sdcard, "airpaper_rx_raw31250.raw"); // this goes to storage/emulated/0/poc1200_triple_quick.raw  other files AFSK_1200_baud.raw , speedcall-dtmf.raw
//                    File file = new File(sdcard, "airpaper_rx_raw2.raw"); // this goes to storage/emulated/0/poc1200_triple_quick.raw  other files AFSK_1200_baud.raw , speedcall-dtmf.raw
//                    File file = new File(sdcard, "airpaper_rx_raw3.raw"); // this goes to storage/emulated/0/poc1200_triple_quick.raw  other files AFSK_1200_baud.raw , speedcall-dtmf.raw
//                    File file = new File(sdcard, "aio_trans_441_IT.raw"); // this goes to storage/emulated/0/poc1200_triple_quick.raw  other files AFSK_1200_baud.raw , speedcall-dtmf.raw
                    // File file = new File(sdcard, "poc1200_triple_quick.raw"); // lets implement 2400baud airpaper_rx_raw.raw

                    FileInputStream fis = new FileInputStream(file);
                    FileChannel out = new FileOutputStream(new File(sdcard, "airpaper_rx_raw2_recoded.raw")).getChannel();

                    while (true) {

                        byte[] buffer_bytes = bytes_buffers[ix++ % bytes_buffers.length];
                        int nRead = fis.read(buffer_bytes, 0, buffer_bytes.length);
                        if (nRead < 0)
                            break;
                        int size = buffer_bytes.length;
                        short[] shorts = new short[buffer_bytes.length / 2];
                        //   to turn bytes to shorts as either big endian or little endian.
                        ByteBuffer.wrap(buffer_bytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shorts);
                        // Log.d(MainActivity.LOG_TAG, "ShortsRead :" + nRead / 2 + " : " + Arrays.toString(shorts));


                        ByteBuffer myByteBuffer = ByteBuffer.allocate(shorts.length * 2);
                        myByteBuffer.order(ByteOrder.LITTLE_ENDIAN);
                        ShortBuffer myShortBuffer = myByteBuffer.asShortBuffer();
                        myShortBuffer.put(shorts);
                        out.position(out.size());
                        out.write(myByteBuffer);
                        queue.put(shorts);
                    }
                    out.close();


                    Log.w(MainActivity.LOG_TAG, "Data Transcoding Ended");

                    //process(buffer);
                } catch (Exception x) {
                    Log.w(MainActivity.LOG_TAG, x.toString());
                }

            }

        }


        private void close() {
            if (recorder != null) recorder.stop();
            Log.d(MainActivity.LOG_TAG, "AudioIn: close");
        }

    }
}
