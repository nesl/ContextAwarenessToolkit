package edu.ucla.nesl.toolkit.common.model.type;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by cgshen on 10/17/16.
 */

public class LabelType implements Serializable {
    private String name;

    // Type of the label
    private LabelDataType labelDataType;

    // Candidate nominal values
    private Set<String> candidateNominalValueSet;

    // Interval of collecting ground truth label (in seconds)
    private int interval;

    // Duration of label to collect (in seconds), when choosing the sensor type
    private int duration;


    public LabelType(String name, LabelDataType labelDataType) {
        this.name = name;
        this.labelDataType = labelDataType;
        this.candidateNominalValueSet = new HashSet<>();
        this.interval = -1;
        this.duration = -1;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LabelDataType getLabelDataType() {
        return labelDataType;
    }

    public void setLabelDataType(LabelDataType labelDataType) {
        this.labelDataType = labelDataType;
    }

    public Set<String> getCandidateNominalValueSet() {
        return candidateNominalValueSet;
    }

    public void setCandidateNominalValueSet(Set<String> candidateNominalValueSet) {
        this.candidateNominalValueSet = candidateNominalValueSet;
    }

    public void addCandidateNominalValue(String str) {
        this.candidateNominalValueSet.add(str);
    }

    public int getInterval() {
        return interval;
    }

    public void setInterval(int interval) {
        // s -> ms conversion
        this.interval = interval * 1000;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }
}
