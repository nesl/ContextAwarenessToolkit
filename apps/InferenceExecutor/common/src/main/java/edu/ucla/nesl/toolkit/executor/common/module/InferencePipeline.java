package edu.ucla.nesl.toolkit.executor.common.module;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cgshen on 11/13/16.
 */

public class InferencePipeline {

    private List<ModuleBase> modules;
    private List<String> dataColumns;

    public InferencePipeline() {
        this.modules = new ArrayList<>();
        this.dataColumns = new ArrayList<>();
    }

    public InferencePipeline(List<ModuleBase> modules, List<String> dataColumns) {
        this.modules = modules;
        this.dataColumns = dataColumns;
    }

    public void addModule(ModuleBase module) {
        this.modules.add(module);
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
}
