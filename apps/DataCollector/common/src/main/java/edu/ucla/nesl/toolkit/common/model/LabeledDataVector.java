package edu.ucla.nesl.toolkit.common.model;

/**
 * Created by cgshen on 10/11/16.
 */

public class LabeledDataVector extends DataVector {
    private DataLabel label;

    public LabeledDataVector() {

    }

    public LabeledDataVector(LabelType labelType) {
        this.label = new DataLabel(labelType);
    }

    public LabeledDataVector(DataLabel label) {
        this.label = label;
    }

    public LabeledDataVector(long timestamp, DataLabel label) {
        super(timestamp);
        this.label = label;
    }

    public DataLabel getLabel() {
        return label;
    }

    public void setLabel(DataLabel label) {
        this.label = label;
    }
}
