package edu.ucla.nesl.toolkit.executor.common.module;

import java.io.Serializable;
import java.util.List;

import edu.ucla.nesl.toolkit.common.model.DataInstance;

/**
 * Created by cgshen on 11/12/16.
 * Base class for modules in an inference pipeline
 */

public abstract class ModuleBase implements Serializable {
    public abstract String getModuleType();

    // Process a data vector
    public abstract List<DataInstance> process(List<DataInstance> data);
}
