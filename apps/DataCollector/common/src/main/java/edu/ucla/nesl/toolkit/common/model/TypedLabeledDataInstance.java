package edu.ucla.nesl.toolkit.common.model;

/**
 * Created by cgshen on 10/7/16.
 */

public class TypedLabeledDataInstance extends TypedDataInstance{
    private DataLabel label;

    public TypedLabeledDataInstance(
            long timestamp,
            double[] values,
            DeviceType deviceType,
            SensorType sensorType,
            DataLabel label) {
        super(timestamp, values, deviceType, sensorType);
        this.label = label;
    }

    public DataLabel getLabel() {
        return label;
    }

    public void setLabel(DataLabel label) {
        this.label = label;
    }
}
