package com.cs160.rymico.spotshot;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.wearable.view.WatchViewStub;
import android.widget.TextView;

import com.google.android.gms.wearable.WearableListenerService;

public class AccelerometerListenerService extends Service implements SensorEventListener{

    public static final int NOTIFY_TWEET = 1890;
    private SensorManager sensorManager;
    private Sensor accel;
    private static final int THRESHOLD = 100;

    private NotificationManager notmanager;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        notmanager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, accel, SensorManager.SENSOR_DELAY_FASTEST);
        return START_STICKY;
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Sensor mySensor = sensorEvent.sensor;

        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = sensorEvent.values[0];
            float y = sensorEvent.values[1];
            float z = sensorEvent.values[2];
            float speed = Math.max(Math.abs(x), Math.max(Math.abs(y), Math.abs(z)));


            if (speed > THRESHOLD) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Intent tweet = new Intent(AccelerometerListenerService.this, MainActivity.class);
                        tweet.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        PendingIntent pendingIntent = PendingIntent.getActivity(AccelerometerListenerService.this, 0, tweet, 0);
                        NotificationCompat.Builder notbuilder = new NotificationCompat.Builder(AccelerometerListenerService.this)
                                .setContentTitle("Are you excited?")
                                .setContentText("Take a SpotShot!")
                                .setSmallIcon(android.R.drawable.ic_menu_camera)
                                .setContentIntent(pendingIntent)
                                .addAction(android.R.drawable.ic_menu_camera, "Camera", pendingIntent);
                        notmanager.notify(NOTIFY_TWEET, notbuilder.build());
                    }
                }).start();
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    public IBinder onBind(Intent intent){
        return null;
    }

    public void onCreate() {
        super.onCreate();
        sensorManager.registerListener(this, accel, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void onDestroy() {
        super.onDestroy();
        sensorManager.unregisterListener(this);
    }


}

