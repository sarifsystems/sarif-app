package io.github.sarifsystems.sarif.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import io.github.sarifsystems.sarif.SarifServiceConnector;
import io.github.sarifsystems.sarif.client.Message;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(RemoteMessage msg) {
        Log.d("SarifFirebase", "From: " + msg.getFrom());

        if (msg.getData().size() > 0) {
            Log.d("SarifFirebase", "Message data payload: " + msg.getData());
        }

        SarifServiceConnector sarif = new SarifServiceConnector(getApplicationContext());
        sarif.runTask(new SarifServiceConnector.Task() {
            @Override
            public void run(SarifService service) {
                service.publish(new Message("push/fetch"));
            }
        });
    }
}
