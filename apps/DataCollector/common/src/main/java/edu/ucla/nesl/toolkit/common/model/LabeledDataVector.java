package edu.ucla.nesl.toolkit.common.model;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.ucla.nesl.toolkit.common.model.type.DataType;
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

    @Override
    public void dumpAsCSV(String filename) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
        for (DataType dt : data.keySet()) {
            for (DataInstance di : data.get(dt)) {
                StringBuilder sb = new StringBuilder();
                sb.append(di.getTimestamp());
                sb.append(",");
                sb.append(dt.getDeviceType());
                sb.append(",");
                sb.append(dt.getSensorType());
                sb.append(",");
                for (float v : di.getValues()) {
                    sb.append(v);
                    sb.append(",");
                }
                sb.deleteCharAt(sb.length() - 1);
                writer.append(sb.toString());
                writer.newLine();
            }
        }
        for (LabelType lt : labels.keySet()) {
            for (DataLabel dl : labels.get(lt)) {
                StringBuilder sb = new StringBuilder();
                sb.append(dl.getTimestamp());
                sb.append(",");
                sb.append(dl.getIntValue());
                sb.append(",");
                sb.append(dl.getRealValue());
                sb.append(",");
                sb.append(dl.getNominalValue());
                sb.append(",");
                if (dl.getSensorValue() != null) {
                    sb.append(dl.getSensorValue().getTimestamp());
                    sb.append(",");
                    sb.append(dl.getSensorValue().getDataType().getDeviceType());
                    sb.append(",");
                    sb.append(dl.getSensorValue().getDataType().getSensorType());
                    sb.append(",");
                    for (float v : dl.getSensorValue().getValues()) {
                        sb.append(v);
                        sb.append(",");
                    }
                    sb.deleteCharAt(sb.length() - 1);
                }
                writer.append(sb.toString());
                writer.newLine();
            }
        }
        writer.flush();
        writer.close();
    }


    public Map<LabelType, List<DataLabel>> getLabels() {
        return labels;
    }

    public void setLabels(Map<LabelType, List<DataLabel>> labels) {
        this.labels = labels;
    }
}
