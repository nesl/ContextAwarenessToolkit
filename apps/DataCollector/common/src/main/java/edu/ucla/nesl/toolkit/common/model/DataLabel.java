package edu.ucla.nesl.toolkit.common.model;

import java.io.Serializable;

/**
 * Created by cgshen on 10/6/16.
 */

public class DataLabel  implements Serializable {
    private LabelType labelType;
    private int intValue;
    private String nominalValue;
    private double realValue;
    private TypedDataInstance sensorValue;

    public DataLabel(LabelType labelType) {
        this.labelType = labelType;
    }

    public DataLabel(LabelType labelType, Object value) {
        this.labelType = labelType;
        setLabelValue(value);
    }

    public void setLabelValue(Object value) {
        switch (this.labelType) {
            case INTEGER:
                this.intValue = (Integer) value;
                break;
            case NOMINAL:
                this.nominalValue = (String) value;
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
        switch (this.labelType) {
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
}
