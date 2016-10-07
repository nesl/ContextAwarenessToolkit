package edu.ucla.nesl.toolkit.common.model;

/**
 * Created by cgshen on 10/7/16.
 */

public class TypedDataInstance extends DataInstance {
    private DeviceType deviceType;
    private SensorType sensorType;

    public TypedDataInstance(
            long timestamp,
            double[] values,
            DeviceType deviceType,
            SensorType sensorType) {
        super(timestamp, values);
        this.deviceType = deviceType;
        this.sensorType = sensorType;
    }

    public DeviceType getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(DeviceType deviceType) {
        this.deviceType = deviceType;
    }

    public SensorType getSensorType() {
        return sensorType;
    }

    public void setSensorType(SensorType sensorType) {
        this.sensorType = sensorType;
    }
}
