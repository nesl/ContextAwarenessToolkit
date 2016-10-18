package edu.ucla.nesl.toolkit.common.model;

import java.io.Serializable;

import edu.ucla.nesl.toolkit.common.model.type.LabelType;

/**
 * Created by cgshen on 10/6/16.
 */

public class DataLabel implements Serializable {
    private LabelType labelType;

    private long timestamp;

    // Value of a nominal label
    private String nominalValue;

    // Value of an int label
    private int intValue;

    // Value of a real (continuous) label
    private double realValue;

    // Value of a sensor label
    private TypedDataInstance sensorValue;

    public DataLabel(long timestamp, LabelType labelType) {
        this.timestamp = timestamp;
        this.labelType = labelType;
    }

    public DataLabel(long timestamp, LabelType labelType, Object value)
            throws InvalidNominalLabelStringException {
        this.timestamp = timestamp;
        this.labelType = labelType;
        setLabelValue(value);
    }

    public void setLabelValue(Object value) throws InvalidNominalLabelStringException {
        switch (this.labelType.getLabelDataType()) {
            case INTEGER:
                this.intValue = (Integer) value;
                break;
            case NOMINAL:
                this.nominalValue = (String) value;
                if (!this.labelType.getCandidateNominalValueSet().contains(this.nominalValue)) {
                    throw new InvalidNominalLabelStringException("Invalid nominal value.");
                }
                break;
            case REAL:
                this.realValue = (Double) value;
                break;
            case SENSOR:
                this.sensorValue = (TypedDataInstance) value;
                break;
        }
    }

    public Object getLabelValue() {
        switch (this.labelType.getLabelDataType()) {
            case INTEGER:
                return this.intValue;
            case NOMINAL:
                return this.nominalValue;
            case REAL:
                return this.realValue;
            case SENSOR:
                return this.sensorValue;
            default:
                return null;
        }
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public LabelType getLabelType() {
        return labelType;
    }

    public void setLabelType(LabelType labelType) {
        this.labelType = labelType;
    }

    public int getIntValue() {
        return intValue;
    }

    public void setIntValue(int intValue) {
        this.intValue = intValue;
    }

    public String getNominalValue() {
        return nominalValue;
    }

    public void setNominalValue(String nominalValue) {
        this.nominalValue = nominalValue;
    }

    public double getRealValue() {
        return realValue;
    }

    public void setRealValue(double realValue) {
        this.realValue = realValue;
    }

    public TypedDataInstance getSensorValue() {
        return sensorValue;
    }

    public void setSensorValue(TypedDataInstance sensorValue) {
        this.sensorValue = sensorValue;
    }
}
