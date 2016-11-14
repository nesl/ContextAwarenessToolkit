package edu.ucla.nesl.toolkit.executor.common.module;

import java.util.List;

/**
 * Created by cgshen on 11/12/16.
 */

public class Feature implements ModuleBase {
    private int windowSize;
    private List<String> features;

    public Feature() {

    }

    public Feature(List<String> features, int windowSize) {
        this.features = features;
        this.windowSize = windowSize;
    }

    @Override
    public float[] process(float[] data) {
        return null;
    }

    @Override
    public String getModuleType() {
        return StringConstant.MOD_FEATURE;
    }

    public int getWindowSize() {
        return windowSize;
    }

    public void setWindowSize(int windowSize) {
        this.windowSize = windowSize;
    }

    public List<String> getFeatures() {
        return features;
    }

    public void setFeatures(List<String> features) {
        this.features = features;
    }

}
