package edu.ucla.nesl.toolkit.common.model;

import java.io.Serializable;

/**
 * Created by cgshen on 10/6/16.
 */

public class DataInstance implements Serializable {
    private long timestamp;
    private double[] values;

    public DataInstance(long timestamp, double[] values) {
        this.timestamp = timestamp;
        this.values = values.clone();
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public double[] getValues() {
        return values;
    }

    public void setValues(double[] values) {
        this.values = values.clone();
    }
}
