package com.krzysztofwitczak.androwearapp.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.krzysztofwitczak.androwearapp.R;
import com.krzysztofwitczak.androwearapp.emotions.EmotionClassifier;
import com.krzysztofwitczak.androwearapp.wear_connection.WearListCallListenerService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.opencsv.CSVWriter;

public class MainActivity extends AppCompatActivity {
    static final String LOG_KEY = "MOBILE_MAIN_ACTIVITY";

    private TextView mHeartRateView;
    private TextView mEmotionView;
    private TextView mThresholdView;

    Button clickButton;
    String EntryContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(LOG_KEY, "onCreate() called!");

        setContentView(R.layout.activity_main);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        mHeartRateView = (TextView) findViewById(R.id.heart_rate_mobile);
        mEmotionView = (TextView) findViewById(R.id.emotionType);
        mThresholdView = (TextView) findViewById(R.id.thresholds);

        ImageView imageView = (ImageView) findViewById(R.id.circle);

        clickButton = (Button) findViewById(R.id.submit);

        clickButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //mRateValue ="666";

                Toast.makeText(getApplicationContext(), "data saved", Toast.LENGTH_LONG).show();
                writeFileInternal("WearLogFile.txt",getBaseContext());



            }
        });

        //  Animation pulse = AnimationUtils.loadAnimation(this, R.anim.pulse);
        // imageView.startAnimation(pulse);

        LocalBroadcastManager.getInstance(this).registerReceiver(
                new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        mHeartRateView.setText(
                                String.format(
                                        "%s",
                                        intent.getStringExtra(
                                                WearListCallListenerService.HEART_RATE)));

                        //disable emotion calculation.

    /*                    String emotionText;
                        String certainty = intent.getStringExtra(
                                WearListCallListenerService.EMOTION_CERTAINTY);
                        if(certainty.equals("0")) {
                            emotionText = "Analyzing...";
                        } else {
                            emotionText = String.format(
                                    "%s (%s%%)",
                                    intent.getStringExtra(WearListCallListenerService.EMOTION_NAME),
                                    certainty);
                        }
                        mEmotionView.setText(emotionText);

                        String thresholds = intent.getStringExtra(
                                WearListCallListenerService.EMOTION_THRESHOLDS);
                        if(thresholds.equals("0 - 0 Bpm")) thresholds = "Analyzing...";
                        mThresholdView.setText(thresholds);*/

                    }
                }, new IntentFilter(WearListCallListenerService.BROADCAST_NAME)
        );
    }

    public void writeFileInternal(String filename, Context ctx) {


//        final byte[] data = "Hahahahah".getData();

       File fileDir = getApplicationContext().getExternalFilesDir("WearableLogger");

        //File fileDir="/data/tech.usee.subjectid.wearlogfiles";

        if (fileDir != null && !fileDir.exists()) {
            fileDir.mkdir();
        }

        String fileFullPath = String.format("%s/%s", fileDir, "logfile.txt");
        File f = new File(fileFullPath);
        CSVWriter writer;
        String[] contents;

        try {

            // File exist
            if (f.exists() && !f.isDirectory()) {
                writer = new CSVWriter(new FileWriter(fileFullPath, true));
            } else {    // File not exist
                writer = new CSVWriter(new FileWriter(fileFullPath));
                contents = new String[]{"startTime", "startTimeHuman", "EndTime","EndTimeHuman",  "EntryContent"};
                writer.writeNext(contents);
            }

            // final byte[] data = "Hahahahah,babba,dadas";
            //contents = new String(data).split(",");
            EntryContent = mHeartRateView.getText().toString();
            contents = new String[]{ EntryContent};
            writer.writeNext(contents);
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void writeFile(String filename, Context ctx) {

        //String filename = "mysecondfile";
        String outputString = "Hello world!";
        //File myDir = getFilesDir();
        String myDir="/data/tech.usee.subjectid.wearlogfiles";
        Log.i("Debug", "myDir:" + myDir);


        try {
            // File secondFile = new File(myDir + "/", filename);



            File secondFile = new File(filename);

            //Log.i("Debug", "secondFile.getParentFile():" + secondFile.getParentFile().toString());


            //secondFile.createNewFile();
            FileOutputStream fos = openFileOutput(filename, Context.MODE_PRIVATE);

            String filePath = ctx.getFilesDir().getPath().toString() + "/"+ filename;
            Log.i("Debug", "ctx.getFilesDir().getPath().toString() :" + ctx.getFilesDir().getPath().toString() );


            Log.i("Debug", "I reach here:" + myDir);

            long dtMili = System.currentTimeMillis();

            DateFormat df = new SimpleDateFormat("EEE, d MMM yyyy, HH:mm");
            String currentDateandTime = df.format(Calendar.getInstance().getTime());

            String outline = String.valueOf(dtMili) + "\t" + currentDateandTime + "\t" +  "\n";

            fos.write(outline.getBytes());
            fos.flush();
            fos.close();
            Log.i("Debug", "file finished writing:" + myDir);

        } catch (Exception e) {
            e.printStackTrace();
        }




    }


    // Menu icons are inflated just as they were with actionbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(LOG_KEY, "onStart() called!");
        startService(new Intent(MainActivity.this, WearListCallListenerService.class));
    }

    public void openGameSetting(MenuItem item) {
        Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
        startActivity(intent);
    }

    public void onRadioButtonClicked(View view) {
        EmotionClassifier.currentAlgorithm = EmotionClassifier.Algorithm.DEFAULT;
    }

    public void showEmulateModal(View view) {
        new EmulateDialog().show(getSupportFragmentManager(), "EmulateDialog");
    }

    public void resetProfile(MenuItem item) {
        EmotionClassifier.getInstance().resetProfile();
    }
}
