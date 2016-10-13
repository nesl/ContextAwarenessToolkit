package edu.ucla.nesl.toolkit.common.model;

import android.hardware.Sensor;

/**
 * Created by cgshen on 10/6/16.
 */

public class SensorType {
    public static final int ANDROID_ACCELEROMETER = Sensor.TYPE_ACCELEROMETER;
    public static final int ANDROID_LINEAR_ACCELERATION = Sensor.TYPE_LINEAR_ACCELERATION;
    public static final int ANDROID_GYROSCOPE = Sensor.TYPE_GYROSCOPE;
    public static final int ANDROID_GRAVITY = Sensor.TYPE_GRAVITY;
    public static final int ANDROID_HEART_RATE = Sensor.TYPE_HEART_RATE;
    public static final int ANDROID_LIGHT = Sensor.TYPE_LIGHT;
    public static final int ANDROID_MAGNETIC_FIELD = Sensor.TYPE_MAGNETIC_FIELD;
    public static final int ANDROID_PRESSURE = Sensor.TYPE_PRESSURE;
    public static final int ANDROID_PROXIMITY = Sensor.TYPE_PROXIMITY;
    public static final int ANDROID_ROTATION_VECTOR = Sensor.TYPE_ROTATION_VECTOR;
    public static final int ANDROID_STEP_COUNTER = Sensor.TYPE_STEP_COUNTER;

    public static final int ANDROID_SENSOR_MAX = 100;

    public static final int ANDROID_LOCATION = 101;
    public static final int ANDROID_MICROPHONE = 102;
    public static final int ANDROID_CAMERA = 103;

    public static final int SOFTWARE_TIME = 201;
    public static final int SOFTWARE_DATE = 202;
    public static final int SOFTWARE_WEATHER = 203;

    public static final int INFERENCE_TRANSPORTATION_MODE = 301;

    public static int getSensorLength(int sensorType) throws InvalidSensorTypeException {
        if (sensorType < SensorType.ANDROID_SENSOR_MAX) {
            if (sensorType == Sensor.TYPE_ACCELEROMETER ||
                    sensorType == Sensor.TYPE_MAGNETIC_FIELD ||
                    sensorType == Sensor.TYPE_GYROSCOPE ||
                    sensorType == Sensor.TYPE_GRAVITY ||
                    sensorType == Sensor.TYPE_LINEAR_ACCELERATION) {
                return 3;
            }
            else if (sensorType == Sensor.TYPE_ROTATION_VECTOR) {
                return 5;
            }
            else if (sensorType == Sensor.TYPE_LIGHT ||
                    sensorType == Sensor.TYPE_PROXIMITY ||
                    sensorType == Sensor.TYPE_PRESSURE ||
                    sensorType == Sensor.TYPE_HEART_RATE ||
                    sensorType == Sensor.TYPE_STEP_COUNTER) {
                return 1;
            }
            else {
                throw new InvalidSensorTypeException("DataInstance: invalid sensor type.");
            }
        }
        return -1;
    }
}
