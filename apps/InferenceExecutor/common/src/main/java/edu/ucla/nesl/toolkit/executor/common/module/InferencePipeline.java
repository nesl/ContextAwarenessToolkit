package edu.ucla.nesl.toolkit.executor.common.module;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by cgshen on 11/13/16.
 */

public class InferencePipeline {
    private static final String TAG = "InfPipeline";

    private Set<Integer> sensors;
    private List<String> dataColumns;

    private int windowSize;

    private Map<String, ModuleBase> modules;

    public InferencePipeline() {
        this.modules = new HashMap<>();
        this.dataColumns = new ArrayList<>();
        this.sensors = new HashSet<>();
    }

    public InferencePipeline(
            Set<Integer> sensors,
            Map<String, ModuleBase> modules,
            List<String> dataColumns) {
        this.sensors = sensors;
        this.modules = modules;
        this.dataColumns = dataColumns;
    }

    public void addModule(ModuleBase module) {
        // Save module and update the max buffer size
        if (module instanceof Preprocess) {
            this.modules.put(StringConstant.MOD_PREPROCESS, module);
            this.windowSize = Math.min(
                    this.windowSize,
                    ((Preprocess) module).getWindowSize());
        }
        else if (module instanceof Feature) {
            this.modules.put(StringConstant.MOD_FEATURE, module);
            this.windowSize = Math.min(
                    this.windowSize,
                    ((Feature) module).getWindowSize());
        }
        else if (module instanceof Classifier) {
            this.modules.put(StringConstant.MOD_CLASSIFIER, module);
        }
        else {
            Log.e(TAG, "Error: unsupported module type");
        }
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
