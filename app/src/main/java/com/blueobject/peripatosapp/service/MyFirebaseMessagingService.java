package com.blueobject.peripatosapp.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;


import com.blueobject.peripatosapp.App;
import com.blueobject.peripatosapp.R;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;



public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        Log.e(TAG, "From: " + remoteMessage.getFrom());
        /*
        if (remoteMessage.getData().size() > 0) {
            Log.e(TAG, "Message data payload: " + remoteMessage.getData());
            if (true) {
                scheduleJob();
            } else {
                handleNow();
            }
        }
        */

        String body = "";
        String tag = "";
        if (remoteMessage.getNotification() != null) {
            body = remoteMessage.getNotification().getBody();
            tag = remoteMessage.getNotification().getTag();
        }

        Map<String, String> data = remoteMessage.getData();

        tag = data.get("tag");

        if (tag.equals("fetchdata")) {

            Log.e("APP", "fetching DATA");

            App.fetchData();
        }

        if (tag.equals("custom")) {

            sendNotification(null, data.get("title"), data.get("body"));
        }

    }


    private void sendNotification(Intent i, String title, String messageBody) {

        Intent intent;

        intent = i;
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent, PendingIntent.FLAG_ONE_SHOT);
        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle(title)
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }

    private void scheduleJob() {
        /*
        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(this));
        Job myJob = dispatcher.newJobBuilder()
                .setService(MyJobService.class)
                .setTag("my-job-tag")
                .build();
        dispatcher.schedule(myJob);
        */
    }

    private void handleNow() {
        Log.d(TAG, "Short lived task is done.");
    }
}
