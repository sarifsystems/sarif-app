package io.github.sarifsystems.sarif.service;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.google.gson.JsonObject;

import io.github.sarifsystems.sarif.SarifServiceConnector;
import io.github.sarifsystems.sarif.client.SarifMessage;

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
                JsonObject p = new JsonObject();
                p.addProperty("token", refreshedToken);
                SarifMessage msg = new SarifMessage("push/register", p);
                sarif.publish(msg);
            }
        });
    }
}
