package edu.ucla.nesl.toolkit.executor.common.module;

import android.util.Log;

import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.util.FastMath;

import java.util.ArrayList;
import java.util.List;

import edu.ucla.nesl.toolkit.common.model.DataInstance;

/**
 * Created by cgshen on 11/12/16.
 */

public class Feature implements ModuleBase {
    private static final String TAG = "Feature";

    private int windowSize;
    private List<String> features;

    public Feature() {

    }

    public Feature(List<String> features, int windowSize) {
        this.features = features;
        this.windowSize = windowSize;
    }

    private float mean(List<double[]> data) {
        double[] axisMean = new double[data.size()];
        for (int i = 0; i < data.size(); i++) {
            axisMean[i] = StatUtils.mean(data.get(i));
        }
        return (float) StatUtils.mean(axisMean);
    }

    private float var(List<double[]> data) {
        double[] axisVar = new double[data.size()];
        for (int i = 0; i < data.size(); i++) {
            axisVar[i] = StatUtils.variance(data.get(i));
        }
        return (float) StatUtils.mean(axisVar);
    }

    private float std(List<double[]> data) {
        return (float) FastMath.sqrt(var(data));
    }

    private float mad(List<double[]> data) {
        double[] axisMad = new double[data.size()];
        for (int i = 0; i < data.size(); i++) {
            double axisMean = StatUtils.mean(data.get(i));
            axisMad[i] = 0;
            for (int j = 0; j < data.get(i).length; j++) {
                axisMad[i] += Math.abs(data.get(i)[j] - axisMean);
            }
        }
        return (float) StatUtils.mean(axisMad);
    }

    private float skew(List<double[]> data) {
        double[] axisSkew = new double[data.size()];
        for (int i = 0; i < data.size(); i++) {
            DescriptiveStatistics st = new DescriptiveStatistics(data.get(i));
            axisSkew[i] = st.getSkewness();
        }
        return (float) StatUtils.mean(axisSkew);
    }

    private float kurt(List<double[]> data) {
        double[] axisKurt = new double[data.size()];
        for (int i = 0; i < data.size(); i++) {
            DescriptiveStatistics st = new DescriptiveStatistics(data.get(i));
            axisKurt[i] = st.getKurtosis();
        }
        return (float) StatUtils.mean(axisKurt);
    }

    private float range(List<double[]> data) {
        double[] axisRange = new double[data.size()];
        for (int i = 0; i < data.size(); i++) {
            DescriptiveStatistics st = new DescriptiveStatistics(data.get(i));
            axisRange[i] = st.getMax() - st.getMin();
        }
        return (float) StatUtils.mean(axisRange);
    }

    private float rms(List<double[]> data) {
        double[] axisRMS = new double[data.size()];
        for (int i = 0; i < data.size(); i++) {
            DescriptiveStatistics st = new DescriptiveStatistics(data.get(i));
            axisRMS[i] = st.getQuadraticMean();
        }
        return (float) StatUtils.mean(axisRMS);
    }

    private float mag(List<double[]> data) {
        double[] axisMag = new double[data.size()];
        for (int i = 0; i < data.size(); i++) {
            DescriptiveStatistics st = new DescriptiveStatistics(data.get(i));
            axisMag[i] = st.getSumsq();
        }
        return (float) StatUtils.mean(axisMag);
    }

    private float qua(List<double[]> data, int p) {
        double[] axisQua = new double[data.size()];
        for (int i = 0; i < data.size(); i++) {
            DescriptiveStatistics st = new DescriptiveStatistics(data.get(i));
            axisQua[i] = st.getPercentile(p * 25);
        }
        return (float) StatUtils.mean(axisQua);
    }

    private float fft(List<double[]> data, int f) {
        double[] axisFft = new double[data.size()];
        for (int i = 0; i < data.size(); i++) {
            axisFft[i] = goertzel(data.get(i), f, data.get(i).length);
        }
        return (float) StatUtils.mean(axisFft);
    }

    private double goertzel(double [] data, double freq, double sr) {
        double s_prev = 0;
        double s_prev2 = 0;
        double coeff = 2 * Math.cos( (2*Math.PI*freq) / sr);
        double s;
        for (int i = 0; i < data.length; i++) {
            double sample = data[i];
            s = sample + coeff*s_prev  - s_prev2;
            s_prev2 = s_prev;
            s_prev = s;
        }
        return s_prev2*s_prev2 + s_prev*s_prev - coeff*s_prev2*s_prev;
    }

    @Override
    public List<DataInstance> process(List<DataInstance> data) {
        List<DataInstance> result = new ArrayList<>();
        float[] featureArray = new float[this.features.size()];

        // Prepare data
        int numOfAxis = data.get(0).getValues().length;
        List<double[]> dataAxis = new ArrayList<>();
        for (int i = 0; i < numOfAxis; i++) {
            dataAxis.add(new double[data.size()]);
        }
        for (int j = 0; j < data.size(); j++) {
            for (int i = 0; i < numOfAxis; i++) {
                dataAxis.get(i)[j] = data.get(j).getValues()[i];
            }
        }

        // Compute features
        for (int i = 0; i < features.size(); i++) {
            String featureName = features.get(i);
            if (featureName.equals(StringConstant.MEAN)) {
                featureArray[i] = mean(dataAxis);
            }
            else if (featureName.equals(StringConstant.STD)) {
                featureArray[i] = std(dataAxis);
            }
            else if (featureName.equals(StringConstant.VAR)) {
                featureArray[i] = var(dataAxis);
            }
            else if (featureName.equals(StringConstant.MAD)) {
                featureArray[i] = mad(dataAxis);
            }
            else if (featureName.equals(StringConstant.SKEW)) {
                featureArray[i] = skew(dataAxis);
            }
            else if (featureName.equals(StringConstant.KURT)) {
                featureArray[i] = kurt(dataAxis);
            }
            else if (featureName.equals(StringConstant.RANGE)) {
                featureArray[i] = range(dataAxis);
            }
            else if (featureName.equals(StringConstant.RMS)) {
                featureArray[i] = rms(dataAxis);
            }
            else if (featureName.equals(StringConstant.MAG)) {
                featureArray[i] = mag(dataAxis);
            }
            else if (featureName.startsWith(StringConstant.QUA)) {
                int percentile = Integer.parseInt(
                        featureName.substring(StringConstant.QUA.length()));
                featureArray[i] = qua(dataAxis, percentile);
            }
            else if (featureName.startsWith(StringConstant.FFT)) {
                int freq = Integer.parseInt(
                        featureName.substring(StringConstant.FFT.length()));
                featureArray[i] = fft(dataAxis, freq);
            }
            else {
                Log.e(TAG, "Error: undefined feature name.");
            }
        }

        // Return the result as a single DataInstance
        result.add(new DataInstance(0, featureArray));
        return result;
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
