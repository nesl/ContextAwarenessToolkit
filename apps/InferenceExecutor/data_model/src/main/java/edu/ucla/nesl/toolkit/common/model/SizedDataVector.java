package edu.ucla.nesl.toolkit.common.model;

import java.util.LinkedList;
import java.util.List;

import edu.ucla.nesl.toolkit.common.model.type.DataType;
import edu.ucla.nesl.toolkit.common.model.type.DeviceType;

/**
 * Created by cgshen on 11/15/16.
 */

public class SizedDataVector extends DataVector {
    private int sizeLimit;

    public SizedDataVector(int sizeLimit) {
        super();
        this.sizeLimit = sizeLimit;
    }

    @Override
    public void addDataType(DeviceType deviceType, int sensorType) {
        DataType dataType = new DataType(deviceType, sensorType);
        if (!data.containsKey(dataType)) {
            data.put(dataType, new LinkedList<DataInstance>());
        }
    }

    @Override
    public void addDataInstance(DeviceType deviceType, int sensorType, DataInstance dataInstance) {
        // TODO: this will create a lot of garbage, needs better indexing
        DataType dataType = new DataType(deviceType, sensorType);
        if (data.containsKey(dataType)) {
            List<DataInstance> list = data.get(dataType);
            list.add(dataInstance);

            // Enforce the sizeLimit limit
            if (list.size() > this.sizeLimit) {
                list.remove(0);
            }
        }
    }

    public int getSizeLimit() {
        return sizeLimit;
    }

    public void setSizeLimit(int sizeLimit) {
        this.sizeLimit = sizeLimit;
    }
}
