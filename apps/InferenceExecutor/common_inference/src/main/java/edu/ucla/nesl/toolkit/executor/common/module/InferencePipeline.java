package edu.ucla.nesl.toolkit.executor.common.module;

import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by cgshen on 11/13/16.
 */

public class InferencePipeline implements Serializable {
    private static final String TAG = "InfPipeline";

    private Set<Integer> sensors;
    private List<String> dataColumns;

    private int windowSize;

    private Map<String, ModuleBase> modules;

    public InferencePipeline() {
        this.modules = new HashMap<>();
        this.dataColumns = new ArrayList<>();
        this.sensors = new HashSet<>();
        this.windowSize = Integer.MAX_VALUE;
    }

    public InferencePipeline(
            Set<Integer> sensors,
            Map<String, ModuleBase> modules,
            List<String> dataColumns) {
        this.sensors = sensors;
        this.modules = modules;
        this.dataColumns = dataColumns;
        this.windowSize = Integer.MAX_VALUE;
    }

    public void addModule(ModuleBase module) {
        // Save module and update the max buffer size
        if (module instanceof Preprocess) {
            this.modules.put(StringConstant.MOD_PREPROCESS, module);
            int newSize = ((Preprocess) module).getWindowSize();
            if (newSize > 0) {
                this.windowSize = Math.min(this.windowSize, newSize);
            }
        }
        else if (module instanceof Feature) {
            this.modules.put(StringConstant.MOD_FEATURE, module);
            int newSize = ((Feature) module).getWindowSize();
            if (newSize > 0) {
                this.windowSize = Math.min(this.windowSize, newSize);
            }

        }
        else if (module instanceof Classifier) {
            this.modules.put(StringConstant.MOD_CLASSIFIER, module);
        }
        else {
            Log.e(TAG, "Error: unsupported module type");
        }
    }

    public byte[] serialize() {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutput out = null;
        try {
            out = new ObjectOutputStream(bos);
            out.writeObject(this);
            out.flush();
            return bos.toByteArray();
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }
        finally {
            try {
                bos.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return null;
    }

    public static InferencePipeline deserialize(byte [] input) {
        ByteArrayInputStream bis = new ByteArrayInputStream(input);
        ObjectInput in = null;
        try {
            in = new ObjectInputStream(bis);
            return (InferencePipeline) in.readObject();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
               ex.printStackTrace();
            }
        }
        return null;
    }

    public void addDataColumn(String name) {
        this.dataColumns.add(name);
    }

    public Map<String, ModuleBase> getModules() {
        return modules;
    }

    public void setModules(Map<String, ModuleBase> modules) {
        this.modules = modules;
    }

    public List<String> getDataColumns() {
        return dataColumns;
    }

    public void setDataColumns(List<String> dataColumns) {
        this.dataColumns = dataColumns;
    }

    public Set<Integer> getSensors() {
        return sensors;
    }

    public void setSensors(Set<Integer> sensors) {
        this.sensors = sensors;
    }

    public void addSensorType(int sensorType) {
        if (!this.sensors.contains(sensorType)) {
            this.sensors.add(sensorType);
        }
    }

    public int getWindowSize() {
        return windowSize;
    }

    public void setWindowSize(int windowSize) {
        this.windowSize = windowSize;
    }
}
