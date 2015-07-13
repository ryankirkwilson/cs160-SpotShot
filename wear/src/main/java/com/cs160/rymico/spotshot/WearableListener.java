package com.cs160.rymico.spotshot;

import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import java.util.List;

public class WearableListener extends WearableListenerService {

    private GoogleApiClient google;
    private static final String START_ACTIVITY = "/start/Login";
    private String node;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        initGoogleApiClient();
        acquireNode();
        sendMessage(node);
        return START_STICKY;
    }

    protected void initGoogleApiClient() {
        google = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();

        google.connect();
    }
    private void acquireNode() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<Node> nodesResult = (List<Node>) Wearable.NodeApi.getConnectedNodes(google).await().getNodes();
                node = nodesResult.get(0).getId();
            }
        }).start();
        Log.w("acquire wear", "acquire wear");
    }

    private void sendMessage(String node) {
        Wearable.MessageApi.sendMessage(google, node, START_ACTIVITY, new byte[0]).setResultCallback(new ResultCallback<MessageApi.SendMessageResult>() {
            @Override
            public void onResult(MessageApi.SendMessageResult sendMessageResult) {
                Log.w("sendMessage wear", "sendMessage wear");
            }
        });
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent){
        node = messageEvent.getSourceNodeId();
        if (messageEvent.getPath().equalsIgnoreCase(START_ACTIVITY)){
            Log.w("onMessageReceived Wear", "onMessageReceived Wear");
            Intent accel = new Intent(this, AccelerometerListenerService.class);
            accel.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(accel);
        }
        else {
            super.onMessageReceived(messageEvent);
        }

    }
}
