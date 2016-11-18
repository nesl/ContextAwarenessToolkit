package edu.ucla.nesl.toolkit.executor;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import edu.ucla.nesl.toolkit.executor.common.communication.SharedConstant;
import edu.ucla.nesl.toolkit.executor.common.module.InferencePipeline;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "Mobile: Activity";

    private TextView statusText;
    private TextView deviceText;

    private MobileInferenceManager mService;
    boolean mBound = false;
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            MobileInferenceManager.LocalBinder binder =
                    (MobileInferenceManager.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
            Log.i(TAG, "Bind to service");
            mService.initService(MainActivity.this.getApplicationContext());
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

        // Get UI components
        statusText = (TextView) findViewById(R.id.status_text);
        deviceText = (TextView) findViewById(R.id.device_text);

        // Keep screen on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    public void onStartClicked(View view) {
        Log.i(TAG, "start clicked");
        if (mBound) {
            mService.startInference();
        }
    }

    public void onStopClicked(View view) {
        Log.i(TAG, "stop clicked");
        if (mBound) {
            mService.stopInference();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Unbind the service
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
        intentFilter.addAction(SharedConstant.PATH_START_INF);
        intentFilter.addAction(SharedConstant.PATH_STOP_INF);
        intentFilter.addAction(MobileInferenceManager.PHONE_INF_STARTED);
        intentFilter.addAction(MobileInferenceManager.PHONE_INF_STOPPED);
        bManager.registerReceiver(mReceiver, intentFilter);
        Log.i(TAG, "Receiver registered.");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, MobileInferenceManager.class);
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

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "Broadcast received " + intent.getAction());
            if (intent.getAction().equals(SharedConstant.PATH_INF_STARTED)) {
                statusText.setText(R.string.status_running);
                deviceText.setText(R.string.device_watch);
            }
            else if (intent.getAction().equals(SharedConstant.PATH_INF_STOPPED)) {
                statusText.setText(R.string.status_stopped);
                deviceText.setText(R.string.device_none);
            }
            else if (intent.getAction().equals(MobileInferenceManager.PHONE_INF_STARTED)) {
                statusText.setText(R.string.status_running);
                deviceText.setText(R.string.device_phone);
            }
            else if (intent.getAction().equals(MobileInferenceManager.PHONE_INF_STOPPED)) {
                statusText.setText(R.string.status_stopped);
                deviceText.setText(R.string.device_none);
            }
        }
    };

    static {
        System.loadLibrary("native-lib");
    }
}
