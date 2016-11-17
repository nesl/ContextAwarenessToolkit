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

import java.util.ArrayList;
import java.util.List;

import edu.ucla.nesl.toolkit.common.model.DataInstance;
import edu.ucla.nesl.toolkit.common.model.SizedDataVector;
import edu.ucla.nesl.toolkit.common.model.type.DeviceType;
import edu.ucla.nesl.toolkit.executor.common.module.Classifier;
import edu.ucla.nesl.toolkit.executor.common.module.DataInterface;
import edu.ucla.nesl.toolkit.executor.common.module.InferencePipeline;
import edu.ucla.nesl.toolkit.executor.common.module.StringConstant;
import edu.ucla.nesl.toolkit.executor.common.util.SensorRate;

/**
 * Created by cgshen on 11/13/16.
 */

public class InferenceExecutor
        extends BroadcastReceiver
        implements SensorEventListener {
    private final static String TAG = "InfExecutor";

    private static InferencePipeline mInferencePipeline;
    private static long interval;
    private static long duration;

    private static DeviceType deviceType;
    private static DataInterface source = DataInterface.SENSOR;
    private static DataInterface sink = DataInterface.NOTIFICATION;

    private static SensorManager mSensorManager;
    private static SensorRate mSensorRate = SensorRate.getDefaultSensorRate();

    private boolean running = false;
    private int resCount = 0;

    private static int numThreads;
    private static final Object lock = new Object();

    private static SizedDataVector dataBuffer;

    public void startInferenceAlarm(Context context) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(context, InferenceExecutor.class);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);
        am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), interval, pi);
        running = true;
        Log.i(TAG, "Inference alarm set.");
    }

    public void stopInferenceAlarm(Context context) {
        Intent intent = new Intent(context, InferenceExecutor.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);
        running = false;
        Log.i(TAG, "Inference alarm cancelled.");
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
        if (mInferencePipeline == null) {
            Log.e(TAG, "Error: no inference specified");
            wl.release();
            return;
        }

        // Setup data source (sensor or getting from radio)
        if (source == DataInterface.SENSOR) {
            // Initialize a data buffer with size limit
            dataBuffer = new SizedDataVector(
                    mInferencePipeline.getWindowSize() * mSensorRate.getFreq());

            // Start inference
            mSensorManager = ((SensorManager) context.getSystemService(Context.SENSOR_SERVICE));
            for (int sensorType : mInferencePipeline.getSensors()) {
                // Initialize the data vector
                dataBuffer.addDataType(deviceType, sensorType);

                // Register the listener for the requested sensor type
                Sensor currentSensor = mSensorManager.getDefaultSensor(sensorType);
                if (currentSensor != null) {
                    mSensorManager.registerListener(
                            this,
                            currentSensor,
                            mSensorRate.getSystemLevel());
                } else {
                    Log.e(TAG, "Error: sensor not found " + sensorType);
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
                            dataBuffer = null;
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

        }
        else if (sink == DataInterface.NOTIFICATION) {

        }
        else {
            Log.e(TAG, "Error: unsupported data sink");
        }
        wl.release();
    }

    private void executeInferencePipeline(List<DataInstance> data) {
        List<DataInstance> process_data = null;
        if (mInferencePipeline.getModules().containsKey(StringConstant.MOD_PREPROCESS)) {
            process_data = mInferencePipeline.getModules().get(
                    StringConstant.MOD_PREPROCESS).process(data);
        }
        if (mInferencePipeline.getModules().containsKey(StringConstant.MOD_FEATURE)) {
            process_data = mInferencePipeline.getModules().get(
                    StringConstant.MOD_FEATURE).process(data);
        }
        if (mInferencePipeline.getModules().containsKey(StringConstant.MOD_CLASSIFIER)) {
            String label = ((Classifier) mInferencePipeline.getModules().get(
                    StringConstant.MOD_CLASSIFIER)).getLabel(process_data);
            if (sink == DataInterface.NOTIFICATION) {
                Log.i(TAG, "Inference result: " + label);
                resCount++;
            }
            else if (sink == DataInterface.BLE) {
                // TODO: send data to BLE using mBleClient
            }
        }
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (dataBuffer.getDataLength(deviceType, sensorEvent.sensor.getType())
                < dataBuffer.getSizeLimit()) {
            // Append data to buffer
            dataBuffer.addDataInstance(
                    deviceType,
                    sensorEvent.sensor.getType(),
                    new DataInstance(sensorEvent.timestamp, sensorEvent.values));
        }
        else {
            // Perform inference
            List<DataInstance> data = dataBuffer.getDataInstance(
                    deviceType,
                    sensorEvent.sensor.getType());
            List<DataInstance> dataCopy = new ArrayList<>();
            for (DataInstance di : data) {
                dataCopy.add(new DataInstance(di));
            }
            dataBuffer.clearData();
            executeInferencePipeline(dataCopy);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    public DeviceType getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(DeviceType deviceType) {
        InferenceExecutor.deviceType = deviceType;
    }

    public InferencePipeline getInferencePipeline() {
        return mInferencePipeline;
    }

    public void setInferencePipeline(InferencePipeline mInferencePipeline) {
        InferenceExecutor.mInferencePipeline = mInferencePipeline;
    }

    public DataInterface getSink() {
        return sink;
    }

    public void setSink(DataInterface sink) {
        InferenceExecutor.sink = sink;
    }

    public DataInterface getSource() {
        return source;
    }

    public void setSource(DataInterface source) {
        InferenceExecutor.source = source;
    }

    public long getInterval() {
        return interval;
    }

    public void setInterval(long interval) {
        InferenceExecutor.interval = interval;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        InferenceExecutor.duration = duration;
    }

    public SensorRate getmSensorRate() {
        return mSensorRate;
    }

    public void setmSensorRate(SensorRate mSensorRate) {
        InferenceExecutor.mSensorRate = mSensorRate;
    }

    public int getResCount() {
        return resCount;
    }

    public void setResCount(int resCount) {
        this.resCount = resCount;
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }
}
