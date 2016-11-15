package edu.ucla.nesl.toolkit.common.model.type;

import java.io.Serializable;

/**
 * Created by cgshen on 10/12/16.
 */

public class DataType implements Serializable {
    private DeviceType deviceType;
    private int sensorType;

    public DataType(DeviceType deviceType, int sensorType) {
        this.deviceType = deviceType;
        this.sensorType = sensorType;
    }

    public DeviceType getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(DeviceType deviceType) {
        this.deviceType = deviceType;
    }

    public int getSensorType() {
        return sensorType;
    }

    public void setSensorType(int sensorType) {
        this.sensorType = sensorType;
    }
}
