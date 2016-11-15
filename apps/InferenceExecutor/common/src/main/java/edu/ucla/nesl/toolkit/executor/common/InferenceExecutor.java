package edu.ucla.nesl.toolkit.executor.common;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.PowerManager;
import android.util.Log;

import java.util.List;

import edu.ucla.nesl.toolkit.executor.common.ble.BLEDataMapClient;
import edu.ucla.nesl.toolkit.executor.common.module.DataInterface;
import edu.ucla.nesl.toolkit.executor.common.module.InferencePipeline;

/**
 * Created by cgshen on 11/13/16.
 */

public abstract class InferenceExecutor
        extends BroadcastReceiver
        implements SensorEventListener {
    private final static String TAG = "InfExecutor";

    private InferencePipeline mInferencePipeline;
    private DataInterface source;
    private DataInterface sink;
    private long interval;
    private long duration;

    private SensorManager mSensorManager;
    private BLEDataMapClient mBleClient;

    private static int resCount = 0;
    private static int numThreads;
    private static final Object lock = new Object();

    private List<List<Float>> dataBuffer;

    public InferenceExecutor() {

    }

    public InferenceExecutor(InferencePipeline mInferencePipeline, long interval, long duration) {
        this.mInferencePipeline = mInferencePipeline;
        this.interval = interval;
        this.duration = duration;

        // By default, the entire inference runs on a single device
        this.source = DataInterface.SENSOR;
        this.sink = DataInterface.NOTIFICATION;
    }

    public InferenceExecutor(
            InferencePipeline mInferencePipeline,
            DataInterface source,
            DataInterface sink,
            long interval,
            long duration) {
        this.mInferencePipeline = mInferencePipeline;
        this.source = source;
        this.sink = sink;
        this.interval = interval;
        this.duration = duration;
    }

    public void setInferenceAlarm(Context context) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(context, InferenceExecutor.class);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);
        am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), this.interval, pi);
        Log.i(TAG, "Alarm set.");
    }

    public void cancelInferenceAlarm(Context context) {
        Intent intent = new Intent(context, InferenceExecutor.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);
        Log.i(TAG, "Alarm cancelled.");
    }


    @Override
    public void onReceive(Context context, Intent intent) {
        PowerManager mPowerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = mPowerManager.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                "inf_wakelock");
        wl.acquire();
        Log.i(TAG, "InferenceExecutor received alarm.");

        // Check if we have a valid inference pipeline
        if (mInferencePipeline == null)
            Log.e(TAG, "Error: no inference specified");

        // Setup data source (sensor or getting from radio)
        if (source == DataInterface.SENSOR) {
            // Start inference
            mSensorManager = ((SensorManager) context.getSystemService(Context.SENSOR_SERVICE));
            for (int sensorType : mInferencePipeline.getSensors()) {
                // Register the listener for the requested sensor type
                Sensor currentSensor = mSensorManager.getDefaultSensor(sensorType);
                if (currentSensor != null) {
                    mSensorManager.registerListener(
                            this,
                            currentSensor,
                            SensorManager.SENSOR_DELAY_FASTEST);
                } else {
                    Log.e(TAG, "Error: sensor not found " + currentSensor.getName());
                }
            }

            // Stop after the sensing duration
            Handler mHandler = new Handler();
            mHandler.postDelayed(
                    new Runnable() {
                        @Override
                        public void run() {
                            mSensorManager.unregisterListener(InferenceExecutor.this);
                            mSensorManager = null;
                            Log.i(TAG, "Inference execution finished.");
                        }
                    },
                    duration);
        }
        else {
            Log.e(TAG, "Error: unsupported data source");
        }

        // Setup data sink (radio or just notification)
        if (sink == DataInterface.BLE) {
            mBleClient = BLEDataMapClient.getInstance(context);
        }
        else if (sink == DataInterface.NOTIFICATION) {

        }
        else {
            Log.e(TAG, "Error: unsupported data sink");
        }
        wl.release();
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        // Perform the actual inference using mInferencePipeline
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    public InferencePipeline getmInferencePipeline() {
        return mInferencePipeline;
    }

    public void setmInferencePipeline(InferencePipeline mInferencePipeline) {
        this.mInferencePipeline = mInferencePipeline;
    }

    public DataInterface getSink() {
        return sink;
    }

    public void setSink(DataInterface sink) {
        this.sink = sink;
    }

    public DataInterface getSource() {
        return source;
    }

    public void setSource(DataInterface source) {
        this.source = source;
    }

    public long getInterval() {
        return interval;
    }

    public void setInterval(long interval) {
        this.interval = interval;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public List<List<Float>> getDataBuffer() {
        return dataBuffer;
    }

    public void setDataBuffer(List<List<Float>> dataBuffer) {
        this.dataBuffer = dataBuffer;
    }
}
