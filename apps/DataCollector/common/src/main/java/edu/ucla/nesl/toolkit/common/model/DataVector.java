package edu.ucla.nesl.toolkit.common.model;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by cgshen on 10/6/16.
 */

public class DataVector implements Serializable {
    // TODO: deal with possible inconsistency between timestamps in DataVector and DataInstance
    private long timestamp;
    private Map<DataType, List<DataInstance>> data;

    public DataVector() {
        this.data = new HashMap<>();
    }

    public DataVector(long timestamp) {
        this.timestamp = timestamp;
        this.data = new HashMap<>();
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void addDataType(DeviceType deviceType, int sensorType) {
        DataType dataType = new DataType(deviceType, sensorType);
        if (!data.containsKey(dataType)) {
            data.put(dataType, new ArrayList<DataInstance>());
        }
    }

    public void removeDataType(DeviceType deviceType, int sensorType) {
        DataType dataType = new DataType(deviceType, sensorType);
        if (data.containsKey(dataType)) {
            data.remove(dataType);
        }
    }

    public void addDataInstance(DeviceType deviceType, int sensorType, DataInstance dataInstance) {
        // TODO: this will create a lot of garbage, needs better indexing
        DataType dataType = new DataType(deviceType, sensorType);
        if (data.containsKey(dataType)) {
            data.get(dataType).add(dataInstance);
        }
    }

    public List<DataInstance> getDataInstance(DeviceType deviceType, int sensorType) {
        DataType dataType = new DataType(deviceType, sensorType);
        if (data.containsKey(dataType)) {
            return data.get(dataType);
        }
        return null;
    }

    public Map<DataType, List<DataInstance>> getData() {
        return this.data;
    }

    public void dump(String filename) throws IOException{
        new ObjectOutputStream(new FileOutputStream(filename)).writeObject(this);
    }
}
