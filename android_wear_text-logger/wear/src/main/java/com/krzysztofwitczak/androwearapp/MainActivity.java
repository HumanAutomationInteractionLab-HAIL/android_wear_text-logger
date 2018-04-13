package com.krzysztofwitczak.androwearapp;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.BoxInsetLayout;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends WearableActivity implements
        SensorEventListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener  {

    static final String LOG_KEY = "WEAR_MAIN_ACTIVITY";

    // Layout variables
    private BoxInsetLayout mContainerView;
    private TextView mTextView;

    // Connect to mobile
    private GoogleApiClient mGoogleApiClient;
    private Node mNode; // the connected device to send the message to
    private Timer mTimer;

    // Sensor variables
    //private SensorManager mSensorManager;
    private Sensor mHearRateSensor;
    private String mRateValue = "Rate unknown";
    private String lastRateValue;
    private boolean isConnected = false;

    //Sensor mHeartRateSensor;
    Sensor accelerometer;
    Sensor mGyroscopeSensor;
    SensorManager sm;
    SensorManager mSensorManager;
    TextView acceleration;
    TextView heart;
    private static final int SENSOR_TYPE_HEART_RATE = 65562;

    Button clickButton ;

    EditText mEdit;

     long startTimeFloat,endTimeFloat,  taskCompletionTime,currentTimeFloat;
    String startTimeHuman,endTimeHuman,currentTimeHuman;
    DateFormat df;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setAmbientEnabled();

        df = new SimpleDateFormat("EEE, d MMM yyyy, HH:mm:ss");



        ImageView imageView = (ImageView) findViewById(R.id.heart);
        Animation pulse = AnimationUtils.loadAnimation(this, R.anim.pulse);
        //disable image heart animation - by HJB
        //imageView.startAnimation(pulse);

        mContainerView = (BoxInsetLayout) findViewById(R.id.container);
        mTextView = (TextView) findViewById(R.id.text);
        clickButton = (Button) findViewById(R.id.submit);
        mEdit = (EditText) findViewById(R.id.editText);

         startTimeFloat = System.currentTimeMillis();

        final DateFormat df = new SimpleDateFormat("EEE, d MMM yyyy, HH:mm:ss");
         startTimeHuman = df.format(Calendar.getInstance().getTime());


        clickButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //mRateValue ="666";

                mRateValue =   mEdit.getText().toString();

                updateDisplay();

                currentTimeFloat = System.currentTimeMillis();

                currentTimeHuman = df.format(Calendar.getInstance().getTime());

                lastRateValue = String.valueOf(currentTimeFloat)+","+currentTimeHuman+","+ ","+ mRateValue;

            }
        });

        mGoogleApiClient = new GoogleApiClient.Builder(MainActivity.this)
                                             .addApi(Wearable.API)
                                             .addConnectionCallbacks(this)
                                             .addOnConnectionFailedListener(this)
                                             .build();
        mTimer = new Timer();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(LOG_KEY, "onStart");
        mGoogleApiClient.connect();

        mTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (!isConnected || lastRateValue == null) {
                    return;
                }
               // new SendToDataLayerThread(lastRateValue, lastRateValue).start();

                new SendToDataLayerThread(lastRateValue, lastRateValue).start();


            }
        }, 0, 1000);


    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(LOG_KEY, "onResume");

        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BODY_SENSORS)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    MainActivity.this,
                    new String[]{Manifest.permission.BODY_SENSORS},
                    1);
        }

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mHearRateSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);
        mSensorManager.registerListener(this, mHearRateSensor, SensorManager.SENSOR_DELAY_FASTEST);

        //mSensorManager = ((SensorManager) getSystemService(SENSOR_SERVICE));
        //mHeartRateSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);
        accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        //mSensorManager.registerListener(this, mHeartRateSensor, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);

        mGyroscopeSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mSensorManager.registerListener(this, mGyroscopeSensor, 40000);


    }

    protected void onPause() {
        super.onPause();
        Log.i(LOG_KEY, "onPause");
    }

    protected void onStop() {
        super.onStop();
        Log.i(LOG_KEY, "onStop() called - shutting of HearRate Listener!");
        mSensorManager.unregisterListener(this);
        mTimer.cancel();
        mTimer.purge();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {


        // TODO Auto-generate method stub

        if (event.sensor.getType() == Sensor.TYPE_HEART_RATE)
        {
            String msg = "" + (int) event.values[0];
            System.out.println("heart"+event.values[0]);
            //heart.setText(msg);

            mRateValue = "Heart rate:"+Integer.toString(Math.round(event.values[0]));

            // Log.d(TAG, msg);


        }

          if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)

        {
           // acceleration.setText("X: " + event.values[0] +
             //       "\nY: " + event.values[1] +
               //     "\nZ: " + event.values[2]);
            System.out.println("X: " + event.values[0] +
                    "\nY: " + event.values[1] +
                    "\nZ: " + event.values[2]);

            //mRateValue = Integer.toString(Math.round(event.values[0]));
           // mRateValue ="TYPE_ACCELEROMETER";

            mRateValue = "Accelerometer: X:"+String.valueOf(event.values[0])+"\n"+"Y:"+String.valueOf(event.values[1])+"\n"+"Z:"+String.valueOf(event.values[2]);
            //, event.values[1], event.values[2]);

//            Log.d(TAG, msg);
        }

        if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            Log.d("gyro", event.timestamp + "\t" + event.values[0] + "\t" + event.values[1] + "\t" + event.values[2]);
            mRateValue = "Gyroscope: X:"+String.valueOf(event.values[0])+"\n"+"Y:"+String.valueOf(event.values[1])+"\n"+"Z:"+String.valueOf(event.values[2]);

        }

        //mRateValue = Integer.toString(Math.round(event.values[0]));
        //mRateValue ="999";

        updateDisplay();

        //when we disable this, the sensor value change will not be sent to the pad/phone
        //lastRateValue = mRateValue;

       // lastRateValue = "99999";

         currentTimeFloat = System.currentTimeMillis();

         //df = new SimpleDateFormat("EEE, d MMM yyyy, HH:mm:ss");
        currentTimeHuman = df.format(Calendar.getInstance().getTime());

        lastRateValue = String.valueOf(currentTimeFloat)+","+currentTimeHuman+","+ ","+ mRateValue;

/*
        mGoogleApiClient = new GoogleApiClient.Builder(MainActivity.this)
        .addApi(Wearable.API)
        .addConnectionCallbacks(this)
        .addOnConnectionFailedListener(this)
        .build();
        mTimer = new Timer();*/

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Log.i(LOG_KEY, "Rate accuracy changed!");
    }

    @Override
    public void onEnterAmbient(Bundle ambientDetails) {
        super.onEnterAmbient(ambientDetails);
        updateDisplay();
        Log.i(LOG_KEY, "onEnterAmbient() - Turning off the screen...");
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(LOG_KEY, "Google API connection successful!");
        Wearable.NodeApi.getConnectedNodes(mGoogleApiClient)
                .setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
                    @Override
                    public void onResult(@NonNull NodeApi.GetConnectedNodesResult nodes) {
                        for (Node node : nodes.getNodes()) {
                            Log.i(LOG_KEY, node.toString());
                            mNode = node;
                        }
                    }
                });
        isConnected = true;
    }

    private class SendToDataLayerThread extends Thread {
        String path;
        String message;

        // Constructor to send a message to the data layer
        SendToDataLayerThread(String p, String msg) {
            path = p;
            message = msg;
        }

        public void run() {

            try {
                MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(
                        mGoogleApiClient, mNode.getId(), path, message.getBytes()).await();

                if (result.getStatus().isSuccess()) {
                    Log.v(LOG_KEY, "Message: {" + message + "} sent to: " + mNode.getDisplayName());
                } else {
                    Log.v(LOG_KEY, "ERROR: failed to send Message");
                }
            }
            finally {
                Log.v(LOG_KEY, "failure to send message, but i want the app to live.");


            }
        }

    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    @Override
    public void onUpdateAmbient() {
        super.onUpdateAmbient();
        updateDisplay();
    }

    @Override
    public void onExitAmbient() {
        updateDisplay();
        super.onExitAmbient();
    }

    private void updateDisplay() {
        if (isAmbient()) {
            mContainerView.setBackgroundColor(getResources().getColor(android.R.color.black));
        } else {
            mContainerView.setBackground(null);
        }

//        mTextView.setText(String.format("%s bmp", mRateValue));
        mTextView.setText(String.format("%s ", mRateValue));

    }
}
