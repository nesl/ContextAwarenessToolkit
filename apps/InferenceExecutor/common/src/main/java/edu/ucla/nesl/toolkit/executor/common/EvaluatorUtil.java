package edu.ucla.nesl.toolkit.executor.common;

/**
 * Created by cgshen on 11/10/16.
 * Based on the JPMML-Android project:
 * https://github.com/jpmml/jpmml-android
 */

import java.io.InputStream;

import org.dmg.pmml.PMML;
import org.jpmml.evaluator.Evaluator;
import org.jpmml.evaluator.ModelEvaluator;
import org.jpmml.evaluator.ModelEvaluatorFactory;
import org.jpmml.model.SerializationUtil;

public class EvaluatorUtil {

    public Evaluator createEvaluator(InputStream is) throws Exception {
        PMML pmml = SerializationUtil.deserializePMML(is);

        ModelEvaluatorFactory modelEvaluatorFactory = ModelEvaluatorFactory.newInstance();
        ModelEvaluator<?> modelEvaluator = modelEvaluatorFactory.newModelEvaluator(pmml);
        modelEvaluator.verify();

        return modelEvaluator;
    }

}
