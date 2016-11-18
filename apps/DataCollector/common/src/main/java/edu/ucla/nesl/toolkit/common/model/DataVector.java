package edu.ucla.nesl.toolkit.common.model;

import android.util.Log;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import edu.ucla.nesl.toolkit.common.model.type.DataType;
import edu.ucla.nesl.toolkit.common.model.type.DeviceType;

/**
 * Created by cgshen on 10/6/16.
 */

public class DataVector implements Serializable {
    private static final String TAG = "DataVector";

    // TODO: deal with possible inconsistency between timestamps in DataVector and DataInstance
    private long timestamp;
    protected Map<DataType, List<DataInstance>> data;

    public DataVector() {
        this.data = new HashMap<>();
    }

    public DataVector(long timestamp) {
        this.timestamp = timestamp;
        this.data = new HashMap<>();
    }

    public void addDataType(DeviceType deviceType, int sensorType) {
        DataType dataType = DataType.getInstance(deviceType, sensorType);
        if (!data.containsKey(dataType)) {
            data.put(dataType, new ArrayList<DataInstance>());
        }
    }

    public void removeDataType(DeviceType deviceType, int sensorType) {
        DataType dataType = DataType.getInstance(deviceType, sensorType);
        if (data.containsKey(dataType)) {
            data.remove(dataType);
        }
    }

    public void addDataInstance(DeviceType deviceType, int sensorType, DataInstance dataInstance) {
        DataType dataType = DataType.getInstance(deviceType, sensorType);
        if (data.containsKey(dataType)) {
            data.get(dataType).add(dataInstance);
        }
        else {
            Log.e(TAG, "Error: data type does not exist.");
        }
    }

    public List<DataInstance> getDataInstance(DeviceType deviceType, int sensorType) {
        DataType dataType = DataType.getInstance(deviceType, sensorType);
        if (data.containsKey(dataType)) {
            return data.get(dataType);
        }
        return null;
    }

    public List<DataInstance> getLatestDataInstance(
            DeviceType deviceType,
            int sensorType,
            int size) {
        DataType dataType = DataType.getInstance(deviceType, sensorType);
        if (data.containsKey(dataType)) {
            int length = data.get(dataType).size();
            return data.get(dataType).subList(length - size - 1, length - 1);
        }
        return null;
    }

    public int getDataLength(DeviceType deviceType, int sensorType) {
        DataType dataType = DataType.getInstance(deviceType, sensorType);
        if (data.containsKey(dataType)) {
            return data.get(dataType).size();
        }
        return 0;
    }

    public void clearData() {
        for (List list : data.values()) {
            list.clear();
        }
    }

    public Map<DataType, List<DataInstance>> getData() {
        return this.data;
    }

    public void dumpAsObject(String filename) throws IOException{
        new ObjectOutputStream(new FileOutputStream(filename)).writeObject(this);
    }

    public void dumpAsCSV(String filename) throws IOException{
        BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
        for (DataType dt : data.keySet()) {
            for (DataInstance di : data.get(dt)) {
                StringBuffer sb = new StringBuffer();
                sb.append(di.getTimestamp());
                sb.append(",");
                sb.append(dt.getDeviceType());
                sb.append(",");
                sb.append(dt.getSensorType());
                sb.append(",");
                for (float v : di.getValues()) {
                    sb.append(dt.getSensorType());
                    sb.append(",");
                }
                sb.deleteCharAt(sb.length() - 1);
                writer.append(sb.toString());
                writer.newLine();
            }
        }
        writer.flush();
        writer.close();
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

}
