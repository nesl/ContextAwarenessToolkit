package edu.ucla.nesl.toolkit.common;

import edu.ucla.nesl.toolkit.common.model.DataLabel;
import edu.ucla.nesl.toolkit.common.model.DataVector;
import edu.ucla.nesl.toolkit.common.model.type.DeviceType;
import edu.ucla.nesl.toolkit.common.model.InvalidDataVectorTypeException;
import edu.ucla.nesl.toolkit.common.model.InvalidSensorTypeException;
import edu.ucla.nesl.toolkit.common.model.type.LabelDataType;
import edu.ucla.nesl.toolkit.common.model.LabeledDataVector;
import edu.ucla.nesl.toolkit.common.model.type.LabelType;

/**
 * Created by cgshen on 10/6/16.
 */

public class DataCollectionConfigurator {
    DataVector dataVector;

    public DataCollectionConfigurator(boolean labeled) {
        this.dataVector = labeled ? new LabeledDataVector() : new DataVector();
    }

    public DataCollectionConfigurator(DataVector dataVector) {
        this.dataVector = dataVector;
    }

    public DataVector getDataVector() {
        return dataVector;
    }

    public void setDataVector(DataVector dataVector) {
        this.dataVector = dataVector;
    }

    public void addSensorTypeToVector(DeviceType deviceType, int sensorType)
            throws InvalidSensorTypeException {
        this.dataVector.addDataType(deviceType, sensorType);
    }

    public void addLabelType(LabelType labelType) throws InvalidDataVectorTypeException {
        if (this.dataVector instanceof LabeledDataVector) {
            ((LabeledDataVector) this.dataVector).addLabelType(labelType);
        }
        else {
            throw new InvalidDataVectorTypeException(
                    "DataCollectionConfigurator: The current DataVector is not labeled.");
        }
    }
}
