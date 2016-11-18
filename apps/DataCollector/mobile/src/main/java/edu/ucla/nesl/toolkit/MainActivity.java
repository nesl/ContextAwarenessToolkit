package edu.ucla.nesl.toolkit;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;

import edu.ucla.nesl.toolkit.common.DataCollectionService;
import edu.ucla.nesl.toolkit.common.model.DataVector;
import edu.ucla.nesl.toolkit.common.model.InvalidDataVectorTypeException;
import edu.ucla.nesl.toolkit.common.model.type.DeviceType;
import edu.ucla.nesl.toolkit.common.model.InvalidSensorTypeException;
import edu.ucla.nesl.toolkit.common.model.type.LabelDataType;
import edu.ucla.nesl.toolkit.common.model.type.LabelType;
import edu.ucla.nesl.toolkit.common.model.type.SensorType;
import edu.ucla.nesl.toolkit.common.DataCollectionConfigurator;
import edu.ucla.nesl.toolkit.common.util.TimeString;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    // Set format for date and time
    private static final TimeString mTimestring = new TimeString();

    private TextView mStatusTextView;
    private TextView mCounterTextView;
    private Button mStartButton;
    private Button mStopButton;

    private boolean mTracking = false;
    private String mTime = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Request permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WAKE_LOCK)
                        != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.VIBRATE)
                        != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.SET_ALARM)
                        != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WAKE_LOCK,
                            Manifest.permission.VIBRATE,
                            Manifest.permission.SET_ALARM
                    },
                    1);
        }

        // Keep screen on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Get UI elements
        mStatusTextView = (TextView) findViewById(R.id.status_text);
        mCounterTextView = (TextView) findViewById(R.id.counter_text);
        mStartButton = (Button) findViewById(R.id.start_button);
        mStopButton = (Button) findViewById(R.id.stop_button);

        // Get saved recording state from shared preference
        SharedPreferences sharedPref = getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        mTracking = sharedPref.getBoolean(getString(R.string.saved_rec_flag), false);
        if (mTracking) {
            mTime = sharedPref.getString(getString(R.string.saved_rec_time), "");
            mStatusTextView.setText(String.format(getString(R.string.status_text_start), mTime));
            mStartButton.setEnabled(false);
        } else {
            mStatusTextView.setText(R.string.status_text);
            mStopButton.setEnabled(false);
        }

        if (!mTracking) {
            // Start the DataCollectionService
            Intent intent = new Intent(this, DataCollectionService.class);
            startService(intent);
        }
    }

    public void onStartClicked(View view) {
        Log.i(TAG, "start clicked");
        if (!mTracking) {
            try {
                // Configure the DataVector for data collection
                DataCollectionConfigurator configurator = new DataCollectionConfigurator(true);
                configurator.addSensorTypeToVector(
                        DeviceType.ANDROID_PHONE,
                        SensorType.ANDROID_ACCELEROMETER);
                configurator.addSensorTypeToVector(
                        DeviceType.ANDROID_PHONE,
                        SensorType.ANDROID_GRAVITY);
                LabelType labelType = new LabelType("ground_truth", LabelDataType.NOMINAL);
                labelType.setInterval(10);
                labelType.addCandidateNominalValue("Activity A");
                labelType.addCandidateNominalValue("Activity B");
                labelType.addCandidateNominalValue("Activity C");
                try {
                    configurator.addLabelType(labelType);
                }
                catch (InvalidDataVectorTypeException ex) {
                    ex.printStackTrace();
                }

                // Configure the data collection service
                DataCollectionService.configureDataCollection(
                        DeviceType.ANDROID_PHONE,
                        configurator.getDataVector());

                // Start the data collection
                mTime = mTimestring.currentTimeForFile();
                DataCollectionService.startDataCollection(MainActivity.this);

                // Update status
                mTracking = true;
                mStatusTextView.setText(String.format(getString(R.string.status_text_start), mTime));
                mStartButton.setEnabled(false);
                mStopButton.setEnabled(true);
            }
            catch (InvalidSensorTypeException e) {
                e.printStackTrace();
            }
        }
        else {
            Log.w(TAG, "Tracking already started!");
        }
    }

    public void onStopClicked(View view) {
        Log.i(TAG, "stop clicked");
        if (mTracking) {
            // Get the collected data object
            DataVector result = DataCollectionService.stopDataCollection();

            // Dump the data to file
            try {
                result.dumpAsObject(
                        Environment.getExternalStorageDirectory().getAbsolutePath()
                                + "/data_file_" + mTime);
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Update display status
            mStatusTextView.setText("Tracking stopped (" + mTime + ")");
            mTracking = false;
            mTime = null;
            mStartButton.setEnabled(true);
            mStopButton.setEnabled(false);
        }
        else {
            Log.w(TAG, "Tracking already stopped!");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause()");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume()");
    }
}
