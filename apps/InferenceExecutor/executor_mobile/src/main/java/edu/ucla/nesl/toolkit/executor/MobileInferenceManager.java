package edu.ucla.nesl.toolkit.executor;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Handler;
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
import edu.ucla.nesl.toolkit.executor.common.rule.RuntimeRule;
import edu.ucla.nesl.toolkit.executor.common.util.InferencePipelineBuilder;

public class MobileInferenceManager extends Service {
    private static final String TAG = "MobileInfMgr";

    public static final String PHONE_INF_STARTED = "phone_started";
    public static final String PHONE_INF_STOPPED = "phone_stopped";

    private static final String INF_JSON = "inference_pipeline.json";
    private InferenceExecutor mInferenceExecutor = null;
    private MobileCommunicationManager mCommunicationManager;

    private Context mContext;
    private RuntimeRule mRule;

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

    public void initService(Context context, RuntimeRule rule) {
        this.mContext = context;
        this.mRule = rule;
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

    private int tryRunningOnPhone() {
        if (phoneReady) {
            if (!mInferenceExecutor.isRunning()) {
                mInferenceExecutor.startInferenceAlarm(mContext);
                Intent intent = new Intent();
                intent.setAction(PHONE_INF_STARTED);
                LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
                Log.i(TAG, "Sending broadcast PHONE_INF_STARTED.");
                return 0;
            }
            else {
                Log.e(TAG, "Error: phone inference already running.");
                return -1;
            }
        }
        else {
            Log.e(TAG, "Error: phone inference not configured.");
            return -1;
        }
    }

    private int tryRunningOnWear() {
        if (wearReady) {
            if (!wearRunning) {
                // Start remote wear inference
                mCommunicationManager.sendMessage(SharedConstant.PATH_START_INF, null);
                return 0;
            }
            else {
                Log.e(TAG, "Error: wear inference already running.");
                return -1;
            }
        }
        else {
            Log.e(TAG, "Error: wear inference not configured.");
            return -1;
        }
    }

    private void tryStopOnPhone() {
        if (phoneReady) {
            if (mInferenceExecutor.isRunning()) {
                mInferenceExecutor.stopInferenceAlarm(mContext);
                Intent intent = new Intent();
                intent.setAction(PHONE_INF_STOPPED);
                LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
                Log.i(TAG, "Sending broadcast PHONE_INF_STOPPED.");
                return;
            }
        }
        Log.i(TAG, "Not stopping: phone inference not running.");
    }

    private void tryStopOnWear() {
        if (wearReady) {
            if (wearRunning) {
                // Stop remote wear inference
                mCommunicationManager.sendMessage(SharedConstant.PATH_STOP_INF, null);
                return;
            }
        }
        Log.i(TAG, "Not stopping: wear inference not running.");
    }

    public void startInference() {
        switch (mRule) {
            case phone_only:
                tryRunningOnPhone();
                return;
            case wear_only:
                tryRunningOnWear();
                return;
            case best_effort:
                // Try both wear and phone
                if (tryRunningOnWear() < 0) {
                    tryRunningOnPhone();
                }

                // Register broadcast receiver
                LocalBroadcastManager bManager = LocalBroadcastManager.getInstance(this);
                IntentFilter intentFilter = new IntentFilter();
                intentFilter.addAction(SharedConstant.PATH_DEVICE_ACTIVE);
                bManager.registerReceiver(mReceiver, intentFilter);
                Log.i(TAG, "Receiver registered.");

                // Start the device status monitor
                wearWatchDog.start();

                return;
            default:
                Log.e(TAG, "Error: unknown runtime rule");
        }
    }

    public void stopInference() {
        tryStopOnWear();
        tryStopOnPhone();
        if (wearWatchDog.running) {
            wearWatchDog.stop();

            // Unregister broadcast receiver
            LocalBroadcastManager bManager = LocalBroadcastManager.getInstance(this);
            bManager.unregisterReceiver(mReceiver);
            Log.i(TAG, "Receiver un-registered.");
        }
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "Broadcast received " + intent.getAction());
            if (intent.getAction().equals(SharedConstant.PATH_DEVICE_ACTIVE)) {
                if (wearWatchDog.running) {
                    wearWatchDog.addWearBeacon();
                }
            }
        }
    };

    // Watchdog for checking wear's status
    private Handler handler = new Handler();
    private final static long INACTIVE_TIMEOUT = 1000 * 30;
    protected WearWatchDog wearWatchDog = new WearWatchDog();
    private class WearWatchDog {
        private boolean wearIsAvailable = true;
        public boolean running;

        public void start() {
            running = true;
            if (wearIsAvailable)
                handler.postDelayed(moveToPhone, INACTIVE_TIMEOUT);
        }

        public void addWearBeacon() {
            Log.i(TAG, "Beacon ACKed.");
            if (wearIsAvailable) {
                handler.removeCallbacks(moveToPhone);

            }
            else {
                handler.post(moveToWear);
            }
            handler.postDelayed(moveToPhone, INACTIVE_TIMEOUT);
        }

        public void stop() {
            wearIsAvailable = true;
            handler.removeCallbacks(moveToPhone);
            running = false;
        }

        private Runnable moveToPhone = new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "Wear is no longer active, moving inference to phone...");
                wearIsAvailable = false;
                tryStopOnWear();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        tryRunningOnPhone();
                    }
                }, 1000);
            }
        };

        private Runnable moveToWear = new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "Wear is active again, moving inference to wear...");
                wearIsAvailable = true;
                tryStopOnPhone();
                tryRunningOnWear();
            }
        };
    }

    public RuntimeRule getmRule() {
        return mRule;
    }

    public void setmRule(RuntimeRule mRule) {
        this.mRule = mRule;
    }
}
