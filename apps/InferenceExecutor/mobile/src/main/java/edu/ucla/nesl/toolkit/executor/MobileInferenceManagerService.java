package edu.ucla.nesl.toolkit.executor;

import android.content.Intent;
import android.os.IBinder;

import edu.ucla.nesl.toolkit.executor.common.InferenceManagerService;

public class MobileInferenceManagerService extends InferenceManagerService {
    public MobileInferenceManagerService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
