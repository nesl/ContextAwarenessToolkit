package edu.ucla.nesl.toolkit.executor.common.communication;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by cgshen on 11/16/16.
 */

public class CommunicationManager extends WearableListenerService implements DataApi.DataListener {
    protected static final String TAG = "BleClient";
    protected static final int CLIENT_CONNECTION_TIMEOUT = 15000;

    protected GoogleApiClient googleApiClient;
    protected ExecutorService executorService;

    public CommunicationManager() {

    }

    public CommunicationManager(Context context) {
        // Initialize API client
        this.googleApiClient = new GoogleApiClient.Builder(context)
                .addApi(Wearable.API)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {

                    @Override
                    public void onConnected(@Nullable Bundle bundle) {
                        Log.i(TAG, "API Client connected, bundle=: " + bundle);
                        Wearable.DataApi.addListener(googleApiClient, CommunicationManager.this);
                        Log.i(TAG, "DataApi listener registered");
                    }

                    @Override
                    public void onConnectionSuspended(int i) {
                        Log.i(TAG, "API Client suspended, " + i);
                    }

                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {

                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        Log.i(TAG, "API Client connection failed: " + connectionResult);
                    }

                })
                .build();

        // Initialize thread pool
        executorService = Executors.newCachedThreadPool();
    }

    public void connect() {
        Log.i(TAG, "Connecting API client...");
        googleApiClient.connect();
    }

    public void disconnect() {
        Log.i(TAG, "Disconnecting API client...");
        Wearable.DataApi.removeListener(googleApiClient, this);
        googleApiClient.disconnect();
    }

    public boolean validateConnection() {
        if (googleApiClient.isConnected()) {
            return true;
        }
        ConnectionResult result = googleApiClient.blockingConnect(
                CLIENT_CONNECTION_TIMEOUT,
                TimeUnit.MILLISECONDS);
        return result.isSuccess();
    }

    public void sendMessage(final String path, final byte[] data) {
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                sendMessageAsync(path, data);
            }
        });
    }

    private void sendMessageAsync(final String path, byte[] data) {
        if (validateConnection()) {
            // Get list of nodes
            List<Node> nodes = Wearable.NodeApi.getConnectedNodes(
                    googleApiClient
            ).await().getNodes();

            // Send message to each node
            Log.i(TAG, "Sending message to nodes: " + nodes.size());
            for (Node node : nodes) {
                Wearable.MessageApi.sendMessage(
                        googleApiClient, node.getId(), path, data
                ).setResultCallback(new ResultCallback<MessageApi.SendMessageResult>() {

                    @Override
                    public void onResult(MessageApi.SendMessageResult result) {
                        Log.i(TAG, "Sent path=" + path + "? " + result.getStatus().isSuccess());
                    }

                });
            }
        } else {
            Log.e(TAG, "Error: invalid connection.");
        }
    }

    /**
     * Callback of receiving data from the other device
     * @param dataEvents
     */
    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        Log.i(TAG, "onDataChanged()");
    }
}

