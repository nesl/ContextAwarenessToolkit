package edu.ucla.nesl.toolkit.executor.common.module;

import java.util.List;

import edu.ucla.nesl.toolkit.common.model.DataInstance;

/**
 * Created by cgshen on 11/12/16.
 * Base class for modules in an inference pipeline
 */

public interface ModuleBase {
    String getModuleType();

    // Process a data vector
    List<DataInstance> process(List<DataInstance> data);

}
