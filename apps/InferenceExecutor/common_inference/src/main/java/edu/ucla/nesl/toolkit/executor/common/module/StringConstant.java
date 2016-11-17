package edu.ucla.nesl.toolkit.executor.common.module;

/**
 * Created by cgshen on 11/13/16.
 */

public class StringConstant {

    // Module names
    public static final String MOD_PREPROCESS = "mod_preprocess";
    public static final String MOD_FEATURE = "mod_feature";
    public static final String MOD_CLASSIFIER = "mod_classifier";

    // Sensor data
    public static final String ACCX = "accx";
    public static final String ACCY = "accy";
    public static final String ACCZ = "accz";
    public static final String[] ACC = {ACCX, ACCY, ACCZ};
    public static final String GYROX = "gyrox";
    public static final String GYROY = "gyroy";
    public static final String GYROZ = "gyroz";
    public static final String[] GYRO = {GYROX, GYROY, GYROZ};
    public static final String GRAVX = "gravx";
    public static final String GRAVY = "gravy";
    public static final String GRAVZ = "gravz";
    public static final String[] GRAV = {GRAVX, GRAVY, GRAVZ};

    // Pre-processing
    public static final String DATA_COLUMN = "data_column";
    public static final String WINDOW_SIZE = "window_size";
    public static final String OPERATOR = "operator";
    public static final String MOVING_AVG_SMOOTH = "moving_avg_smooth";

    // Feature calculation
    public static final String FEATURE = "feature";
    public static final String TIMESTAMP = "time";
    public static final String LABEL = "label";
    public static final String MEAN = "mean";
    public static final String STD = "std";
    public static final String VAR = "var";
    public static final String MAD = "mad";
    public static final String RMS = "rms";
    public static final String RANGE = "range";
    public static final String SKEW = "skew";
    public static final String KURT = "kurt";
    public static final String MAG = "mag";
    public static final String QUA = "qua";
    public static final String FFT = "fft";

    // Classifiers
    public static final String CLASSIFIER = "classifier";
    public static final String CLASSIFIER_NAME = "classifier_name";
    public static final String CLASSIFIER_PMML = "classifier_pmml";

}
