package com.cs160.rymico.spotshot;

import android.content.Intent;
import android.os.Bundle;
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

public class MobileWearableListener extends WearableListenerService {

    private GoogleApiClient google;
    private String node;
    private static final String START_ACTIVITY = "/start/Login";
    private static final String BACK_ACTIVITY = "/start/MobileWearableListener";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        initGoogleApiClient();
        acquireNode();
        sendMessage(node);
        return super.onStartCommand(intent, flags, startId);
    }

    protected void initGoogleApiClient() {
        google = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();

        google.connect();
        Log.w("initGoogle mobile", "initGoogle mobile");
    }

    private void acquireNode() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<Node> nodesResult = (List<Node>) Wearable.NodeApi.getConnectedNodes(google).await().getNodes();
                node = nodesResult.get(0).getId();
            }
        }).start();
        Log.w("acquire mobile", "acquire mobile");
    }

    private void sendMessage(String node) {
        Wearable.MessageApi.sendMessage(google, node, BACK_ACTIVITY, new byte[0]).setResultCallback(new ResultCallback<MessageApi.SendMessageResult>() {
            @Override
            public void onResult(MessageApi.SendMessageResult sendMessageResult) {
                Log.w("sendMessage mobile", "sendMessage mobile");
            }
        });
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent){
        node = messageEvent.getSourceNodeId();
        if (messageEvent.getPath().equalsIgnoreCase(START_ACTIVITY)){
            Intent main = new Intent(this, MainActivity.class);
            main.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(main);
        }
        else {
            super.onMessageReceived(messageEvent);
        }
    }


}
