package com.cs160.rymico.spotshot;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent wear = new Intent(this, WearableListener.class);
        Intent accel = new Intent(this, AccelerometerListenerService.class);
        startService(wear);
        startService(accel);
        this.finish();
    }
}
