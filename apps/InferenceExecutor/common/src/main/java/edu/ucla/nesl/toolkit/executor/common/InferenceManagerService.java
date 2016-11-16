package edu.ucla.nesl.toolkit.executor.common;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import edu.ucla.nesl.toolkit.executor.common.module.InferencePipeline;
import edu.ucla.nesl.toolkit.executor.common.util.InferencePipelineBuilder;

public class InferenceManagerService extends Service {
    private static final String TAG = "InfMgrService";
    private static final String INF_JSON = "inference_pipeline.json";

    private static String deviceType;

    public InferenceManagerService() {
    }

    public void configureRule() {
        // Phone first, watch first, etc.
    }

    public void configureInfereceExecution() {
        InferencePipeline pipeline = InferencePipelineBuilder.buildFromJSON(
                getApplicationContext(),
                INF_JSON);
        new InferenceExecutor();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private void checkConnectivity() {
        // Check if device is connected via BTLE
    }

    private void checkSensorCoverage() {
        // Check if device can provide meaningful sensing
    }

    private void executeInference() {
        // Execute an inference pipeline
    }
}
