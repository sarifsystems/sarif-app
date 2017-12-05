package io.github.sarifsystems.sarif.service;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import org.json.JSONException;
import org.json.JSONObject;

import io.github.sarifsystems.sarif.SarifAwareActivity;
import io.github.sarifsystems.sarif.SarifServiceConnector;
import io.github.sarifsystems.sarif.client.Message;
import io.github.sarifsystems.sarif.client.SarifClientListener;

public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {

    private SarifServiceConnector sarif;

    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        final String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d("SarifFirebase", "Refreshed token: " + refreshedToken);

        sarif.runTask(new SarifServiceConnector.Task() {
            @Override
            public void run(SarifService sarif) {
                try {
                    Message msg = new Message("push/register");
                    msg.payload = new JSONObject();
                    msg.payload.putOpt("token", refreshedToken);
                    sarif.publish(msg);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
