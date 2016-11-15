package edu.ucla.nesl.toolkit.executor.common.module;

import org.dmg.pmml.PMML;

import java.util.List;

import edu.ucla.nesl.toolkit.common.model.DataInstance;

/**
 * Created by cgshen on 11/12/16.
 */

public class Classifier implements ModuleBase {
    private String name;
    private PMML classifier;

    public Classifier() {

    }

    public Classifier(PMML classifier, String name) {
        this.classifier = classifier;
        this.name = name;
    }

    @Override
    public List<DataInstance> process(List<DataInstance> data) {
        return null;
    }

    @Override
    public String getModuleType() {
        return StringConstant.MOD_CLASSIFIER;
    }

    public PMML getClassifier() {
        return classifier;
    }

    public void setClassifier(PMML classifier) {
        this.classifier = classifier;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
