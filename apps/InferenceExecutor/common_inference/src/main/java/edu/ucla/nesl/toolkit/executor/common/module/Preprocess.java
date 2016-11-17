package edu.ucla.nesl.toolkit.executor.common.module;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import edu.ucla.nesl.toolkit.common.model.DataInstance;

/**
 * Created by cgshen on 11/12/16.
 */

public class Preprocess implements ModuleBase {
    private static final String TAG = "Module: Preprocess";
    private static final float SMOOTH_FACTOR = 0.1f;

    private int windowSize;
    private List<String> operators;

    public Preprocess() {

    }

    public Preprocess(int windowSize, List<String> operators) {
        this.windowSize = windowSize;
        this.operators = operators;
    }

    private List<DataInstance> movingAverageFilter(List<DataInstance> data) {
        int numOfAxis = data.get(0).getValues().length;
        float mean[] = new float[numOfAxis];

        // Get the mean for each axis
        for (int j = 0; j < data.size(); j++) {
            float[] values = data.get(j).getValues();
            for (int i = 0; i < numOfAxis; i++) {
                mean[i] += values[i];
            }
        }
        for (int i = 0; i < numOfAxis; i++) {
            mean[i] /= data.size();
        }

        // Adjust mean from data and create result
        List<DataInstance> result = new ArrayList<>();
        for (int j = 0; j < data.size(); j++) {
            float[] values = data.get(j).getValues();
            float[] newValues = new float[numOfAxis];
            for (int i = 0; i < numOfAxis; i++) {
                if (values[i] > mean[i]) {
                    newValues[i] -= (values[i] - mean[i]) * SMOOTH_FACTOR;
                }
                else {
                    newValues[i] += (mean[i] - values[i]) * SMOOTH_FACTOR;
                }
            }
            result.add(new DataInstance(0, newValues));
        }
        return result;
    }

    @Override
    public List<DataInstance> process(List<DataInstance> data) {
        List<DataInstance> result = null;
        for (String operator : operators) {
            if (operator.equals(StringConstant.MOVING_AVG_SMOOTH)) {
                result = movingAverageFilter(data);
            }
            else {
                Log.e(TAG, "Error: undefined pre-processing function.");
            }
        }
        return result;
    }

    @Override
    public String getModuleType() {
        return StringConstant.MOD_PREPROCESS;
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
