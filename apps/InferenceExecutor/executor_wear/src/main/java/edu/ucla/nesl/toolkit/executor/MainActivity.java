package edu.ucla.nesl.toolkit.executor;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.BoxInsetLayout;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import edu.ucla.nesl.toolkit.executor.common.communication.SharedConstant;
import edu.ucla.nesl.toolkit.executor.common.module.InferencePipeline;

public class MainActivity extends WearableActivity {
    private static final String TAG = "Wear: Activity";
    private static final SimpleDateFormat AMBIENT_DATE_FORMAT =
            new SimpleDateFormat("HH:mm", Locale.US);

    private BoxInsetLayout mContainerView;
    private TextView mTextView;
    private TextView mClockView;

    private WearInferenceManager mService;
    boolean mBound = false;
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            WearInferenceManager.LocalBinder binder =
                    (WearInferenceManager.LocalBinder) service;
            mService = binder.getService();
            mService.initService(MainActivity.this);
            mBound = true;
            Log.i(TAG, "Bind to service");
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            mBound = false;
            Log.i(TAG, "Unbind from service");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setAmbientEnabled();

        // Request permissions
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WAKE_LOCK)
                != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.VIBRATE)
                        != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.SET_ALARM)
                        != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                            android.Manifest.permission.WAKE_LOCK,
                            android.Manifest.permission.VIBRATE,
                            android.Manifest.permission.SET_ALARM
                    },
                    1);
        }

        mContainerView = (BoxInsetLayout) findViewById(R.id.container);
        mTextView = (TextView) findViewById(R.id.text);
        mClockView = (TextView) findViewById(R.id.clock);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mBound) {
            mService.stopInference();
        }

        // Unregister broadcast receiver
        LocalBroadcastManager bManager = LocalBroadcastManager.getInstance(this);
        bManager.unregisterReceiver(mReceiver);
        Log.i(TAG, "Receiver un-registered.");
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Register broadcast receiver
        LocalBroadcastManager bManager = LocalBroadcastManager.getInstance(this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(SharedConstant.PATH_CONFIGURE_INF);
        intentFilter.addAction(SharedConstant.PATH_START_INF);
        intentFilter.addAction(SharedConstant.PATH_STOP_INF);
        bManager.registerReceiver(mReceiver, intentFilter);
        Log.i(TAG, "Receiver registered.");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, WearInferenceManager.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
    }

    @Override
    public void onEnterAmbient(Bundle ambientDetails) {
        super.onEnterAmbient(ambientDetails);
        updateDisplay();
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
            mContainerView.setBackgroundColor(ContextCompat.getColor(
                    this.getApplicationContext(),
                    R.color.black));
            mTextView.setTextColor(ContextCompat.getColor(
                    this.getApplicationContext(),
                    R.color.white));
            mClockView.setVisibility(View.VISIBLE);

            mClockView.setText(AMBIENT_DATE_FORMAT.format(new Date()));
        } else {
            mContainerView.setBackground(null);
            mTextView.setTextColor(ContextCompat.getColor(
                    this.getApplicationContext(),
                    R.color.black));
            mClockView.setVisibility(View.GONE);
        }
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "Broadcast received " + intent.getAction());
            if (intent.getAction().equals(SharedConstant.PATH_CONFIGURE_INF)) {
                InferencePipeline pipeline = InferencePipeline.deserialize(
                        intent.getByteArrayExtra(SharedConstant.KEY_PIPELINE));
                if (pipeline != null) {
                    mService.configureInference(pipeline);
                }

            } else if (intent.getAction().equals(SharedConstant.PATH_START_INF)) {
                mService.startInference();
                mTextView.setText(R.string.status_running);
            }
            else if (intent.getAction().equals(SharedConstant.PATH_STOP_INF)) {
                mService.stopInference();
                mTextView.setText(R.string.status_text);
            }
        }
    };
}
