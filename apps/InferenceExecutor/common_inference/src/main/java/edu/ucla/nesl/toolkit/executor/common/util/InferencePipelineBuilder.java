package edu.ucla.nesl.toolkit.executor.common.util;

import android.content.Context;
import android.hardware.Sensor;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.ucla.nesl.toolkit.executor.common.module.Classifier;
import edu.ucla.nesl.toolkit.executor.common.module.Feature;
import edu.ucla.nesl.toolkit.executor.common.module.InferencePipeline;
import edu.ucla.nesl.toolkit.executor.common.module.Preprocess;
import edu.ucla.nesl.toolkit.executor.common.module.StringConstant;

/**
 * Created by cgshen on 11/13/16.
 */

public class InferencePipelineBuilder {
    private static final String TAG = "InfPipelineBuilder";

    public static JSONObject readJSONFromAssets(Context context, String filename) {
        // Load json from a file in assets
        Log.i(TAG, "Building inference pipeline from JSON...");
        try {
            InputStream is = context.getAssets().open(filename);
            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            is.close();
            return new JSONObject(new String(buffer, "UTF-8"));
        }
        catch (IOException ex) {
            Log.e(TAG, "Error: inference pipeline not found in app's assets.");
            ex.printStackTrace();
        }
        catch (JSONException e) {
            Log.e(TAG, "Error: invalid json format.");
            e.printStackTrace();
        }
        Log.e(TAG, "Error: null JSON str.");
        return null;
    }

    public static Map<String, InferencePipeline> buildForMultipleDevice(
            Context context,
            JSONObject json) {
        Map<String, InferencePipeline> result = null;
        try {
            result = new HashMap<>();
            if (json.has(StringConstant.ANDROID_PHONE)) {
                result.put(StringConstant.ANDROID_PHONE, buildForSingleDevice(
                        context,
                        json.getJSONObject(StringConstant.ANDROID_PHONE)));
            }
            if (json.has(StringConstant.ANDROID_WEAR)) {
                result.put(StringConstant.ANDROID_WEAR, buildForSingleDevice(
                        context,
                        json.getJSONObject(StringConstant.ANDROID_WEAR)));
            }
        }
        catch (JSONException e) {
            Log.e(TAG, "Error: invalid json format.");
            e.printStackTrace();
        }
        return result;
    }

    public static InferencePipeline buildForSingleDevice(Context context, JSONObject json) {
        // Parse a json object from the string
        InferencePipeline inferencePipeline = null;
        try {
            inferencePipeline = new InferencePipeline();

            // Parse pre-processing
            if (json.has(StringConstant.MOD_PREPROCESS)) {
                JSONObject jsonPreprocess = json.getJSONObject(StringConstant.MOD_PREPROCESS);
                Preprocess preprocessor = new Preprocess();

                // Parse data columns and specify sensors
                if (jsonPreprocess.has(StringConstant.DATA_COLUMN)) {
                    JSONArray jsonColumns = jsonPreprocess.getJSONArray(StringConstant.DATA_COLUMN);
                    for (int i = 0; i < jsonColumns.length(); i++) {
                        String name = jsonColumns.getString(i);
                        if (Arrays.asList(StringConstant.ACC).contains(name)) {
                            inferencePipeline.addSensorType(Sensor.TYPE_ACCELEROMETER);
                        }
                        else if (Arrays.asList(StringConstant.GRAV).contains(name)) {
                            inferencePipeline.addSensorType(Sensor.TYPE_GRAVITY);
                        }
                        else if (Arrays.asList(StringConstant.GYRO).contains(name)) {
                            inferencePipeline.addSensorType(Sensor.TYPE_GYROSCOPE);
                        }
                        else {
                            Log.e(TAG, "Error: invalid sensor type: " + name);
                            throw new JSONException("error");
                        }
                    }
                }
                else {
                    Log.e(TAG, "Error: no data column specified.");
                    throw new JSONException("error");
                }

                // Parse window size
                if (jsonPreprocess.has(StringConstant.WINDOW_SIZE)) {
                    preprocessor.setWindowSize(jsonPreprocess.getInt(StringConstant.WINDOW_SIZE));
                }
                else {
                    Log.e(TAG, "Error:No window size specified for preprocess.");
                    throw new JSONException("error");
                }

                // Parse pre-processing operations
                if (jsonPreprocess.has(StringConstant.OPERATOR)) {
                    List<String> operators = new ArrayList<>();
                    JSONArray jsonOperators = jsonPreprocess.getJSONArray(StringConstant.OPERATOR);
                    for (int i = 0; i < jsonOperators.length(); i++) {
                        operators.add(jsonOperators.getString(i));
                    }
                    preprocessor.setOperators(operators);
                }
                else {
                    Log.e(TAG, "Error: No operator specified for preprocess.");
                    throw new JSONException("error");
                }

                // Add to the inference pipeline
                inferencePipeline.addModule(preprocessor);
            }
            else {
                Log.e(TAG, "Error: no preprocess specified.");
                throw new JSONException("error");
            }

            // Parse feature calculation
            if (json.has(StringConstant.MOD_FEATURE)) {
                JSONObject jsonFeature = json.getJSONObject(StringConstant.MOD_FEATURE);
                Feature featureCalculator = new Feature();

                // Parse window size
                if (jsonFeature.has(StringConstant.WINDOW_SIZE)) {
                    featureCalculator.setWindowSize(jsonFeature.getInt(StringConstant.WINDOW_SIZE));
                }
                else {
                    Log.e(TAG, "Error: No window size specified for feature.");
                    throw new JSONException("error");
                }

                // Parse feature function names
                if (jsonFeature.has(StringConstant.FEATURE)) {
                    List<String> features = new ArrayList<>();
                    JSONArray jsonFeatures = jsonFeature.getJSONArray(StringConstant.FEATURE);
                    for (int i = 0; i < jsonFeatures.length(); i++) {
                        features.add(jsonFeatures.getString(i));
                    }
                    featureCalculator.setFeatures(features);
                }
                else {
                    Log.e(TAG, "Error: No feature function specified for feature.");
                    throw new JSONException("error");
                }

                // Add to the inference pipeline
                inferencePipeline.addModule(featureCalculator);
            }
            else {
                Log.e(TAG, "Error: no feature specified.");
                throw new JSONException("error");
            }

            // Parse classifier
            if (json.has(StringConstant.MOD_CLASSIFIER)) {
                JSONObject jsonClassifier = json.getJSONObject(StringConstant.MOD_CLASSIFIER);
                if (jsonClassifier.has(StringConstant.CLASSIFIER_NAME) &&
                        jsonClassifier.has(StringConstant.CLASSIFIER_PMML)) {
                    Classifier classifier = new Classifier();
                    classifier.setName(jsonClassifier.getString(StringConstant.CLASSIFIER_NAME));
                    classifier.setClassifier(
                            PMMLUtil.createPMML(
                                    context,
                                    jsonClassifier.getString(StringConstant.CLASSIFIER_PMML)));
                    inferencePipeline.addModule(classifier);
                }
            }
            else {
                Log.e(TAG, "Error: No classifier specified.");
                throw new JSONException("error");
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error: invalid json format.");
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "Error: cannot parse PMML.");
            e.printStackTrace();
        } catch (IOException e) {
            Log.e(TAG, "Error: invalid PMML format.");
            e.printStackTrace();
        }

        return inferencePipeline;
    }
}
