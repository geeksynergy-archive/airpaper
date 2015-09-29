package com.geeksynergy.airpaper;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import net.ab0oo.aprs.parser.Parser;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;

public class MainActivity extends AppCompatActivity implements IQSourceInterface.Callback, AnalyzerSurface.CallbackInterface, PacketCallback {
    public static final int RTL2832U_RESULT_CODE = 1234;    // arbitrary value, used when sending intent to RTL2832U

    static ArrayBlockingQueue<buffer_packet> bQueue = new ArrayBlockingQueue<buffer_packet>(1); // 1 array of short ???

    private static final String LOGTAG = "MainActivity";
    private static final String RECORDING_DIR = "RFAnalyzer";
    private static final int FILE_SOURCE = 0;
    private static final int HACKRF_SOURCE = 1;
    private static final int RTLSDR_SOURCE = 2;
    private static final String[] SOURCE_NAMES = new String[]{"filesource", "hackrf", "rtlsdr"};
    public static String LOG_TAG = "AirPaperMultimonDroid";
    Toolbar toolbar;
    ViewPager pager;
    ViewPagerAdapter adapter;
    SlidingTabLayout tabs;
    String tabTitles[];
    int NumbOfTabs = 8;
    private String PIPE_PATH = "/data/data/com.geksynergy.airpaper/pipe";
    private AudioBufferProcessor abp = null;
    private TextView decod_tv;
    private boolean asci_utf = true; // true is ascii and false is utf16
    private boolean data_metadata = false;
    public static boolean decod_string = false;

    private String Arti_category = "";
    private String Arti_title = "";
    private String Arti_info = "";
    private String Arti_img = "";
    private String Arti_date = "";
    private String Arti_time = "";
    private String nave_str = "";
    private Boolean cont_complete = false;
    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            //Log.d(LOG_TAG, "GOT MESSAGE FROM FILE READER!");
            decod_tv.append(msg.getData().getString("line") + "\n");
//          Latest.latesttext.append(msg.getData().getString("line") + "\n");
            // Verify if the input is ascii or utf16..?
            // presence of transmission type tells what it is ..//
//            if(!asci_utf) {
//                System.out.println("Unicode Values - " + simply_translate((msg.getData().getString("line"))));
//                Latest.latesttext.append((msg.getData().getString("line")) + "\n");
//            }
            Log.d(LOG_TAG, msg.getData().getString("line"));
            if (data_metadata) {
                if (msg.getData().getString("line").startsWith(">>Numeric:")) {
                    try {// Latest.latesttext.append((msg.getData().getString("line")) + "\n");
//                        String tmp_unicode = simply_translate(msg.getData().getString("line").substring((">>Numeric:").length()));
//                        Arti_title = tmp_unicode.substring(0, msg.getData().getString("line").indexOf("|"));
//                        Arti_info = tmp_unicode.substring(msg.getData().getString("line").indexOf("|") + 1);
//                        cont_complete = true;
                    } catch (Exception z) {

                    }
                }
                if (msg.getData().getString("line").startsWith(">>Alpha:data:image/jpeg;base64")) {
                    //Latest.latesttext.append((msg.getData().getString("line")) + "\n");
                    Arti_img = msg.getData().getString("line"); // needs decoding
                } else {
                    if (msg.getData().getString("line").startsWith(">>Alpha: 0000")) {
                        try {
                            //Latest.latesttext.append((msg.getData().getString("line")) + "\n");
                            String tmp_redData = msg.getData().getString("line").substring((">>Alpha: 0000").length());
                            //Log.d(MainActivity.LOG_TAG, tmp_redData);
                            String tmp_unicode = simply_translate(tmp_redData);
                            //Log.d(MainActivity.LOG_TAG, tmp_unicode);
                            Arti_title = tmp_unicode.substring(0, tmp_unicode.indexOf("|"));
                            Arti_info = tmp_unicode.substring(tmp_unicode.indexOf("|") + 1);
//                            try {
//                                JsonFileWriter.putJSONData(Arti_category, "NewMSg", nave_str, Arti_img, Arti_date, Arti_time);
//                            } catch (IOException e) {
//                                e.printStackTrace();
//                            }
//
//                            Log.d(MainActivity.LOG_TAG, "handleMessage " + tmp_unicode);
                            Latest.latesttext.append("category:" + Arti_category + "\n" +
                                            "title: " + Arti_title + "\n" +
                                            "date: " + Arti_date + "\n" +
                                            "time: " + Arti_time + "\n" +
                                            "info: " + Arti_info + "\n" +
                                            "img: " + Arti_img + "\n\n\n"
                            );
                        } catch (Exception z) {

                        }
                    } else if (msg.getData().getString("line").startsWith(">>Alpha:")) {
                        try {
                            //Latest.latesttext.append((msg.getData().getString("line")) + "\n");
                            Arti_title = msg.getData().getString("line").substring((">>Alpha:").length(), msg.getData().getString("line").indexOf("|"));
                            Arti_info = msg.getData().getString("line").substring(msg.getData().getString("line").indexOf("|") + 1);
                            cont_complete = true;
                        } catch (Exception z) {

                        }
                    }
                }
                if (cont_complete == true)// Fire json updater here
                    try {
                        JsonFileWriter.putJSONData(Arti_category, Arti_title, Arti_info, Arti_img, Arti_date, Arti_time);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }


            }
            if (msg.getData().getString("line").startsWith(">>POCSAG2400:")) {
                DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
                DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
                System.out.println(dateFormat.format(new Date())); //2014/08/06 15:59:48
                data_metadata = true;
                if (true) // try switch
                    Arti_category = msg.getData().getString("line").substring((">>POCSAG2400: Address:").length(), msg.getData().getString("line").indexOf("Function:"));  // >>POCSAG2400: Address:
                Arti_date = dateFormat.format(new Date());
                Arti_time = timeFormat.format(new Date());
                cont_complete = false;
            } else {
                data_metadata = false;
                cont_complete = false;
            }

        }
    };


    private Button readButton, stopButton;
    private List<Recycler_preview_Template> persons;
    private RecyclerView rv;
    private MenuItem mi_startStop = null;
    private MenuItem mi_demodulationMode = null;
    private MenuItem mi_record = null;
    private LinearLayout fl_analyzerFrame = null;
    private AnalyzerSurface analyzerSurface = null;
    private AnalyzerProcessingLoop analyzerProcessingLoop = null;
    private IQSourceInterface source = null;
    private Scheduler scheduler = null;
    private Demodulator demodulator = null;
    private SharedPreferences preferences = null;
    private Bundle savedInstanceState = null;
    private Process logcat = null;
    private boolean running = true; // by default this is false
    private File recordingFile = null;
    private int demodulationMode = Demodulator.DEMODULATION_NFM;
    private int MySource;
    private Tracker mTracker;
    private String selected_File;

    private String simply_translate(String str) {
        String ucoded_str = "";
        nave_str = "";
        str.trim();
        if(str.endsWith("<SOH>"))
            str = str.substring(0,str.length()-5);
        Log.d(MainActivity.LOG_TAG, "Length: " + str.length() + ": " + str);
        if (str.length() % 4 == 0)
            for (int i = 0; i < str.length(); i = i + 4)
            {
                ucoded_str += (char) Integer.parseInt(str.substring(i, i + 4), 16);
                nave_str += "\\u" + str.substring(i, i + 4);
            }
        else if (str.length() > 4) {
            str = str.substring(0, str.length() - str.length() % 4);
            for (int i = 0; i < str.length(); i = i + 4)
            {
                ucoded_str += (char) Integer.parseInt(str.substring(i, i + 4), 16);
                nave_str += "\\u" + str.substring(i, i + 4);
            }
        }

        return (ucoded_str);
    }

    private Bitmap simply_img(String str_img) {
        String completeImageData = "data:image/jpeg;base64,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";
        String imageDataBytes = completeImageData.substring(completeImageData.indexOf(",") + 1);
        InputStream stream = new ByteArrayInputStream(Base64.decode(imageDataBytes.getBytes(), Base64.DEFAULT));
        //Latest.latestimg.setImageBitmap(BitmapFactory.decodeStream(stream));
        return BitmapFactory.decodeStream(stream);

    }

    private View.OnClickListener onClickReadButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Log.d(LOG_TAG, "START: Monitor");
            startMonitor();
            MainActivity.decod_string = true;

            v.setEnabled(false);
            stopButton.setEnabled(true);
//
//            try {
//                JsonFileWriter.putJSONData("200058", "Silver is not Gold", "some context here ", Arti_img, Arti_date, Arti_time);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }

        }
    };

    private View.OnClickListener onClickStopButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Log.d(LOG_TAG, "STOP: Monitor");
            stopMonitor();
            MainActivity.decod_string = false;
            v.setEnabled(false);
            readButton.setEnabled(true);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        readButton = (Button) findViewById(R.id.startbutton);
        readButton.setOnClickListener(onClickReadButtonListener);

        stopButton = (Button) findViewById(R.id.stopbutton);
        stopButton.setOnClickListener(onClickStopButtonListener);

        decod_tv = (TextView) findViewById(R.id.decoder_tv);
        decod_tv.setMovementMethod(new ScrollingMovementMethod());

        Log.d(LOG_TAG, "Decomon: OnCreate");


        tabTitles = getResources().getStringArray(R.array.tabTitles);

        toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.mipmap.airpaper);

        // Creating The ViewPagerAdapter and Passing Fragment Manager, Titles for the Tabs and Number Of Tabs.
        adapter = new ViewPagerAdapter(getSupportFragmentManager(), tabTitles, NumbOfTabs);

        // Assigning ViewPager View and setting the adapter
        pager = (ViewPager) findViewById(R.id.pager);
        pager.setAdapter(adapter);
        // Assiging the Sliding Tab Layout View
        tabs = (SlidingTabLayout) findViewById(R.id.tabs);
        tabs.setDistributeEvenly(false); // To make the Tabs Fixed set this true, This makes the tabs Space Evenly in Available width

        // Setting Custom Color for the Scroll bar indicator of the Tab View
        tabs.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @Override
            public int getIndicatorColor(int position) {
                return getResources().getColor(R.color.tabsScrollColor);
            }
        });

        // Setting the ViewPager For the SlidingTabsLayout
        tabs.setViewPager(pager);

        // Google Analytics Begins Here

        AnalyticsApplication application = (AnalyticsApplication) getApplication();
        mTracker = application.getDefaultTracker();

        mTracker.send(new HitBuilders.EventBuilder()
                .setCategory("Action")
                .setAction("Open")
                .build());

        // Google Analytics Ends Here

        MySource = RTLSDR_SOURCE;
//        selected_File= "/sdcard/RFAnalyzer/2015-09-14-20-46-41_rtlsdr_108000000Hz_1000000Sps.iq";
//        selected_File = "/sdcard/RFAnalyzer/2015-09-15-14-41-00_rtlsdr_106968064Hz_1000000Sps.iq";
//        selected_File = "/sdcard/RFAnalyzer/From_all_clip.iq";
        selected_File = "/sdcard/RFAnalyzer/From_all_clip.iq";

        // Set default Settings on first run:
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        // Get reference to the shared preferences:
        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        // Overwrite defaults for file paths in the preferences:
        String extStorage = Environment.getExternalStorageDirectory().getAbsolutePath();    // get the path to the ext. storage
        // File Source file:
        String defaultFile = getString(R.string.pref_filesource_file_default);
        if (preferences.getString(getString(R.string.pref_filesource_file), "").equals(defaultFile))
            preferences.edit().putString(getString(R.string.pref_filesource_file), extStorage + "/" + defaultFile).apply();
        // Log file:
        defaultFile = getString(R.string.pref_logfile_default);
        if (preferences.getString(getString(R.string.pref_logfile), "").equals(defaultFile))
            preferences.edit().putString(getString(R.string.pref_logfile), extStorage + "/" + defaultFile).apply();

        // Start logging if enabled:
        if (preferences.getBoolean(getString(R.string.pref_logging), false)) {
            try {
                File logfile = new File(preferences.getString(getString(R.string.pref_logfile), ""));
                logfile.getParentFile().mkdir();    // Create folder
                logcat = Runtime.getRuntime().exec("logcat -f " + logfile);
                Log.i("MainActivity", "onCreate: started logcat (" + logcat.toString() + ") to " + logfile);
                Toast.makeText(MainActivity.this, "onCreate: started logcat", Toast.LENGTH_SHORT).show();

            } catch (Exception e) {
                Log.e("MainActivity", "onCreate: Failed to start logging!");
                Toast.makeText(MainActivity.this, "onCreate: Failed to start logging", Toast.LENGTH_SHORT).show();

            }
        }

        // Get references to the GUI components:
        fl_analyzerFrame = (LinearLayout) findViewById(R.id.fl_analyzerFrame);
//
//        // Create a analyzer surface:
        analyzerSurface = new AnalyzerSurface(this, this);
        analyzerSurface.setVerticalScrollEnabled(preferences.getBoolean(getString(R.string.pref_scrollDB), true));
        analyzerSurface.setVerticalZoomEnabled(preferences.getBoolean(getString(R.string.pref_zoomDB), true));
        analyzerSurface.setDecoupledAxis(preferences.getBoolean(getString(R.string.pref_decoupledAxis), false));
        analyzerSurface.setDisplayRelativeFrequencies(preferences.getBoolean(getString(R.string.pref_relativeFrequencies), false));
        analyzerSurface.setWaterfallColorMapType(Integer.valueOf(preferences.getString(getString(R.string.pref_colorMapType), "4")));
        analyzerSurface.setFftDrawingType(Integer.valueOf(preferences.getString(getString(R.string.pref_fftDrawingType), "2")));
        analyzerSurface.setFftRatio(Float.valueOf(preferences.getString(getString(R.string.pref_spectrumWaterfallRatio), "0.5")));
        analyzerSurface.setFontSize(Integer.valueOf(preferences.getString(getString(R.string.pref_fontSize), "2")));
        analyzerSurface.setShowDebugInformation(preferences.getBoolean(getString(R.string.pref_showDebugInformation), false));

        // Put the analyzer surface in the analyzer frame of the layout:
        fl_analyzerFrame.addView(analyzerSurface);

        // Restore / Initialize the running state and the demodulator mode:
        if (savedInstanceState != null) {
            running = savedInstanceState.getBoolean(getString(R.string.save_state_running));
            demodulationMode = savedInstanceState.getInt(getString(R.string.save_state_demodulatorMode));
            demodulationMode = Demodulator.DEMODULATION_WFM;
            /* BUGFIX / WORKAROUND:
             * The RTL2832U driver will not allow to close the socket and immediately start the driver
			 * again to reconnect after an orientation change / app kill + restart.
			 * It will report back in onActivityResult() with a -1 (not specified).
			 *
			 * Work-around:
			 * 1) We won't restart the Analyzer if the current source is set to a local RTL-SDR instance:
			 * 2) Delay the restart of the Analyzer after the driver was shut down correctly...
			 */

            //    Toast.makeText(MainActivity.this,"Stopping and restarting may take time",Toast.LENGTH_SHORT).show();
//
            if (running && Integer.valueOf(preferences.getString(getString(R.string.pref_sourceType), "1")) == RTLSDR_SOURCE
                    && !preferences.getBoolean(getString(R.string.pref_rtlsdr_externalServer), false)) {
                // 1) don't start Analyzer immediately
                running = false;

                // Just inform the user about what is going on (why does this take so long? ...)
//                Toast.makeText(MainActivity.this,"Stopping and restarting RTL2832U driver...",Toast.LENGTH_SHORT).show();

                // 2) Delayed start of the Analyzer:
                Thread timer = new Thread() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(1500);
//                            Toast.makeText(MainActivity.this,"Waited for a second and Half",Toast.LENGTH_SHORT).show();
                            startAnalyzer();
                        } catch (InterruptedException e) {
                            Log.e(LOGTAG, "onCreate: (timer thread): Interrupted while sleeping.");
                        }
                    }
                };
                timer.start();
            }

        } else {
            // Set running to true if autostart is enabled (this will start the analyzer in onStart() )
            running = preferences.getBoolean((getString(R.string.pref_autostart)), true); // this was initially false
//            running = true;
//            Toast.makeText(MainActivity.this,"Start Analyzer Failed on First Attempt",Toast.LENGTH_SHORT).show();

        }
        // Set the hardware volume keys to work on the music audio stream:
        setVolumeControlStream(AudioManager.STREAM_MUSIC);


        // Lets get a unicoded string

    }

    private void startMonitor() {
        if (abp == null) {
            abp = new AudioBufferProcessor(this);
            abp.start();
        } else {
            abp.startRecording();
        }
    }

    private void stopMonitor() {
        abp.stopRecording();
    }

    private void startPipeRead() {
        Thread t = new Thread(null, new Runnable() {
            public void run() {
                try {
                    BufferedReader in = new BufferedReader(new FileReader(PIPE_PATH));
                    String line;
                    while (true) {
                        line = in.readLine();
                        if (line != null) {
                            Log.d(LOG_TAG, line);
                            Message msg = Message.obtain();
                            msg.what = 0;
                            Bundle bundle = new Bundle();
                            bundle.putString("line", line);
                            msg.setData(bundle);
                            handler.sendMessage(msg);
                        }

                    }
                } catch (FileNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }
        });
        t.start();

    }


    // PacketCallback interface
    public void received(byte[] data) {
        //Log.d(MainActivity.LOG_TAG, "received packets : " + Arrays.toString(data));
        Message msg = Message.obtain();
        msg.what = 0;
        Bundle bundle = new Bundle();
        String packet;
        try {
            packet = Parser.parseAX25(data).toString();
        } catch (Exception e) {
            packet = ">>" + new String(data);
        }
        bundle.putString("line", packet);
        msg.setData(bundle);
        handler.sendMessage(msg);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        // close source
        if (source != null && source.isOpen())
            source.close();

        // stop logging:
        if (logcat != null) {
            try {
                logcat.destroy();
                logcat.waitFor();
                Log.i(LOGTAG, "onDestroy: logcat exit value: " + logcat.exitValue());
            } catch (Exception e) {
                Log.e(LOGTAG, "onDestroy: couldn't stop logcat: " + e.getMessage());
            }
        }

        // shut down RTL2832U driver if running:
        if (running && Integer.valueOf(preferences.getString(getString(R.string.pref_sourceType), "1")) == RTLSDR_SOURCE
                && !preferences.getBoolean(getString(R.string.pref_rtlsdr_externalServer), false)) {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("iqsrc://-x"));    // -x is invalid. will cause the driver to shut down (if running)
                startActivity(intent);
            } catch (ActivityNotFoundException e) {
                Log.e(LOGTAG, "onDestroy: RTL2832U is not installed");
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
        //Other Way
        //getMenuInflater().inflate(R.menu.menu_main, menu);
        //return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        SoundRecording testFile = new SoundRecording();

        // Handle item selection
        switch (item.getItemId()) {
            case R.id.restart_airpaper:
                //newGame();
                startAnalyzer();
                Toast.makeText(MainActivity.this, "startedAnalyzer()", Toast.LENGTH_LONG).show();
                return true;
            case R.id.action_settings:
                testFile.stopRecording();
                // do some other action here
                return true;
            case R.id.reload_json:
                JSONFileReader fileReader = new JSONFileReader();
                fileReader.getJSONData(this, getApplicationContext());
                return true;

            case R.id.Source:
                if (item.getTitle().equals("RTL_SDR SRC")) {
                    item.setTitle("IQ_FILE SRC");
                    MySource = RTLSDR_SOURCE;
                } else {
                    item.setTitle("RTL_SDR SRC");
                    MySource = FILE_SOURCE;
                }
                onStart();
                return true;

            case R.id.Show_Analyzer:
                if (item.getTitle().equals("Show Analyzer")) {
                    item.setTitle("Hide Analyzer");
                    fl_analyzerFrame.setVisibility(View.VISIBLE);
                } else {
                    item.setTitle("Show Analyzer");
                    fl_analyzerFrame.setVisibility(View.INVISIBLE);
                }
                return true;

            case R.id.create_pdf:
                PdfCreator creator = new PdfCreator();
                creator.createPdf(this, getApplicationContext());
                return true;
            case R.id.FM_MOD:
                if (item.getTitle().equals("WideBandFM")) {
                    item.setTitle("NarrowBandFM");
                    setDemodulationMode(Demodulator.DEMODULATION_WFM);
                } else {
                    item.setTitle("WideBandFM");
                    setDemodulationMode(Demodulator.DEMODULATION_NFM);
                }
                onStart();

                return true;
            case R.id.record_sound:
//                Thread stoprecorder = new Thread() {
//                    @Override
//                    public void run() {
//                        try {
//                            SoundRecording testFile = new SoundRecording();
//                            testFile.startRecording();
//                            Thread.sleep(100000);
//                            testFile.stopRecording();
//                        } catch (InterruptedException e) {
//                            Log.e(LOGTAG, "onCreate: (timer thread): Interrupted while sleeping.");
//                        }
//                    }
//                };
//                stoprecorder.start();
//                testFile.startRecording();

                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }


    /**
     * Will update the action bar icons and titles according to the current app state
     */
    private void updateActionBar() {
//
//        this.runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                // Set title and icon of the start/stop button according to the state:
//                if(mi_startStop != null) {
//                    if (running) {
//                        mi_startStop.setTitle(R.string.action_stop);
//                        mi_startStop.setIcon(R.drawable.ic_action_pause);
//                    } else {
//                        mi_startStop.setTitle(R.string.action_start);
//                        mi_startStop.setIcon(R.drawable.ic_action_play);
//                    }
//                }
//
//                // Set title and icon for the demodulator mode button
//                if(mi_demodulationMode != null) {
//                    int iconRes;
//                    int titleRes;
//                    switch (demodulationMode) {
//                        case Demodulator.DEMODULATION_OFF:
//                            iconRes = R.drawable.ic_action_demod_off;
//                            titleRes = R.string.action_demodulation_off;
//                            break;
//                        case Demodulator.DEMODULATION_AM:
//                            iconRes = R.drawable.ic_action_demod_am;
//                            titleRes = R.string.action_demodulation_am;
//                            break;
//                        case Demodulator.DEMODULATION_NFM:
//                            iconRes = R.drawable.ic_action_demod_nfm;
//                            titleRes = R.string.action_demodulation_nfm;
//                            break;
//                        case Demodulator.DEMODULATION_WFM:
//                            iconRes = R.drawable.ic_action_demod_wfm;
//                            titleRes = R.string.action_demodulation_wfm;
//                            break;
//                        case Demodulator.DEMODULATION_LSB:
//                            iconRes = R.drawable.ic_action_demod_lsb;
//                            titleRes = R.string.action_demodulation_lsb;
//                            break;
//                        case Demodulator.DEMODULATION_USB:
//                            iconRes = R.drawable.ic_action_demod_usb;
//                            titleRes = R.string.action_demodulation_usb;
//                            break;
//                        default:
//                            Log.e(LOGTAG,"updateActionBar: invalid mode: " + demodulationMode);
//                            iconRes = -1;
//                            titleRes = -1;
//                            break;
//                    }
//                    if(titleRes > 0 && iconRes > 0) {
//                        mi_demodulationMode.setTitle(titleRes);
//                        mi_demodulationMode.setIcon(iconRes);
//                    }
//                }
//
//                // Set title and icon of the record button according to the state:
//                if(mi_record != null) {
//                    if (recordingFile != null) {
//                        mi_record.setTitle(R.string.action_recordOn);
//                        mi_record.setIcon(R.drawable.ic_action_record_on);
//                    } else {
//                        mi_record.setTitle(R.string.action_recordOff);
//                        mi_record.setIcon(R.drawable.ic_action_record_off);
//                    }
//                }
//            }
//        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        // Check if the user changed the preferences:
        checkForChangedPreferences();

        running = true; // Lets override the settings here GS
        // Start the analyzer if running is true:
        if (running)
            startAnalyzer();

        // on the first time after the app was killed by the system, savedInstanceState will be
        // non-null and we restore the settings:
        if (savedInstanceState != null) {
            analyzerSurface.setVirtualFrequency(savedInstanceState.getLong(getString(R.string.save_state_virtualFrequency)));
            analyzerSurface.setVirtualSampleRate(savedInstanceState.getInt(getString(R.string.save_state_virtualSampleRate)));
            analyzerSurface.setDBScale(savedInstanceState.getFloat(getString(R.string.save_state_minDB)),
                    savedInstanceState.getFloat(getString(R.string.save_state_maxDB)));
            analyzerSurface.setChannelFrequency(savedInstanceState.getLong(getString(R.string.save_state_channelFrequency)));
            analyzerSurface.setChannelWidth(savedInstanceState.getInt(getString(R.string.save_state_channelWidth)));
            analyzerSurface.setSquelch(savedInstanceState.getFloat(getString(R.string.save_state_squelch)));
            if (demodulator != null && scheduler != null) {
                demodulator.setChannelWidth(savedInstanceState.getInt(getString(R.string.save_state_channelWidth)));
                scheduler.setChannelFrequency(savedInstanceState.getLong(getString(R.string.save_state_channelFrequency)));
            }
            savedInstanceState = null; // not needed any more...
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        boolean runningSaved = running;    // save the running state, to restore it after the app re-starts...
        stopAnalyzer();                    // will stop the processing loop, scheduler and source
        running = runningSaved;            // running will be saved in onSaveInstanceState()

        // safe preferences:
        if (source != null) {
            SharedPreferences.Editor edit = preferences.edit();
            edit.putLong(getString(R.string.pref_frequency), source.getFrequency());
            edit.putInt(getString(R.string.pref_sampleRate), source.getSampleRate());
            edit.commit();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // err_info from RTL2832U:
        String[] rtlsdrErrInfo = {
                "permission_denied",
                "root_required",
                "no_devices_found",
                "unknown_error",
                "replug",
                "already_running"};

        switch (requestCode) {
            case RTL2832U_RESULT_CODE:
                // This happens if the RTL2832U driver was started.
                // We check for errors and print them:
                if (resultCode == RESULT_OK)
                    Log.i(LOGTAG, "onActivityResult: RTL2832U driver was successfully started.");
                else {
                    int errorId = -1;
                    int exceptionCode = 0;
                    String detailedDescription = null;
                    if (data != null) {
                        errorId = data.getIntExtra("marto.rtl_tcp_andro.RtlTcpExceptionId", -1);
                        exceptionCode = data.getIntExtra("detailed_exception_code", 0);
                        detailedDescription = data.getStringExtra("detailed_exception_message");
                    }
                    String errorMsg = "ERROR NOT SPECIFIED";
                    if (errorId >= 0 && errorId < rtlsdrErrInfo.length)
                        errorMsg = rtlsdrErrInfo[errorId];

                    Log.e(LOGTAG, "onActivityResult: RTL2832U driver returned with error: " + errorMsg + " (" + errorId + ")"
                            + (detailedDescription != null ? ": " + detailedDescription + " (" + exceptionCode + ")" : ""));

                    if (source != null && source instanceof RtlsdrSource) {
                        Toast.makeText(MainActivity.this, "Error with Source [" + source.getName() + "]: " + errorMsg + " (" + errorId + ")"
                                + (detailedDescription != null ? ": " + detailedDescription + " (" + exceptionCode + ")" : ""), Toast.LENGTH_LONG).show();
                        source.close();
                    }
                }
                break;
        }
    }

    @Override
    public void onIQSourceReady(IQSourceInterface source) {    // is called after source.open()
        if (running)
            startAnalyzer();    // will start the processing loop, scheduler and source
    }

    @Override
    public void onIQSourceError(final IQSourceInterface source, final String message) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, "Error with Source [" + source.getName() + "]: " + message, Toast.LENGTH_LONG).show();
            }
        });
        stopAnalyzer();

        if (this.source != null && this.source.isOpen())
            this.source.close();
    }

    /**
     * Will check if any preference conflicts with the current state of the app and fix it
     */
    public void checkForChangedPreferences() {
        // Source Type (this is pretty complex as we have to check each type individually):
        int sourceType = Integer.valueOf(preferences.getString(getString(R.string.pref_sourceType), "1"));
        sourceType = MySource;
        if (source != null) {
            switch (sourceType) {
                case FILE_SOURCE:
                    if (!(source instanceof FileIQSource)) {
                        source.close();
                        createSource();
                    } else {
                        long freq = Integer.valueOf(preferences.getString(getString(R.string.pref_filesource_frequency), "88495003"));
                        int sampRate = Integer.valueOf(preferences.getString(getString(R.string.pref_filesource_sampleRate), "2000000"));
                        String fileName = preferences.getString(getString(R.string.pref_filesource_file), "");
                        int fileFormat = Integer.valueOf(preferences.getString(getString(R.string.pref_filesource_format), "0"));
                        boolean repeat = preferences.getBoolean(getString(R.string.pref_filesource_repeat), false);
                        if (freq != source.getFrequency() || sampRate != source.getSampleRate()
                                || !fileName.equals(((FileIQSource) source).getFilename())
                                || repeat != ((FileIQSource) source).isRepeat()
                                || fileFormat != ((FileIQSource) source).getFileFormat()) {
                            source.close();
                            createSource();
                        }
                    }
                    break;
                case HACKRF_SOURCE:
                    if (!(source instanceof HackrfSource)) {
                        source.close();
                        createSource();
                    } else {
                        // overwrite hackrf source settings if changed:
                        boolean amp = preferences.getBoolean(getString(R.string.pref_hackrf_amplifier), false);
                        boolean antennaPower = preferences.getBoolean(getString(R.string.pref_hackrf_antennaPower), false);
                        int frequencyShift = Integer.valueOf(preferences.getString(getString(R.string.pref_hackrf_frequencyShift), "0"));
                        if (((HackrfSource) source).isAmplifierOn() != amp)
                            ((HackrfSource) source).setAmplifier(amp);
                        if (((HackrfSource) source).isAntennaPowerOn() != antennaPower)
                            ((HackrfSource) source).setAntennaPower(antennaPower);
                        if (((HackrfSource) source).getFrequencyShift() != frequencyShift)
                            ((HackrfSource) source).setFrequencyShift(frequencyShift);
                    }
                    break;
                case RTLSDR_SOURCE:
                    if (!(source instanceof RtlsdrSource)) {
                        source.close();
                        createSource();
                    } else {
                        // Check if ip or port has changed and recreate source if necessary:
                        String ip = preferences.getString(getString(R.string.pref_rtlsdr_ip), "");
                        int port = Integer.valueOf(preferences.getString(getString(R.string.pref_rtlsdr_port), "1234"));
                        boolean externalServer = preferences.getBoolean(getString(R.string.pref_rtlsdr_externalServer), false);
                        if (externalServer) {
                            if (!ip.equals(((RtlsdrSource) source).getIpAddress()) || port != ((RtlsdrSource) source).getPort()) {
                                source.close();
                                createSource();
                                return;
                            }
                        } else {
                            if (!((RtlsdrSource) source).getIpAddress().equals("127.0.0.1") || 1234 != ((RtlsdrSource) source).getPort()) {
                                source.close();
                                createSource();
                                return;
                            }
                        }

                        // otherwise just overwrite rtl-sdr source settings if changed:
                        int frequencyCorrection = Integer.valueOf(preferences.getString(getString(R.string.pref_rtlsdr_frequencyCorrection), "0"));
                        int frequencyShift = Integer.valueOf(preferences.getString(getString(R.string.pref_rtlsdr_frequencyShift), "0"));
                        if (frequencyCorrection != ((RtlsdrSource) source).getFrequencyCorrection())
                            ((RtlsdrSource) source).setFrequencyCorrection(frequencyCorrection);
                        if (((RtlsdrSource) source).getFrequencyShift() != frequencyShift)
                            ((RtlsdrSource) source).setFrequencyShift(frequencyShift);
                    }
                    break;
                default:
            }
        }

        if (analyzerSurface != null) {
            // All GUI settings will just be overwritten:
            analyzerSurface.setVerticalScrollEnabled(preferences.getBoolean(getString(R.string.pref_scrollDB), true));
            analyzerSurface.setVerticalZoomEnabled(preferences.getBoolean(getString(R.string.pref_zoomDB), true));
            analyzerSurface.setDecoupledAxis(preferences.getBoolean(getString(R.string.pref_decoupledAxis), false));
            analyzerSurface.setDisplayRelativeFrequencies(preferences.getBoolean(getString(R.string.pref_relativeFrequencies), false));
            analyzerSurface.setWaterfallColorMapType(Integer.valueOf(preferences.getString(getString(R.string.pref_colorMapType), "4")));
            analyzerSurface.setFftDrawingType(Integer.valueOf(preferences.getString(getString(R.string.pref_fftDrawingType), "2")));
            analyzerSurface.setAverageLength(Integer.valueOf(preferences.getString(getString(R.string.pref_averaging), "0")));
            analyzerSurface.setPeakHoldEnabled(preferences.getBoolean(getString(R.string.pref_peakHold), false));
            analyzerSurface.setFftRatio(Float.valueOf(preferences.getString(getString(R.string.pref_spectrumWaterfallRatio), "0.5")));
            analyzerSurface.setFontSize(Integer.valueOf(preferences.getString(getString(R.string.pref_fontSize), "2")));
            analyzerSurface.setShowDebugInformation(preferences.getBoolean(getString(R.string.pref_showDebugInformation), false));
        }

        // Screen Orientation:
        String screenOrientation = preferences.getString(getString(R.string.pref_screenOrientation), "auto");
        if (screenOrientation.equals("auto"))
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
        else if (screenOrientation.equals("landscape"))
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        else if (screenOrientation.equals("portrait"))
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        else if (screenOrientation.equals("reverse_landscape"))
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
        else if (screenOrientation.equals("reverse_portrait"))
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
    }


    /**
     * Will create a IQ Source instance according to the user settings.
     *
     * @return true on success; false on error
     */
    public boolean createSource() {
        long frequency;
        int sampleRate;
        int sourceType = Integer.valueOf(preferences.getString(getString(R.string.pref_sourceType), "1"));

        // override source to RTL 2832U
        sourceType = MySource;

        switch (sourceType) {
            case FILE_SOURCE:
                // Create IQ Source (filesource)
                try {
                    frequency = Integer.valueOf(preferences.getString(getString(R.string.pref_filesource_frequency), "88495003"));
                    sampleRate = Integer.valueOf(preferences.getString(getString(R.string.pref_filesource_sampleRate), "1000000"));
                } catch (NumberFormatException e) {
                    this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "File Source: Wrong format of frequency or sample rate", Toast.LENGTH_LONG).show();
                        }
                    });
                    return false;
                }

                String filename = preferences.getString(getString(R.string.pref_filesource_file), "");
                filename = selected_File;
                //   "/sdcard/RFAnalyzer/2015-08-29-19-24-54_rtlsdr_98300000Hz_1000000Sps.iq"
                int fileFormat = Integer.valueOf(preferences.getString(getString(R.string.pref_filesource_format), "0"));
                fileFormat = 1; //gs
                boolean repeat = preferences.getBoolean(getString(R.string.pref_filesource_repeat), false);
                repeat = true; // gs
                sampleRate = 1000000;


                source = new FileIQSource(filename, sampleRate, frequency, 16384, repeat, fileFormat);
                break;
            case HACKRF_SOURCE:
                // Create HackrfSource
                source = new HackrfSource();
                source.setFrequency(preferences.getLong(getString(R.string.pref_frequency), 88495003));
                source.setSampleRate(preferences.getInt(getString(R.string.pref_sampleRate), HackrfSource.MAX_SAMPLERATE));
                ((HackrfSource) source).setVgaRxGain(preferences.getInt(getString(R.string.pref_hackrf_vgaRxGain), HackrfSource.MAX_VGA_RX_GAIN / 2));
                ((HackrfSource) source).setLnaGain(preferences.getInt(getString(R.string.pref_hackrf_lnaGain), HackrfSource.MAX_LNA_GAIN / 2));
                ((HackrfSource) source).setAmplifier(preferences.getBoolean(getString(R.string.pref_hackrf_amplifier), false));
                ((HackrfSource) source).setAntennaPower(preferences.getBoolean(getString(R.string.pref_hackrf_antennaPower), false));
                ((HackrfSource) source).setFrequencyShift(Integer.valueOf(
                        preferences.getString(getString(R.string.pref_hackrf_frequencyShift), "0")));
                break;
            case RTLSDR_SOURCE:
                // Create RtlsdrSource
                if (preferences.getBoolean(getString(R.string.pref_rtlsdr_externalServer), false))
                    source = new RtlsdrSource(preferences.getString(getString(R.string.pref_rtlsdr_ip), ""),
                            Integer.valueOf(preferences.getString(getString(R.string.pref_rtlsdr_port), "1234")));
                else {
                    // Toast.makeText(MainActivity.this, "Setting Source to 127.0.0.1:1234", Toast.LENGTH_SHORT).show();
                    source = new RtlsdrSource("127.0.0.1", 1234);
                }

                frequency = preferences.getLong(getString(R.string.pref_frequency), 88495003);
                frequency = 88495003;
                sampleRate = preferences.getInt(getString(R.string.pref_sampleRate), source.getMaxSampleRate());
                if (sampleRate > 2000000)    // might be the case after switching over from HackRF
                    sampleRate = 2000000;
                source.setFrequency(frequency);
                source.setSampleRate(sampleRate);

                ((RtlsdrSource) source).setFrequencyCorrection(Integer.valueOf(preferences.getString(getString(R.string.pref_rtlsdr_frequencyCorrection), "0")));
                ((RtlsdrSource) source).setFrequencyShift(Integer.valueOf(
                        preferences.getString(getString(R.string.pref_rtlsdr_frequencyShift), "0")));
                ((RtlsdrSource) source).setManualGain(preferences.getBoolean(getString(R.string.pref_rtlsdr_manual_gain), false));
                ((RtlsdrSource) source).setAutomaticGainControl(preferences.getBoolean(getString(R.string.pref_rtlsdr_agc), false));
                if (((RtlsdrSource) source).isManualGain()) {
                    ((RtlsdrSource) source).setGain(preferences.getInt(getString(R.string.pref_rtlsdr_gain), 0));
                    ((RtlsdrSource) source).setIFGain(preferences.getInt(getString(R.string.pref_rtlsdr_ifGain), 0));
                }
                break;
            default:
                Log.e(LOGTAG, "createSource: Invalid source type: " + sourceType);
                return false;
        }

        // inform the analyzer surface about the new source
        analyzerSurface.setSource(source);

//        Toast.makeText(MainActivity.this, "Source Created", Toast.LENGTH_SHORT).show();

        return true;
    }

    /**
     * Will open the IQ Source instance.
     * Note: some sources need special treatment on opening, like the rtl-sdr source.
     *
     * @return true on success; false on error
     */
    public boolean openSource() {
        int sourceType = Integer.valueOf(preferences.getString(getString(R.string.pref_sourceType), "1"));

        sourceType = MySource; // Force RTL 2832

        switch (sourceType) {
            case FILE_SOURCE:
                if (source != null && source instanceof FileIQSource)
                    return source.open(this, this);
                else {
                    Log.e(LOGTAG, "openSource: sourceType is FILE_SOURCE, but source is null or of other type.");
                    return false;
                }
            case HACKRF_SOURCE:
                if (source != null && source instanceof HackrfSource)
                    return source.open(this, this);
                else {
                    Log.e(LOGTAG, "openSource: sourceType is HACKRF_SOURCE, but source is null or of other type.");
                    return false;
                }
            case RTLSDR_SOURCE:
                if (source != null && source instanceof RtlsdrSource) {
                    // We might need to start the driver:
                    if (!preferences.getBoolean(getString(R.string.pref_rtlsdr_externalServer), false)) {
                        // start local rtl_tcp instance:
                        Toast.makeText(MainActivity.this, "start local rtl_tcp instance at 127.0.0.1:1234", Toast.LENGTH_LONG).show();

                        try {
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setData(Uri.parse("iqsrc://-a 127.0.0.1 -p 1234 -n 1"));
                            startActivityForResult(intent, RTL2832U_RESULT_CODE);

                        } catch (ActivityNotFoundException e) {
                            Log.e(LOGTAG, "createSource: RTL2832U is not installed");
                            Toast.makeText(MainActivity.this, "createSource: RTL2832U is not installed", Toast.LENGTH_LONG).show();

                            // Show a dialog that links to the play market:
                            new AlertDialog.Builder(this)
                                    .setTitle("RTL2832U driver not installed!")
                                    .setMessage("You need to install the (free) RTL2832U driver to use RTL-SDR dongles.")
                                    .setPositiveButton("Install from Google Play", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int whichButton) {
                                            Intent marketIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=marto.rtl_tcp_andro"));
                                            startActivity(marketIntent);
                                        }
                                    })
                                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int whichButton) {
                                            // do nothing
                                        }
                                    })
                                    .show();
                            return false;
                        }
                    }
                    boolean status_rtlOpen = source.open(this, this);
                    return status_rtlOpen;

                } else {
                    Log.e(LOGTAG, "openSource: sourceType is RTLSDR_SOURCE, but source is null or of other type.");
                    return false;
                }
            default:
                Log.e(LOGTAG, "openSource: Invalid source type: " + sourceType);
                Toast.makeText(MainActivity.this, "openSource: Invalid source type: ", Toast.LENGTH_LONG).show();
                return false;
        }

    }

    /**
     * Will stop the RF Analyzer. This includes shutting down the scheduler (which turns of the
     * source), the processing loop and the demodulator if running.
     */
    public void stopAnalyzer() {
        // Stop the Scheduler if running:
        if (scheduler != null) {
            // Stop recording in case it is running:
            stopRecording();
            scheduler.stopScheduler();
        }

        // Stop the Processing Loop if running:
        if (analyzerProcessingLoop != null)
            analyzerProcessingLoop.stopLoop();

        // Stop the Demodulator if running:
        if (demodulator != null)
            demodulator.stopDemodulator();

        // Wait for the scheduler to stop:
        if (scheduler != null && !scheduler.getName().equals(Thread.currentThread().getName())) {
            try {
                scheduler.join();
            } catch (InterruptedException e) {
                Log.e(LOGTAG, "startAnalyzer: Error while stopping Scheduler.");
            }
        }

        // Wait for the processing loop to stop
        if (analyzerProcessingLoop != null) {
            try {
                analyzerProcessingLoop.join();
            } catch (InterruptedException e) {
                Log.e(LOGTAG, "startAnalyzer: Error while stopping Processing Loop.");
            }
        }

        // Wait for the demodulator to stop
        if (demodulator != null) {
            try {
                demodulator.join();
            } catch (InterruptedException e) {
                Log.e(LOGTAG, "startAnalyzer: Error while stopping Demodulator.");
            }
        }

        running = false;

        // update action bar icons and titles:
        updateActionBar();

        // allow screen to turn off again:
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            }
        });
    }

    /**
     * Will start the RF Analyzer. This includes creating a source (if null), open a source
     * (if not open), starting the scheduler (which starts the source) and starting the
     * processing loop.
     */
    public void startAnalyzer() {
        this.stopAnalyzer();    // Stop if running; This assures that we don't end up with multiple instances of the thread loops

        // Retrieve fft size and frame rate from the preferences
        int fftSize = Integer.valueOf(preferences.getString(getString(R.string.pref_fftSize), "1024"));
        int frameRate = Integer.valueOf(preferences.getString(getString(R.string.pref_frameRate), "1"));
        boolean dynamicFrameRate = preferences.getBoolean(getString(R.string.pref_dynamicFrameRate), true);

        running = true;

        if (source == null) {
            if (!this.createSource())
                return;
        }

        // check if the source is open. if not, open it!
        if (!source.isOpen()) {
            if (!openSource()) {
                Toast.makeText(MainActivity.this, "Source not available (" + source.getName() + ")", Toast.LENGTH_SHORT).show();
                running = false;
                return;
            }
            return;    // we have to wait for the source to become ready... onIQSourceReady() will call startAnalyzer() again...
        }
        // Create a new instance of Scheduler and Processing Loop:
        scheduler = new Scheduler(fftSize, source);
        analyzerProcessingLoop = new AnalyzerProcessingLoop(
                analyzerSurface,            // Reference to the Analyzer Surface
                fftSize,                    // FFT size
                scheduler.getFftOutputQueue(), // Reference to the input queue for the processing loop
                scheduler.getFftInputQueue()); // Reference to the buffer-pool-return queue
        if (dynamicFrameRate)
            analyzerProcessingLoop.setDynamicFrameRate(true);
        else {
            analyzerProcessingLoop.setDynamicFrameRate(false);
            analyzerProcessingLoop.setFrameRate(frameRate);
        }

        // Start both threads:
        scheduler.start();
        analyzerProcessingLoop.start();

//        scheduler.setChannelFrequency(analyzerSurface.getChannelFrequency());
        scheduler.setChannelFrequency(88495003);

        // Start the demodulator thread:
        demodulator = new Demodulator(scheduler.getDemodOutputQueue(), scheduler.getDemodInputQueue(), source.getPacketSize());
        demodulator.start();

        // Set the demodulation mode (will configure the demodulator correctly)
        this.setDemodulationMode(demodulationMode);

        // update the action bar icons and titles:
        updateActionBar();

        // Prevent the screen from turning off:
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            }
        });
    }

    /**
     * Will pop up a dialog to let the user choose a demodulation mode.
     */
    private void showDemodulationDialog() {
        if (scheduler == null || demodulator == null || source == null) {
            Toast.makeText(MainActivity.this, "Analyzer must be running to change modulation mode", Toast.LENGTH_LONG).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Select a demodulation mode:")
                .setSingleChoiceItems(R.array.demodulation_modes, demodulator.getDemodulationMode(), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        setDemodulationMode(which);
                        dialog.dismiss();
                    }
                })
                .show();
    }

    /**
     * Will set the modulation mode to the given value. Takes care of adjusting the
     * scheduler and the demodulator respectively and updates the action bar menu item.
     *
     * @param mode Demodulator.DEMODULATION_OFF, *_AM, *_NFM, *_WFM
     */
    public void setDemodulationMode(int mode) {
        if (scheduler == null || demodulator == null || source == null) {
            Log.e(LOGTAG, "setDemodulationMode: scheduler/demodulator/source is null");
            return;
        }

        // (de-)activate demodulation in the scheduler and set the sample rate accordingly:
        if (mode == Demodulator.DEMODULATION_OFF) {
            scheduler.setDemodulationActivated(false);
        } else {
            if (recordingFile != null && source.getSampleRate() != Demodulator.INPUT_RATE) {
                // We are recording at an incompatible sample rate right now.
                Log.i(LOGTAG, "setDemodulationMode: Recording is running at " + source.getSampleRate() + " Sps. Can't start demodulation.");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "Recording is running at incompatible sample rate for demodulation!", Toast.LENGTH_LONG).show();
                    }
                });
                return;
            }

            // adjust sample rate of the source:
            source.setSampleRate(Demodulator.INPUT_RATE);

            // Verify that the source supports the sample rate:
            if (source.getSampleRate() != Demodulator.INPUT_RATE) {
                Log.e(LOGTAG, "setDemodulationMode: cannot adjust source sample rate!");
                Toast.makeText(MainActivity.this, "Source does not support the sample rate necessary for demodulation (" +
                        Demodulator.INPUT_RATE / 1000000 + " Msps)", Toast.LENGTH_LONG).show();
                scheduler.setDemodulationActivated(false);
                mode = Demodulator.DEMODULATION_OFF;    // deactivate demodulation...
            } else {
                scheduler.setDemodulationActivated(true);
            }
        }

        // set demodulation mode in demodulator:
        demodulator.setDemodulationMode(mode);
        this.demodulationMode = mode;    // save the setting

        // disable/enable demodulation view in surface:
        if (mode == Demodulator.DEMODULATION_OFF) {
            analyzerSurface.setDemodulationEnabled(false);

        } else {
            analyzerSurface.setDemodulationEnabled(true);    // will re-adjust channel freq, width and squelch,
            // if they are outside the current viewport and update the
            // demodulator via callbacks.
//            analyzerSurface.setShowLowerBand(mode != Demodulator.DEMODULATION_USB);		// show lower side band if not USB
//            analyzerSurface.setShowUpperBand(mode != Demodulator.DEMODULATION_LSB);		// show upper side band if not LSB

        }
        // update action bar:
        updateActionBar();
    }

    /**
     * Will pop up a dialog to let the user input a new frequency.
     * Note: A frequency can be entered either in Hz or in MHz. If the input value
     * is a number smaller than the maximum frequency of the source in MHz, then it
     * is interpreted as a frequency in MHz. Otherwise it will be handled as frequency
     * in Hz.
     */
    private void tuneToFrequency() {
        if (source == null)
            return;

        // calculate max frequency of the source in MHz:
        final double maxFreqMHz = source.getMaxFrequency() / 1000000f;

//        final LinearLayout ll_view = (LinearLayout) this.getLayoutInflater().inflate(R.layout.tune_to_frequency, null);
//        final EditText et_frequency = (EditText) ll_view.findViewById(R.id.et_tune_to_frequency);
//        final CheckBox cb_bandwidth = (CheckBox) ll_view.findViewById(R.id.cb_tune_to_frequency_bandwidth);
//        final EditText et_bandwidth = (EditText) ll_view.findViewById(R.id.et_tune_to_frequency_bandwidth);
//        final Spinner sp_bandwidthUnit = (Spinner) ll_view.findViewById(R.id.sp_tune_to_frequency_bandwidth_unit);
//        final TextView tv_warning = (TextView) ll_view.findViewById(R.id.tv_tune_to_frequency_warning);

        // Show warning if we are currently recording to file:
//        if(recordingFile != null)
//            tv_warning.setVisibility(View.VISIBLE);
//
//        cb_bandwidth.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                et_bandwidth.setEnabled(isChecked);
//                sp_bandwidthUnit.setEnabled(isChecked);
//            }
//        });
//        cb_bandwidth.toggle();	// to trigger the onCheckedChangeListener at least once to set inital state
//        cb_bandwidth.setChecked(preferences.getBoolean(getString(R.string.pref_tune_to_frequency_setBandwidth), false));
//        et_bandwidth.setText(preferences.getString(getString(R.string.pref_tune_to_frequency_bandwidth), "1"));
//        sp_bandwidthUnit.setSelection(preferences.getInt(getString(R.string.pref_tune_to_frequency_bandwidthUnit), 0));

//        new AlertDialog.Builder(this)
//                .setTitle("Tune to Frequency")
//                .setMessage("Frequency is " + source.getFrequency()/1000000f + "MHz. Type a new Frequency (Values below "
//                        + maxFreqMHz + " will be interpreted as MHz, higher values as Hz): ")
//                .setView(ll_view)
//                .setPositiveButton("Set", new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int whichButton) {
//                        try {
//                            float newFreq = source.getFrequency()/1000000f;
//                            if(et_frequency.getText().length() != 0)
//                                newFreq = Float.valueOf(et_frequency.getText().toString());
//                            if (newFreq < maxFreqMHz)
//                                newFreq = newFreq * 1000000;
//                            if (newFreq <= source.getMaxFrequency() && newFreq >= source.getMinFrequency()) {
//                                source.setFrequency((long)newFreq);
//                                analyzerSurface.setVirtualFrequency((long)newFreq);
//                                if(demodulationMode != Demodulator.DEMODULATION_OFF)
//                                    analyzerSurface.setDemodulationEnabled(true);	// This will re-adjust the channel freq correctly
//
//                                // Set bandwidth (virtual sample rate):
//                                if(cb_bandwidth.isChecked() && et_bandwidth.getText().length() != 0) {
//                                    float bandwidth = Float.valueOf(et_bandwidth.getText().toString());
//                                    if(sp_bandwidthUnit.getSelectedItemPosition() == 0)			//MHz
//                                        bandwidth *= 1000000;
//                                    else if(sp_bandwidthUnit.getSelectedItemPosition() == 1)	//KHz
//                                        bandwidth *= 1000;
//                                    if(bandwidth > source.getMaxSampleRate())
//                                        bandwidth = source.getMaxFrequency();
//                                    source.setSampleRate(source.getNextHigherOptimalSampleRate((int)bandwidth));
//                                    analyzerSurface.setVirtualSampleRate((int)bandwidth);
//                                }
//                                // safe preferences:
//                                SharedPreferences.Editor edit = preferences.edit();
//                                edit.putBoolean(getString(R.string.pref_tune_to_frequency_setBandwidth), cb_bandwidth.isChecked());
//                                edit.putString(getString(R.string.pref_tune_to_frequency_bandwidth), et_bandwidth.getText().toString());
//                                edit.putInt(getString(R.string.pref_tune_to_frequency_bandwidthUnit), sp_bandwidthUnit.getSelectedItemPosition());
//                                edit.apply();
//
//                            } else {
//                                Toast.makeText(MainActivity.this, "Frequency is out of the valid range: " + (long)newFreq + " Hz", Toast.LENGTH_LONG).show();
//                            }
//                        } catch (NumberFormatException e) {
//                            Log.e(LOGTAG, "tuneToFrequency: Error while setting frequency: " + e.getMessage());
//                        }
//                    }
//                })
//                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int whichButton) {
//                        // do nothing
//                    }
//                })
//                .show();
    }

    /**
     * Will pop up a dialog to let the user adjust gain settings
     */
    private void adjustGain() {
        if (source == null)
            return;

        int sourceType = Integer.valueOf(preferences.getString(getString(R.string.pref_sourceType), "1"));
        sourceType = MySource;
//        switch (sourceType) {
//            case FILE_SOURCE:
//                Toast.makeText(this, getString(R.string.filesource_doesnt_support_gain), Toast.LENGTH_LONG).show();
//                break;
//            case HACKRF_SOURCE:
//                // Prepare layout:
//                final LinearLayout view_hackrf = (LinearLayout) this.getLayoutInflater().inflate(R.layout.hackrf_gain, null);
//                final SeekBar sb_hackrf_vga = (SeekBar) view_hackrf.findViewById(R.id.sb_hackrf_vga_gain);
//                final SeekBar sb_hackrf_lna = (SeekBar) view_hackrf.findViewById(R.id.sb_hackrf_lna_gain);
//                final TextView tv_hackrf_vga = (TextView) view_hackrf.findViewById(R.id.tv_hackrf_vga_gain);
//                final TextView tv_hackrf_lna = (TextView) view_hackrf.findViewById(R.id.tv_hackrf_lna_gain);
//                sb_hackrf_vga.setMax(HackrfSource.MAX_VGA_RX_GAIN / HackrfSource.VGA_RX_GAIN_STEP_SIZE);
//                sb_hackrf_lna.setMax(HackrfSource.MAX_LNA_GAIN / HackrfSource.LNA_GAIN_STEP_SIZE);
//                sb_hackrf_vga.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//                    @Override
//                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//                        tv_hackrf_vga.setText("" + progress * HackrfSource.VGA_RX_GAIN_STEP_SIZE);
//                        ((HackrfSource)source).setVgaRxGain(progress*HackrfSource.VGA_RX_GAIN_STEP_SIZE);
//                    }
//
//                    @Override
//                    public void onStartTrackingTouch(SeekBar seekBar) {
//                    }
//
//                    @Override
//                    public void onStopTrackingTouch(SeekBar seekBar) {
//                    }
//                });
//                sb_hackrf_lna.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//                    @Override
//                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//                        tv_hackrf_lna.setText("" + progress * HackrfSource.LNA_GAIN_STEP_SIZE);
//                        ((HackrfSource)source).setLnaGain(progress*HackrfSource.LNA_GAIN_STEP_SIZE);
//                    }
//
//                    @Override
//                    public void onStartTrackingTouch(SeekBar seekBar) {
//                    }
//
//                    @Override
//                    public void onStopTrackingTouch(SeekBar seekBar) {
//                    }
//                });
//                sb_hackrf_vga.setProgress(((HackrfSource) source).getVgaRxGain() / HackrfSource.VGA_RX_GAIN_STEP_SIZE);
//                sb_hackrf_lna.setProgress(((HackrfSource) source).getLnaGain() / HackrfSource.LNA_GAIN_STEP_SIZE);
//
//                // Show dialog:
//                AlertDialog hackrfDialog = new AlertDialog.Builder(this)
//                        .setTitle("Adjust Gain Settings")
//                        .setView(view_hackrf)
//                        .setPositiveButton("Set", new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialog, int whichButton) {
//                                // safe preferences:
//                                SharedPreferences.Editor edit = preferences.edit();
//                                edit.putInt(getString(R.string.pref_hackrf_vgaRxGain), sb_hackrf_vga.getProgress()*HackrfSource.VGA_RX_GAIN_STEP_SIZE);
//                                edit.putInt(getString(R.string.pref_hackrf_lnaGain), sb_hackrf_lna.getProgress()*HackrfSource.LNA_GAIN_STEP_SIZE);
//                                edit.apply();
//                            }
//                        })
//                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialog, int whichButton) {
//                                // do nothing
//                            }
//                        })
//                        .create();
//                hackrfDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
//                    @Override
//                    public void onDismiss(DialogInterface dialog) {
//                        // sync source with (new/old) settings
//                        int vgaRxGain = preferences.getInt(getString(R.string.pref_hackrf_vgaRxGain),HackrfSource.MAX_VGA_RX_GAIN/2);
//                        int lnaGain = preferences.getInt(getString(R.string.pref_hackrf_lnaGain),HackrfSource.MAX_LNA_GAIN/2);
//                        if(((HackrfSource)source).getVgaRxGain() != vgaRxGain)
//                            ((HackrfSource)source).setVgaRxGain(vgaRxGain);
//                        if(((HackrfSource)source).getLnaGain() != lnaGain)
//                            ((HackrfSource)source).setLnaGain(lnaGain);
//                    }
//                });
//                hackrfDialog.show();
//                hackrfDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
//                break;
//            case RTLSDR_SOURCE:
//                final int[] possibleGainValues = ((RtlsdrSource)source).getPossibleGainValues();
//                final int[] possibleIFGainValues = ((RtlsdrSource)source).getPossibleIFGainValues();
//                if(possibleGainValues.length <= 1 && possibleIFGainValues.length <= 1) {
//                    Toast.makeText(MainActivity.this, source.getName() + " does not support gain adjustment!", Toast.LENGTH_LONG).show();
//                }
//                // Prepare layout:
//                final LinearLayout view_rtlsdr = (LinearLayout) this.getLayoutInflater().inflate(R.layout.rtlsdr_gain, null);
//                final LinearLayout ll_rtlsdr_gain = (LinearLayout) view_rtlsdr.findViewById(R.id.ll_rtlsdr_gain);
//                final LinearLayout ll_rtlsdr_ifgain = (LinearLayout) view_rtlsdr.findViewById(R.id.ll_rtlsdr_ifgain);
//                final Switch sw_rtlsdr_manual_gain = (Switch) view_rtlsdr.findViewById(R.id.sw_rtlsdr_manual_gain);
//                final CheckBox cb_rtlsdr_agc = (CheckBox) view_rtlsdr.findViewById(R.id.cb_rtlsdr_agc);
//                final SeekBar sb_rtlsdr_gain = (SeekBar) view_rtlsdr.findViewById(R.id.sb_rtlsdr_gain);
//                final SeekBar sb_rtlsdr_ifGain = (SeekBar) view_rtlsdr.findViewById(R.id.sb_rtlsdr_ifgain);
//                final TextView tv_rtlsdr_gain = (TextView) view_rtlsdr.findViewById(R.id.tv_rtlsdr_gain);
//                final TextView tv_rtlsdr_ifGain = (TextView) view_rtlsdr.findViewById(R.id.tv_rtlsdr_ifgain);
//
//                // Assign current gain:
//                int gainIndex = 0;
//                int ifGainIndex = 0;
//                for (int i = 0; i < possibleGainValues.length; i++) {
//                    if(((RtlsdrSource)source).getGain() == possibleGainValues[i]) {
//                        gainIndex = i;
//                        break;
//                    }
//                }
//                for (int i = 0; i < possibleIFGainValues.length; i++) {
//                    if(((RtlsdrSource)source).getIFGain() == possibleIFGainValues[i]) {
//                        ifGainIndex = i;
//                        break;
//                    }
//                }
//                sb_rtlsdr_gain.setMax(possibleGainValues.length - 1);
//                sb_rtlsdr_ifGain.setMax(possibleIFGainValues.length - 1);
//                sb_rtlsdr_gain.setProgress(gainIndex);
//                sb_rtlsdr_ifGain.setProgress(ifGainIndex);
//                tv_rtlsdr_gain.setText("" + possibleGainValues[gainIndex]);
//                tv_rtlsdr_ifGain.setText("" + possibleIFGainValues[ifGainIndex]);
//
//                // Assign current manual gain and agc setting
//                sw_rtlsdr_manual_gain.setChecked(((RtlsdrSource)source).isManualGain());
//                cb_rtlsdr_agc.setChecked(((RtlsdrSource)source).isAutomaticGainControl());
//
//                // Add listener to gui elements:
//                sw_rtlsdr_manual_gain.setOnCheckedChangeListener(new Switch.OnCheckedChangeListener() {
//                    @Override
//                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                        sb_rtlsdr_gain.setEnabled(isChecked);
//                        tv_rtlsdr_gain.setEnabled(isChecked);
//                        sb_rtlsdr_ifGain.setEnabled(isChecked);
//                        tv_rtlsdr_ifGain.setEnabled(isChecked);
//                        ((RtlsdrSource)source).setManualGain(isChecked);
//                        if(isChecked) {
//                            ((RtlsdrSource) source).setGain(possibleGainValues[sb_rtlsdr_gain.getProgress()]);
//                            ((RtlsdrSource) source).setIFGain(possibleIFGainValues[sb_rtlsdr_ifGain.getProgress()]);
//                        }
//                    }
//                });
//                cb_rtlsdr_agc.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//                    @Override
//                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                        ((RtlsdrSource)source).setAutomaticGainControl(isChecked);
//                    }
//                });
//                sb_rtlsdr_gain.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//                    @Override
//                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//                        tv_rtlsdr_gain.setText("" + possibleGainValues[progress]);
//                        ((RtlsdrSource) source).setGain(possibleGainValues[progress]);
//                    }
//
//                    @Override
//                    public void onStartTrackingTouch(SeekBar seekBar) {
//                    }
//
//                    @Override
//                    public void onStopTrackingTouch(SeekBar seekBar) {
//                    }
//                });
//                sb_rtlsdr_ifGain.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//                    @Override
//                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//                        tv_rtlsdr_ifGain.setText("" + possibleIFGainValues[progress]);
//                        ((RtlsdrSource) source).setIFGain(possibleIFGainValues[progress]);
//                    }
//
//                    @Override
//                    public void onStartTrackingTouch(SeekBar seekBar) {
//                    }
//
//                    @Override
//                    public void onStopTrackingTouch(SeekBar seekBar) {
//                    }
//                });
//
//                // Disable gui elements if gain cannot be adjusted:
//                if(possibleGainValues.length <= 1)
//                    ll_rtlsdr_gain.setVisibility(View.GONE);
//                if(possibleIFGainValues.length <= 1)
//                    ll_rtlsdr_ifgain.setVisibility(View.GONE);
//
//                if(!sw_rtlsdr_manual_gain.isChecked()) {
//                    sb_rtlsdr_gain.setEnabled(false);
//                    tv_rtlsdr_gain.setEnabled(false);
//                    sb_rtlsdr_ifGain.setEnabled(false);
//                    tv_rtlsdr_ifGain.setEnabled(false);
//                }
//
//                // Show dialog:
//                AlertDialog rtlsdrDialog = new AlertDialog.Builder(this)
//                        .setTitle("Adjust Gain Settings")
//                        .setView(view_rtlsdr)
//                        .setPositiveButton("Set", new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialog, int whichButton) {
//                                // safe preferences:
//                                SharedPreferences.Editor edit = preferences.edit();
//                                edit.putBoolean(getString(R.string.pref_rtlsdr_manual_gain), sw_rtlsdr_manual_gain.isChecked());
//                                edit.putBoolean(getString(R.string.pref_rtlsdr_agc), cb_rtlsdr_agc.isChecked());
//                                edit.putInt(getString(R.string.pref_rtlsdr_gain), possibleGainValues[sb_rtlsdr_gain.getProgress()]);
//                                edit.putInt(getString(R.string.pref_rtlsdr_ifGain), possibleIFGainValues[sb_rtlsdr_ifGain.getProgress()]);
//                                edit.apply();
//                            }
//                        })
//                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialog, int whichButton) {
//                                // do nothing
//                            }
//                        })
//                        .create();
//                rtlsdrDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
//                    @Override
//                    public void onDismiss(DialogInterface dialog) {
//                        boolean manualGain = preferences.getBoolean(getString(R.string.pref_rtlsdr_manual_gain), false);
//                        boolean agc = preferences.getBoolean(getString(R.string.pref_rtlsdr_agc), false);
//                        int gain = preferences.getInt(getString(R.string.pref_rtlsdr_gain), 0);
//                        int ifGain = preferences.getInt(getString(R.string.pref_rtlsdr_ifGain), 0);
//                        ((RtlsdrSource)source).setGain(gain);
//                        ((RtlsdrSource)source).setIFGain(ifGain);
//                        ((RtlsdrSource)source).setManualGain(manualGain);
//                        ((RtlsdrSource)source).setAutomaticGainControl(agc);
//                        if(manualGain) {
//                            // Note: This is a workaround. After setting manual gain to true we must
//                            // rewrite the manual gain values:
//                            ((RtlsdrSource) source).setGain(gain);
//                            ((RtlsdrSource) source).setIFGain(ifGain);
//                        }
//                    }
//                });
//                rtlsdrDialog.show();
//                rtlsdrDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
//                break;
//            default:
//                Log.e(LOGTAG, "adjustGain: Invalid source type: " + sourceType);
//                break;
//        }
//    }
//
//    public void showRecordingDialog() {
//        if(!running || scheduler == null || demodulator == null || source == null) {
//            Toast.makeText(MainActivity.this, "Analyzer must be running to start recording", Toast.LENGTH_LONG).show();
//            return;
//        }
//
//        final String externalDir = Environment.getExternalStorageDirectory().getAbsolutePath();
//        final int[] supportedSampleRates = source.getSupportedSampleRates();
//        final double maxFreqMHz = source.getMaxFrequency() / 1000000f; // max frequency of the source in MHz
//        final int sourceType = Integer.valueOf(preferences.getString(getString(R.string.pref_sourceType), "1"));
//        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.US);
//
//        // Get references to the GUI components:
//        final ScrollView view = (ScrollView) this.getLayoutInflater().inflate(R.layout.start_recording, null);
//        final EditText et_filename = (EditText) view.findViewById(R.id.et_recording_filename);
//        final EditText et_frequency = (EditText) view.findViewById(R.id.et_recording_frequency);
//        final Spinner sp_sampleRate = (Spinner) view.findViewById(R.id.sp_recording_sampleRate);
//        final TextView tv_fixedSampleRateHint = (TextView) view.findViewById(R.id.tv_recording_fixedSampleRateHint);
//        final CheckBox cb_stopAfter = (CheckBox) view.findViewById(R.id.cb_recording_stopAfter);
//        final EditText et_stopAfter = (EditText) view.findViewById(R.id.et_recording_stopAfter);
//        final Spinner sp_stopAfter = (Spinner) view.findViewById(R.id.sp_recording_stopAfter);
//
//        // Setup the sample rate spinner:
//        final ArrayAdapter<Integer> sampleRateAdapter = new ArrayAdapter<Integer>(this, android.R.layout.simple_list_item_1);
//        for(int sampR: supportedSampleRates)
//            sampleRateAdapter.add(sampR);
//        sampleRateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        sp_sampleRate.setAdapter(sampleRateAdapter);
//
//        // Add listener to the frequency textfield, the sample rate spinner and the checkbox:
//        et_frequency.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {}
//            @Override
//            public void afterTextChanged(Editable s) {
//                if(et_frequency.getText().length() == 0)
//                    return;
//                double freq = Double.valueOf(et_frequency.getText().toString());
//                if (freq < maxFreqMHz)
//                    freq = freq * 1000000;
//                et_filename.setText(simpleDateFormat.format(new Date()) + "_" + SOURCE_NAMES[sourceType] + "_"
//                        + (long)freq + "Hz_" + sp_sampleRate.getSelectedItem() + "Sps.iq");
//            }
//        });
//        sp_sampleRate.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                if(et_frequency.getText().length() == 0)
//                    return;
//                double freq = Double.valueOf(et_frequency.getText().toString());
//                if (freq < maxFreqMHz)
//                    freq = freq * 1000000;
//                et_filename.setText(simpleDateFormat.format(new Date()) + "_" + SOURCE_NAMES[sourceType] + "_"
//                        + (long) freq + "Hz_" + sp_sampleRate.getSelectedItem() + "Sps.iq");
//            }
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {}
//        });
//        cb_stopAfter.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                et_stopAfter.setEnabled(isChecked);
//                sp_stopAfter.setEnabled(isChecked);
//            }
//        });
//
//        // Set default frequency, sample rate and stop after values:
//        et_frequency.setText("" + analyzerSurface.getVirtualFrequency());
//        int sampleRateIndex = 0;
//        int lastSampleRate = preferences.getInt(getString(R.string.pref_recordingSampleRate),1000000);
//        for (; sampleRateIndex < supportedSampleRates.length; sampleRateIndex++) {
//            if(supportedSampleRates[sampleRateIndex] >= lastSampleRate)
//                break;
//        }
//        if(sampleRateIndex >= supportedSampleRates.length)
//            sampleRateIndex = supportedSampleRates.length - 1;
//        sp_sampleRate.setSelection(sampleRateIndex);
//        cb_stopAfter.toggle(); // just to trigger the listener at least once!
//        cb_stopAfter.setChecked(preferences.getBoolean(getString(R.string.pref_recordingStopAfterEnabled), false));
//        et_stopAfter.setText("" + preferences.getInt(getString(R.string.pref_recordingStopAfterValue), 10));
//        sp_stopAfter.setSelection(preferences.getInt(getString(R.string.pref_recordingStopAfterUnit), 0));
//
//        // disable sample rate selection if demodulation is running:
//        if(demodulationMode != Demodulator.DEMODULATION_OFF) {
//            sampleRateAdapter.add(source.getSampleRate());	// add the current sample rate in case it's not already in the list
//            sp_sampleRate.setSelection(sampleRateAdapter.getPosition(source.getSampleRate()));	// select it
//            sp_sampleRate.setEnabled(false);	// disable the spinner
//            tv_fixedSampleRateHint.setVisibility(View.VISIBLE);
//        }
//
//        // Show dialog:
//        new AlertDialog.Builder(this)
//                .setTitle("Start recording")
//                .setView(view)
//                .setPositiveButton("Record", new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int whichButton) {
//                        String filename = et_filename.getText().toString();
//                        final int stopAfterUnit = sp_stopAfter.getSelectedItemPosition();
//                        final int stopAfterValue = Integer.valueOf(et_stopAfter.getText().toString());
//                        //todo check filename
//
//                        // Set the frequency in the source:
//                        if(et_frequency.getText().length() == 0)
//                            return;
//                        double freq = Double.valueOf(et_frequency.getText().toString());
//                        if (freq < maxFreqMHz)
//                            freq = freq * 1000000;
//                        if (freq <= source.getMaxFrequency() && freq >= source.getMinFrequency())
//                            source.setFrequency((long)freq);
//                        else {
//                            Toast.makeText(MainActivity.this, "Frequency is invalid!", Toast.LENGTH_LONG).show();
//                            return;
//                        }
//
//                        // Set the sample rate (only if demodulator is off):
//                        if(demodulationMode == Demodulator.DEMODULATION_OFF)
//                            source.setSampleRate((Integer)sp_sampleRate.getSelectedItem());
//
//                        // Open file and start recording:
//                        recordingFile = new File(externalDir + "/" + RECORDING_DIR + "/" + filename);
//                        recordingFile.getParentFile().mkdir();	// Create directory if it does not yet exist
//                        try {
//                            scheduler.startRecording(new BufferedOutputStream(new FileOutputStream(recordingFile)));
//                        } catch (FileNotFoundException e) {
//                            Log.e(LOGTAG, "showRecordingDialog: File not found: " + recordingFile.getAbsolutePath());
//                        }
//
//                        // safe preferences:
//                        SharedPreferences.Editor edit = preferences.edit();
//                        edit.putInt(getString(R.string.pref_recordingSampleRate), (Integer) sp_sampleRate.getSelectedItem());
//                        edit.putBoolean(getString(R.string.pref_recordingStopAfterEnabled), cb_stopAfter.isChecked());
//                        edit.putInt(getString(R.string.pref_recordingStopAfterValue), stopAfterValue);
//                        edit.putInt(getString(R.string.pref_recordingStopAfterUnit), stopAfterUnit);
//                        edit.apply();
//
//                        analyzerSurface.setRecordingEnabled(true);
//
//                        updateActionBar();
//
//                        // if stopAfter was selected, start thread to supervise the recording:
//                        if(cb_stopAfter.isChecked()) {
//                            Thread supervisorThread = new Thread() {
//                                @Override
//                                public void run() {
//                                    Log.i(LOGTAG, "recording_superviser: Supervisor Thread started. (Thread: " + this.getName() + ")");
//                                    try {
//                                        long startTime = System.currentTimeMillis();
//                                        boolean stop = false;
//
//                                        // We check once per half a second if the stop criteria is met:
//                                        Thread.sleep(500);
//                                        while (recordingFile != null && !stop) {
//                                            switch (stopAfterUnit) {    // see arrays.xml - recording_stopAfterUnit
//                                                case 0: /* MB */
//                                                    if (recordingFile.length() / 1000000 >= stopAfterValue)
//                                                        stop = true;
//                                                    break;
//                                                case 1: /* GB */
//                                                    if (recordingFile.length() / 1000000000 >= stopAfterValue)
//                                                        stop = true;
//                                                    break;
//                                                case 2: /* sec */
//                                                    if (System.currentTimeMillis() - startTime >= stopAfterValue * 1000)
//                                                        stop = true;
//                                                    break;
//                                                case 3: /* min */
//                                                    if (System.currentTimeMillis() - startTime >= stopAfterValue * 1000 * 60)
//                                                        stop = true;
//                                                    break;
//                                            }
//                                        }
//                                        // stop recording:
//                                        stopRecording();
//                                    } catch (InterruptedException e) {
//                                        Log.e(LOGTAG, "recording_superviser: Interrupted!");
//                                    } catch (NullPointerException e) {
//                                        Log.e(LOGTAG, "recording_superviser: Recording file is null!");
//                                    }
//                                    Log.i(LOGTAG, "recording_superviser: Supervisor Thread stopped. (Thread: " + this.getName() + ")");
//                                }
//                            };
//                            supervisorThread.start();
//                        }
//                    }
//                })
//                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int whichButton) {
//                        // do nothing
//                    }
//                })
//                .show()
//                .getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    public void stopRecording() {
        if (scheduler.isRecording()) {
            scheduler.stopRecording();
        }
        if (recordingFile != null) {
            final String filename = recordingFile.getAbsolutePath();
            final long filesize = recordingFile.length() / 1000000;    // file size in MB
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this, "Recording stopped: " + filename + " (" + filesize + " MB)", Toast.LENGTH_LONG).show();
                }
            });
            recordingFile = null;
            updateActionBar();
        }
        if (analyzerSurface != null)
            analyzerSurface.setRecordingEnabled(false);
    }

    /**
     * Called by the analyzer surface after the user changed the channel width
     *
     * @param newChannelWidth new channel width (single sided) in Hz
     * @return true if channel width is valid; false if out of range
     */
    @Override
    public boolean onUpdateChannelWidth(int newChannelWidth) {
        if (demodulator != null)
            return demodulator.setChannelWidth(newChannelWidth);
        else
            return false;
    }

    @Override
    public void onUpdateChannelFrequency(long newChannelFrequency) {
        if (scheduler != null)
            scheduler.setChannelFrequency(newChannelFrequency);
    }

    @Override
    public void onUpdateSquelchSatisfied(boolean squelchSatisfied) {
        if (scheduler != null)
            scheduler.setSquelchSatisfied(squelchSatisfied);
    }

    @Override
    public int onCurrentChannelWidthRequested() {
        if (demodulator != null)
            return demodulator.getChannelWidth();
        else
            return -1;
    }


}
