package edu.ucla.nesl.toolkit.executor.common.module;

/**
 * Created by cgshen on 11/12/16.
 * Base class for modules in an inference pipeline
 */

public interface ModuleBase {
    // Load from a json string
    void load(String jsonFile);

    // Process a data vector
    float process(float[] data);
}
