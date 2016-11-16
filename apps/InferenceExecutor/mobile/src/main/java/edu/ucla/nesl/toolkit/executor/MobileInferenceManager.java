package edu.ucla.nesl.toolkit.executor;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import edu.ucla.nesl.toolkit.common.model.type.DeviceType;
import edu.ucla.nesl.toolkit.executor.common.InferenceExecutor;
import edu.ucla.nesl.toolkit.executor.common.module.DataInterface;
import edu.ucla.nesl.toolkit.executor.common.module.InferencePipeline;
import edu.ucla.nesl.toolkit.executor.common.util.InferencePipelineBuilder;

public class MobileInferenceManager extends Service {
    private static final String TAG = "MobileInfMgr";

    private static final String INF_JSON = "inference_pipeline.json";
    private InferenceExecutor mInferenceExecutor = null;

    private final IBinder mBinder = new LocalBinder();
    public class LocalBinder extends Binder {
        MobileInferenceManager getService() {
            return MobileInferenceManager.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public MobileInferenceManager() {

    }

    public void configureDefaultInference(Context context) {
        InferencePipeline pipeline = InferencePipelineBuilder.buildFromJSON(context, INF_JSON);
        mInferenceExecutor = new InferenceExecutor(
                DeviceType.ANDROID_PHONE,
                pipeline,
                DataInterface.SENSOR,
                DataInterface.NOTIFICATION,
                1000 * 20,
                1000
        );
        Log.i(TAG, "InferenceExecutor configure succeeded.");
    }

    public void startInference(Context context) {
        if (mInferenceExecutor != null) {
            if (!mInferenceExecutor.isRunning()) {
                mInferenceExecutor.startInferenceAlarm(context);
            }
            else {
                Log.e(TAG, "Error: inference already running.");
            }
        }
        else {
            Log.e(TAG, "Error: inference not configured.");
        }
    }

    public void stopInference(Context context) {
        if (mInferenceExecutor != null) {
            if (mInferenceExecutor.isRunning()) {
                mInferenceExecutor.stopInferenceAlarm(context);
            }
            else {
                Log.e(TAG, "Error: inference not running.");
            }
        }
        else {
            Log.e(TAG, "Error: inference not configured.");
        }
    }

    public void configureRule() {
        // Phone first, watch first, etc.
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

    public InferenceExecutor getmInferenceExecutor() {
        return mInferenceExecutor;
    }

    public void setmInferenceExecutor(InferenceExecutor mInferenceExecutor) {
        this.mInferenceExecutor = mInferenceExecutor;
    }
}
