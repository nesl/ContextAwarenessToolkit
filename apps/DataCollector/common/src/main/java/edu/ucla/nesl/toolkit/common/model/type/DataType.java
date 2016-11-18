package edu.ucla.nesl.toolkit.common.model.type;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by cgshen on 10/12/16.
 */

public class DataType implements Serializable {
    private static Map<DeviceType, Integer> deviceSensorMap = new HashMap<>();
    private static Map<Integer, DataType> sensorObjectMap = new HashMap<>();

    /**
     * Make sure each DataType is a singleton
     * @param deviceType
     * @param sensorType
     * @return
     */
    public static DataType getInstance(DeviceType deviceType, int sensorType) {
        if (deviceSensorMap.containsKey(deviceType)) {
            if (sensorObjectMap.containsKey(deviceSensorMap.get(deviceType))) {
                return sensorObjectMap.get(deviceSensorMap.get(deviceType));
            }
        }
        DataType dt = new DataType(deviceType, sensorType);
        if (!deviceSensorMap.containsKey(deviceType)) {
            deviceSensorMap.put(deviceType, sensorType);
        }
        sensorObjectMap.put(sensorType, dt);
        return dt;
    }

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
