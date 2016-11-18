package edu.ucla.nesl.toolkit.executor;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.Map;

import edu.ucla.nesl.toolkit.common.model.type.DeviceType;
import edu.ucla.nesl.toolkit.executor.common.InferenceExecutor;
import edu.ucla.nesl.toolkit.executor.common.communication.SharedConstant;
import edu.ucla.nesl.toolkit.executor.common.module.DataInterface;
import edu.ucla.nesl.toolkit.executor.common.module.InferencePipeline;
import edu.ucla.nesl.toolkit.executor.common.module.StringConstant;
import edu.ucla.nesl.toolkit.executor.common.util.InferencePipelineBuilder;

public class MobileInferenceManager extends Service {
    private static final String TAG = "MobileInfMgr";

    public static final String PHONE_INF_STARTED = "phone_started";
    public static final String PHONE_INF_STOPPED = "phone_stopped";

    private static final String INF_JSON = "inference_pipeline.json";
    private InferenceExecutor mInferenceExecutor = null;
    private Context mContext;
    private MobileCommunicationManager mCommunicationManager;

    protected static boolean phoneReady = false;
    protected static boolean wearReady = false;
    protected static boolean wearRunning = false;

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

    public void initService(Context context) {
        this.mContext = context;
        mCommunicationManager = MobileCommunicationManager.getInstance(mContext);
        configureDefaultInference(mContext);
    }

    public void configureDefaultInference(Context context) {
        Map<String, InferencePipeline> pipelines = InferencePipelineBuilder.buildForMultipleDevice(
                context,
                InferencePipelineBuilder.readJSONFromAssets(context, INF_JSON));

        if (pipelines != null && !pipelines.isEmpty()) {
            // Set the phone-side inference executor
            if (pipelines.containsKey(StringConstant.ANDROID_PHONE)) {
                mInferenceExecutor = new InferenceExecutor();
                mInferenceExecutor.setDeviceType(DeviceType.ANDROID_PHONE);
                mInferenceExecutor.setInferencePipeline(
                        pipelines.get(StringConstant.ANDROID_PHONE));
                mInferenceExecutor.setSource(DataInterface.SENSOR);
                mInferenceExecutor.setSink(DataInterface.NOTIFICATION);
                mInferenceExecutor.setDuration(1000 * 2);
                mInferenceExecutor.setInterval(1000 * 20);
                phoneReady = true;
                Log.i(TAG, "Phone-side InferenceExecutor configure succeeded.");
            }

            // Set the wear-side inference executor
            if (pipelines.containsKey(StringConstant.ANDROID_WEAR)) {
                mCommunicationManager.sendMessage(
                        SharedConstant.PATH_CONFIGURE_INF,
                        pipelines.get(StringConstant.ANDROID_WEAR).serialize());
            }
        }
        else {
            Log.e(TAG, "No valid inference pipeline.");
        }
    }

    public void startInference() {
        if (wearReady) {
            if (!wearRunning) {
                // Start remote wear inference
                mCommunicationManager.sendMessage(SharedConstant.PATH_START_INF, null);
            }
            else {
                Log.e(TAG, "Error: wear inference already running.");
            }
        }
        else {
            if (phoneReady) {
                if (!mInferenceExecutor.isRunning()) {
                    mInferenceExecutor.startInferenceAlarm(mContext);
                    Intent intent = new Intent();
                    intent.setAction(PHONE_INF_STARTED);
                    LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
                    Log.i(TAG, "Sending broadcast PHONE_INF_STARTED.");
                }
                else {
                    Log.e(TAG, "Error: phone inference already running.");
                }
            }
            else {
                Log.e(TAG, "Error: phone inference not configured.");
            }
        }
    }

    public void stopInference() {
        if (wearReady) {
            if (wearRunning) {
                // Start remote wear inference
                mCommunicationManager.sendMessage(SharedConstant.PATH_INF_STOPPED, null);
            }
            else {
                Log.e(TAG, "Error: wear inference not running.");
            }
        }
        else {
            if (phoneReady) {
                if (mInferenceExecutor.isRunning()) {
                    mInferenceExecutor.stopInferenceAlarm(mContext);
                    Intent intent = new Intent();
                    intent.setAction(PHONE_INF_STOPPED);
                    LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
                    Log.i(TAG, "Sending broadcast PHONE_INF_STOPPED.");
                }
                else {
                    Log.e(TAG, "Error: phone inference not running.");
                }
            }
            else {
                Log.e(TAG, "Error: inference not configured.");
            }
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
}
