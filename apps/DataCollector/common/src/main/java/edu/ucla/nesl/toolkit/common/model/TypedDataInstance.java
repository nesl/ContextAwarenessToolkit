package edu.ucla.nesl.toolkit.common.model;

/**
 * Created by cgshen on 10/12/16.
 */

public class TypedDataInstance extends DataInstance {
    public DataType dataType;

    public TypedDataInstance(DataType dataType) {
        this.dataType = dataType;
    }

    public TypedDataInstance(long timestamp, float[] values, DataType dataType) {
        super(timestamp, values);
        this.dataType = dataType;
    }

    public DataType getDataType() {
        return dataType;
    }

    public void setDataType(DataType dataType) {
        this.dataType = dataType;
    }
}
