package edu.ucla.nesl.toolkit.executor;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.MessageEvent;

import edu.ucla.nesl.toolkit.executor.common.communication.CommunicationManager;
import edu.ucla.nesl.toolkit.executor.common.communication.SharedConstant;

/**
 * Created by cgshen on 11/17/16.
 */

public class MobileCommunicationManager extends CommunicationManager {
    protected static final String TAG = "MobileBleClient";
    private static MobileCommunicationManager instance;

    public static synchronized MobileCommunicationManager getInstance(Context context) {
        if (instance == null) {
            instance = new MobileCommunicationManager(context.getApplicationContext());
        }
        return instance;
    }

    public MobileCommunicationManager() {
        super();
    }

    public MobileCommunicationManager(Context context) {
        super(context);
        // wakeupHandler.sendEmptyMessage(0);
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        super.onDataChanged(dataEvents);
        for (DataEvent dataEvent : dataEvents) {
            if (dataEvent.getType() == DataEvent.TYPE_CHANGED) {
                DataItem dataItem = dataEvent.getDataItem();
                Uri uri = dataItem.getUri();
                String path = uri.getPath();
                // TODO: unpack the received sensor data
            }
        }
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        super.onMessageReceived(messageEvent);
        if (messageEvent.getPath().equals(SharedConstant.PATH_INF_READY)) {
            MobileInferenceManager.wearReady = true;
            Log.i(TAG, "Wear-side InferenceExecutor configure succeeded.");
        }
        else if (messageEvent.getPath().equals(SharedConstant.PATH_INF_STARTED)) {
            MobileInferenceManager.wearRunning = true;
            Log.i(TAG, "Wear-side InferenceExecutor started.");
            Intent intent = new Intent();
            intent.setAction(SharedConstant.PATH_INF_STARTED);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
            Log.i(TAG, "Sending broadcast PATH_INF_STARTED.");
        }
        else if (messageEvent.getPath().equals(SharedConstant.PATH_INF_STOPPED)) {
            MobileInferenceManager.wearRunning = false;
            Log.i(TAG, "Wear-side InferenceExecutor stopped.");
            Intent intent = new Intent();
            intent.setAction(SharedConstant.PATH_INF_STOPPED);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
            Log.i(TAG, "Sending broadcast PATH_INF_STOPPED.");
        }
        else if (messageEvent.getPath().equals(SharedConstant.PATH_DEVICE_ACTIVE)) {
            Log.i(TAG, "Device active beacon (SigMo) received!");
            Intent intent = new Intent();
            intent.setAction(SharedConstant.PATH_DEVICE_ACTIVE);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
            Log.i(TAG, "Sending broadcast PATH_DEVICE_ACTIVE.");
        }
        else {
            Log.e(TAG, "Error: unknown message path.");
        }
    }

    private Handler wakeupHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    MobileCommunicationManager.this.sendMessage(SharedConstant.PATH_WAKEUP, null);
                }
            });
            sendEmptyMessageDelayed(0, 10000);
        }
    };
}
