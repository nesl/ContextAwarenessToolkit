package edu.ucla.nesl.toolkit.executor;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import edu.ucla.nesl.toolkit.common.model.type.DeviceType;
import edu.ucla.nesl.toolkit.executor.common.InferenceExecutor;
import edu.ucla.nesl.toolkit.executor.common.communication.SharedConstant;
import edu.ucla.nesl.toolkit.executor.common.module.DataInterface;
import edu.ucla.nesl.toolkit.executor.common.module.InferencePipeline;


public class WearInferenceManager extends Service {
    private static final String TAG = "WearInfMgr";

    private InferenceExecutor mInferenceExecutor = null;
    private WearCommunicationManager mCommunicationManager;
    private Context mContext;

    private final IBinder mBinder = new LocalBinder();
    public class LocalBinder extends Binder {
        WearInferenceManager getService() {
            return WearInferenceManager.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public WearInferenceManager() {

    }

    public void initService(Context context) {
        this.mContext = context;
        this.mCommunicationManager = WearCommunicationManager.getInstance(context);
    }

    public void configureInference(InferencePipeline pipeline) {
        if (pipeline != null) {
            mInferenceExecutor = new InferenceExecutor();
            mInferenceExecutor.setDeviceType(DeviceType.ANDROID_SMARTWATCH);
            mInferenceExecutor.setInferencePipeline(pipeline);
            mInferenceExecutor.setSource(DataInterface.SENSOR);
            mInferenceExecutor.setSink(DataInterface.NOTIFICATION);
            mInferenceExecutor.setDuration(1000 * 2);
            mInferenceExecutor.setInterval(1000 * 20);
            Log.i(TAG, "Wear-side InferenceExecutor ready.");

            // Let the phone know that the inference is ready
            mCommunicationManager.sendMessage(SharedConstant.PATH_INF_READY, null);
        }
        else {
            Log.e(TAG, "Null inference pipeline.");
        }
    }

    public void startInference() {
        if (mInferenceExecutor != null && mInferenceExecutor.getInferencePipeline() != null) {
            if (!mInferenceExecutor.isRunning()) {
                mInferenceExecutor.startInferenceAlarm(mContext);

                // Let the phone know that the inference is running
                mCommunicationManager.sendMessage(SharedConstant.PATH_INF_STARTED, null);
            }
            else {
                Log.e(TAG, "Error: inference already running.");
            }
        }
        else {
            Log.e(TAG, "Error: inference not configured.");
        }
    }

    public void stopInference() {
        if (mInferenceExecutor != null && mInferenceExecutor.getInferencePipeline() != null) {
            if (mInferenceExecutor.isRunning()) {
                mInferenceExecutor.stopInferenceAlarm(mContext);

                // Let the phone know that the inference is stopped
                mCommunicationManager.sendMessage(SharedConstant.PATH_INF_STOPPED, null);
            }
            else {
                Log.e(TAG, "Error: inference not running.");
            }
        }
        else {
            Log.e(TAG, "Error: inference not configured.");
        }
    }

}
