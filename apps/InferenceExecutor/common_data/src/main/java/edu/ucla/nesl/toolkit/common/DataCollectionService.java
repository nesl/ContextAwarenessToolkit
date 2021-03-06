package edu.ucla.nesl.toolkit.common;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.Vibrator;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import edu.ucla.nesl.toolkit.common.model.DataInstance;
import edu.ucla.nesl.toolkit.common.model.LabeledDataVector;
import edu.ucla.nesl.toolkit.common.model.type.DataType;
import edu.ucla.nesl.toolkit.common.model.DataVector;
import edu.ucla.nesl.toolkit.common.model.type.DeviceType;
import edu.ucla.nesl.toolkit.common.model.type.SensorType;

/**
 * Created by cgshen on 10/11/16.
 */

public class DataCollectionService extends Service implements SensorEventListener {
    private static final String TAG = "DataCollector";

    private static SensorManager mSensorManager;
    private static List<Sensor> mSensorList;

    private static Vibrator mVibrator;
    private static DataCollectionService mDataCollectionService;
    private static PowerManager.WakeLock mWakeLock;

    private static DeviceType mDeviceType;
    private static DataVector mDataVector;

    private static LabelCollectionService mLabelCollectionService;

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind()");
        return mBinder;
    }

    private final IBinder mBinder = new MyBinder();
    public class MyBinder extends Binder {
        public DataCollectionService getService() {
            return DataCollectionService.this;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand(): id=" + startId);
        mDataCollectionService = DataCollectionService.this;
        return START_STICKY;
    }

    public static void configureDataCollection(DeviceType deviceType, DataVector dataVector) {
        Log.d(TAG, "configureDataCollection()");
        if (mDataCollectionService != null) {
            mDeviceType = deviceType;
            mDataVector = dataVector;
            mSensorList = new ArrayList<>();
            mSensorManager = ((SensorManager)
                    mDataCollectionService.getSystemService(SENSOR_SERVICE));
            PowerManager pm = (PowerManager)
                    mDataCollectionService.getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock( PowerManager.PARTIAL_WAKE_LOCK, TAG);
            mVibrator = (Vibrator) mDataCollectionService.getSystemService(
                    Context.VIBRATOR_SERVICE);
        }
        else {
            Log.e(TAG, "Error: context is null.");
        }
    }

    public static void startDataCollection(Context context) {
        Log.d(TAG, "startDataCollection()");
        if (isDataCollectionConfigured()) {
            // Acquire a wakelock
            mWakeLock.acquire();

            // Start the sensor data collection
            registerAllSensors();

            // If necessary, start label collection
            if (mDataVector instanceof LabeledDataVector) {
                mLabelCollectionService = new LabelCollectionService(
                        (LabeledDataVector) mDataVector);
                mLabelCollectionService.setLabelCollectionAlarm(context);
            }

            // Send a vibration feedback
            mVibrator.vibrate(800);
        }
        else {
            Log.e(TAG, "Data collection not configured, call configureDataCollection() first");
        }
    }

    public static DataVector stopDataCollection() {
        Log.d(TAG, "stopDataCollection()");

        // Unregister sensors
        unregisterAllSensors();
        mSensorManager = null;
        mSensorList.clear();
        mSensorList = null;

        // If necessary, stop label collection
        if (mDataVector instanceof LabeledDataVector) {
            mLabelCollectionService.cancelLabelCollectionAlarm();
            mLabelCollectionService = null;
        }

        // Send a (shorter) vibration feedback
        mVibrator.vibrate(300);
        mVibrator = null;

        // Release wakelock
        if (mWakeLock != null) {
            mWakeLock.release();
        }
        mWakeLock = null;

        // Return the data collected
        return mDataVector;
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        int currentSensorType = sensorEvent.sensor.getType();
        mDataVector.addDataInstance(
                mDeviceType,
                currentSensorType,
                new DataInstance(sensorEvent.timestamp, sensorEvent.values));
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    private static boolean isDataCollectionConfigured() {
        return mSensorManager != null &&
                mWakeLock != null &&
                mDataCollectionService != null &&
                mVibrator != null;
    }

    private static void registerAllSensors() {
        // Setup each sensor in the current configuration
        for (DataType dataType : mDataVector.getData().keySet()) {
            // Only collect sensor data with the same device type
            if (dataType.getDeviceType() == mDeviceType &&
                    dataType.getSensorType() < SensorType.ANDROID_SENSOR_MAX) {
                Sensor currentSensor = mSensorManager.getDefaultSensor(
                        dataType.getSensorType());
                mSensorList.add(currentSensor);
                mSensorManager.registerListener(
                        mDataCollectionService,
                        currentSensor,
                        SensorManager.SENSOR_DELAY_FASTEST);
            }
        }
    }

    private static void unregisterAllSensors() {
        // Unregister all sensors in the current configuration
        for (Sensor sensor : mSensorList) {
            mSensorManager.unregisterListener(mDataCollectionService, sensor);
        }
    }

    @Override
    public void onDestroy () {
        super.onDestroy();

        // Clean up if the data collection is still on
        if (isDataCollectionConfigured()) {
            stopDataCollection();
        }
    }
}
