package edu.ucla.nesl.toolkit.executor.common.module;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cgshen on 11/12/16.
 */

public class Preprocess implements ModuleBase {
    private static final String TAG = "Module: Preprocess";

    private int windowSize;
    private List<String> operators;

    public Preprocess() {

    }

    public Preprocess(int windowSize, List<String> operators) {
        this.windowSize = windowSize;
        this.operators = operators;
    }

    @Override
    public String getModuleType() {
        return StringConstant.MOD_PREPROCESS;
    }

    @Override
    public float[] process(float[] data) {
        return null;
    }

    public int getWindowSize() {
        return windowSize;
    }

    public void setWindowSize(int windowSize) {
        this.windowSize = windowSize;
    }

    public List<String> getOperators() {
        return operators;
    }

    public void setOperators(List<String> operators) {
        this.operators = operators;
    }

    public static void main(String[] args) {
        System.out.println(TAG);
    }

}
