package edu.ucla.nesl.toolkit.executor.common.module;

import android.util.Log;

import org.dmg.pmml.FieldName;
import org.dmg.pmml.PMML;
import org.jpmml.evaluator.Evaluator;
import org.jpmml.evaluator.FieldValue;
import org.jpmml.evaluator.InputField;
import org.jpmml.evaluator.TargetField;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import edu.ucla.nesl.toolkit.common.model.DataInstance;
import edu.ucla.nesl.toolkit.executor.common.util.PMMLUtil;

/**
 * Created by cgshen on 11/12/16.
 */

public class Classifier implements ModuleBase {
    private static final String TAG = "Classifier";

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
        if (data.size() != 1) {
            Log.e(TAG, "Error: classifier only accepts 1-d feature vector.");
            return null;
        }

        try {
            // Get a model evaluator from PMML
            Evaluator evaluator = PMMLUtil.createEvaluator(classifier);

            // Map features to input fields
            Map<FieldName, FieldValue> arguments = new LinkedHashMap<>();
            List<InputField> inputFields = evaluator.getInputFields();
            if (inputFields.size() != data.get(0).getValues().length) {
                Log.e(TAG, "Error: input feature dimension mismatch with classifier.");
                return null;
            }
            int featureCount = 0;
            for(InputField inputField : inputFields){
                FieldName inputFieldName = inputField.getName();
                FieldValue inputFieldValue = inputField.prepare(
                        data.get(0).getValues()[featureCount++]);
                arguments.put(inputFieldName, inputFieldValue);
            }

            // Evaluate the PMML model
            Map<FieldName, ?> results = evaluator.evaluate(arguments);

            // Get result from target
            List<DataInstance> result = new ArrayList<>();
            List<TargetField> targetFields = evaluator.getTargetFields();
            for(TargetField targetField : targetFields){
                FieldName targetFieldName = targetField.getName();
                float[] targetValue = {Integer.parseInt(results.get(targetFieldName).toString())};
                result.add(new DataInstance(0, targetValue));
            }
            return result;
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    public String getLabel(List<DataInstance> data) {
        String label = "";
        for (DataInstance di : process(data)) {
            label += di.getValues()[0] + " ";
        }
        return label;
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
