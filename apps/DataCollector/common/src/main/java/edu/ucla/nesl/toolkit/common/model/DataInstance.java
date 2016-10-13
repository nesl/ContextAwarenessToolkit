package edu.ucla.nesl.toolkit.common.model;

import android.hardware.Sensor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by cgshen on 10/6/16.
 */

public class DataInstance implements Serializable {
    private long timestamp;
    private float[] values;

    public DataInstance() {

    }

    public DataInstance(long timestamp, float[] values) {
        this.timestamp = timestamp;
        this.values = values.clone();
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public float[] getValues() {
        return values;
    }

    public void setValues(float[] values) {
        this.values = values.clone();
    }
}
