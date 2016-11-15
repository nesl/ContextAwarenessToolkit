package edu.ucla.nesl.toolkit.common.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.ucla.nesl.toolkit.common.model.type.LabelType;

/**
 * Created by cgshen on 10/11/16.
 */

public class LabeledDataVector extends DataVector {
    private Map<LabelType, List<DataLabel>> labels;

    public LabeledDataVector() {
        this.labels = new HashMap<>();
    }

    public void addLabelType(LabelType labelType) {
        if (!labels.containsKey(labelType)) {
            labels.put(labelType, new ArrayList<DataLabel>());
        }
    }

    public void removeLabelType(LabelType labelType) {
        if (labels.containsKey(labelType)) {
            labels.remove(labelType);
        }
    }

    public void addLabel(DataLabel dataLabel) {
        LabelType currentLabelType = dataLabel.getLabelType();
        if (labels.containsKey(currentLabelType)) {
            labels.get(currentLabelType).add(dataLabel);
        }
    }

    public List<DataLabel> getLabels(LabelType labelType) {
        if (labels.containsKey(labelType)) {
            return labels.get(labelType);
        }
        return null;
    }

    public Map<LabelType, List<DataLabel>> getLabels() {
        return labels;
    }

    public void setLabels(Map<LabelType, List<DataLabel>> labels) {
        this.labels = labels;
    }
}
