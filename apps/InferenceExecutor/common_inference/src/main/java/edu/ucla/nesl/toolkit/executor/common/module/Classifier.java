package edu.ucla.nesl.toolkit.executor.common.module;

import android.util.Log;

import org.dmg.pmml.FieldName;
import org.dmg.pmml.PMML;
import org.jpmml.evaluator.Computable;
import org.jpmml.evaluator.Evaluator;
import org.jpmml.evaluator.FieldValue;
import org.jpmml.evaluator.InputField;
import org.jpmml.evaluator.TargetField;
import org.jpmml.evaluator.VoteDistribution;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.ucla.nesl.toolkit.common.model.DataInstance;
import edu.ucla.nesl.toolkit.executor.common.util.PMMLUtil;

/**
 * Created by cgshen on 11/12/16.
 */

public class Classifier extends ModuleBase {
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
            Map<FieldName, ?> evaluateResult = evaluator.evaluate(arguments);

            // Get result from target
            List<DataInstance> labelResult = new ArrayList<>();
            List<TargetField> targetFields = evaluator.getTargetFields();
            for(TargetField targetField : targetFields){
                FieldName targetFieldName = targetField.getName();
                Object targetFieldValue = evaluateResult.get(targetFieldName);

                // Find the label with the highest vote
                if (targetFieldValue instanceof VoteDistribution) {
                    VoteDistribution voteDistribution = (VoteDistribution) targetFieldValue;
                    Set<String> labelDist = voteDistribution.getCategoryValues();
                    int bestLabel = 0;
                    double bestProb = 0;
                    for (String str : labelDist) {
                        if (voteDistribution.getProbability(str) > bestProb) {
                            bestLabel = Integer.parseInt(str);
                            bestProb = Double.valueOf(voteDistribution.getProbability(str));
                        }
                    }
                    float[] targetValue = {bestLabel};
                    labelResult.add(new DataInstance(0, targetValue));
                }
                else if(targetFieldValue instanceof Computable){
                    Computable computable = (Computable) targetFieldValue;
                    float[] targetValue = {Integer.parseInt(computable.getResult().toString())};
                    labelResult.add(new DataInstance(0, targetValue));
                }
            }
            return labelResult;
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public String getLabel(List<DataInstance> data) {
        String label = "";
        List<DataInstance> processed_data = process(data);
        if (processed_data != null) {
            for (DataInstance di : processed_data) {
                label += di.getValues()[0] + " ";
            }

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
