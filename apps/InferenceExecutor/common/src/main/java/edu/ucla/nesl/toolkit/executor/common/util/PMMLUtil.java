package edu.ucla.nesl.toolkit.executor.common.util;

/**
 * Created by cgshen on 11/10/16.
 * Based on the JPMML-Android project:
 * https://github.com/jpmml/jpmml-android
 */

import android.content.Context;

import java.io.IOException;
import java.io.InputStream;

import org.dmg.pmml.PMML;
import org.jpmml.evaluator.Evaluator;
import org.jpmml.evaluator.ModelEvaluator;
import org.jpmml.evaluator.ModelEvaluatorFactory;
import org.jpmml.model.SerializationUtil;

public class PMMLUtil {

    public static PMML createPMML(Context context, String filename)
            throws ClassNotFoundException, IOException {
        filename = filename.substring(filename.lastIndexOf('/') + 1) + ".ser";
        InputStream is = context.getAssets().open(filename);
        return SerializationUtil.deserializePMML(is);
    }

    public static Evaluator createEvaluator(PMML pmml) throws Exception {
        ModelEvaluatorFactory modelEvaluatorFactory = ModelEvaluatorFactory.newInstance();
        ModelEvaluator<?> modelEvaluator = modelEvaluatorFactory.newModelEvaluator(pmml);
        modelEvaluator.verify();
        return modelEvaluator;
    }

}
