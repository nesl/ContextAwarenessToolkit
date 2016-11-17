package edu.ucla.nesl.toolkit.executor;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;

import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;

import edu.ucla.nesl.toolkit.executor.common.communication.CommunicationManager;

/**
 * Created by cgshen on 11/17/16.
 */

public class MobileCommunicationManager extends CommunicationManager {
    protected static final String TAG = "MobileBleClient";
    private static CommunicationManager instance;

    public static synchronized CommunicationManager getInstance(Context context) {
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

    private Handler wakeupHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    // TODO: Send message to wake up the other device
                    // sendMessage(WAKEUP...)
                }
            });
            sendEmptyMessageDelayed(0, 10000);
        }
    };
}
