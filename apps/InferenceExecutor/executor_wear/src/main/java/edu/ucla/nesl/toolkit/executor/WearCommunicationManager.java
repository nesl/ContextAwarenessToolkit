package edu.ucla.nesl.toolkit.executor;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import edu.ucla.nesl.toolkit.executor.common.communication.CommunicationManager;
import edu.ucla.nesl.toolkit.executor.common.communication.SharedConstant;

/**
 * Created by cgshen on 11/17/16.
 */

public class WearCommunicationManager extends CommunicationManager {
    protected static final String TAG = "WearBleClient";
    private static CommunicationManager instance;

    public static synchronized CommunicationManager getInstance(Context context) {
        if (instance == null) {
            instance = new WearCommunicationManager(context.getApplicationContext());
        }
        return instance;
    }

    public WearCommunicationManager() {
        super();
    }

    public WearCommunicationManager(Context context) {
        super(context);
    }

    public void putDataArrayAsync(final long timestamp, final byte[] values) {
        executorService.submit(new Runnable() {

            @Override
            public void run() {
                putDataArray(timestamp, values);
            }

        });
    }

    private void putDataArray(long timestamp, byte[] values) {
        PutDataMapRequest dataMap = PutDataMapRequest.create(SharedConstant.PATH_TEST);
        dataMap.getDataMap().putLong(SharedConstant.KEY_TIMESTAMP, timestamp);
        dataMap.getDataMap().putByteArray(SharedConstant.KEY_VALUE, values);
        PutDataRequest putDataRequest = dataMap.asPutDataRequest();
        putDataRequest(putDataRequest);
    }

    private void putDataRequest(PutDataRequest putDataRequest) {
        if (validateConnection()) {
            Wearable.DataApi.putDataItem(
                    googleApiClient,
                    putDataRequest
            ).setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
                @Override
                public void onResult(DataApi.DataItemResult dataItemResult) {
                    Log.v(TAG, "Sending data message: " + dataItemResult.getStatus().isSuccess());
                }
            });
        }
    }

    /**
     * Callback of receiving message from the other device
     * @param messageEvent
     */
    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        super.onMessageReceived(messageEvent);
        Log.i(TAG, "Received message: " + messageEvent.getPath());

        // Perform corresponding actions based on the message content
        if (messageEvent.getPath().equals(SharedConstant.PATH_TEST)) {

        }

    }

}
