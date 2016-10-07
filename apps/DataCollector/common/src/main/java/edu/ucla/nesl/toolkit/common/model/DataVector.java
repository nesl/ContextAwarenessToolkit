package edu.ucla.nesl.toolkit.common.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by cgshen on 10/6/16.
 */

public class DataVector implements Serializable {
    private DeviceType deviceType;
    private SensorType sensorType;
    private List<DataInstance> data;

    public DataVector(SensorType sensorType, DeviceType deviceType) {
        this.sensorType = sensorType;
        this.deviceType = deviceType;
        this.data = new ArrayList<>();
    }

    public void addDataInstance(DataInstance dataInstance) {
        this.data.add(dataInstance);
    }

    public List<DataInstance> getData() {
        return this.data;
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
