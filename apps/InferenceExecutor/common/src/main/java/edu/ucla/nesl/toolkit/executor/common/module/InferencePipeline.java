package edu.ucla.nesl.toolkit.executor.common.module;

import android.hardware.Sensor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by cgshen on 11/13/16.
 */

public class InferencePipeline {

    private Set<Integer> sensors;
    private List<ModuleBase> modules;
    private List<String> dataColumns;
    private int maxWindowSize = 0;

    public InferencePipeline() {
        this.modules = new ArrayList<>();
        this.dataColumns = new ArrayList<>();
        this.sensors = new HashSet<>();
    }

    public InferencePipeline(
            Set<Integer> sensors,
            List<ModuleBase> modules,
            List<String> dataColumns) {
        this.sensors = sensors;
        this.modules = modules;
        this.dataColumns = dataColumns;
    }

    public void addModule(ModuleBase module) {
        this.modules.add(module);

        // Update the max buffer size
        if (module instanceof Preprocess)
            this.maxWindowSize = Math.max(
                    this.maxWindowSize,
                    ((Preprocess) module).getWindowSize());
        if (module instanceof Feature)
            this.maxWindowSize = Math.max(
                    this.maxWindowSize,
                    ((Feature) module).getWindowSize());
    }

    public void addDataColumn(String name) {
        this.dataColumns.add(name);
    }

    public List<ModuleBase> getModules() {
        return modules;
    }

    public void setModules(List<ModuleBase> modules) {
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

    public int getMaxWindowSize() {
        return maxWindowSize;
    }

    public void setMaxWindowSize(int maxWindowSize) {
        this.maxWindowSize = maxWindowSize;
    }
}
