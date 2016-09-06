package io.github.sarifsystems.sarif.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.sarifsystems.sarif.client.Message;
import io.github.sarifsystems.sarif.client.SarifClient;
import io.github.sarifsystems.sarif.client.SarifClientListener;

public class SarifService extends Service implements SarifClientListener {

    protected SarifClient client;
    protected List<SarifClientListener> listeners;
    private Map<String, MessageReceiver> requests = new HashMap<>();

    private static final String TAG = "SarifService";

    public SarifService() {
        listeners = new ArrayList<SarifClientListener>();
    }

    @Override
    public void onCreate() {
        client = new SarifClient("android", this);
        try {
            client.connect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        client.disconnect();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new SarifServiceBinder(this);
    }

    public void publish(Message msg) {
        try {
            client.publish(msg);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void subscribe(String device, String action) {
        try {
            client.subscribe(device, action);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void request(Message msg, MessageReceiver receiver) {
        if (msg.id == null) {
            msg.id = SarifClient.generateId();
        }
        requests.put(msg.id, receiver);
        publish(msg);

        final String id = msg.id;
        final Handler h = new Handler();
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                requests.remove(id);
            }
        }, 30*1000);
    }

    public void addListener(SarifClientListener listener) {
        listeners.add(listener);

        if (client != null && client.isConnected()) {
            listener.onConnected();
        }
    }

    public void removeListener(SarifClientListener listener) {
        listeners.remove(listener);
    }

    public void onConnected() {
        subscribe("self", null);
        for (SarifClientListener listener : listeners) {
            listener.onConnected();
        }
    }

    public void onConnectionLost(Exception reason) {
        Log.e(TAG, "onConnectionLost: ", reason);
        for (SarifClientListener listener : listeners) {
            listener.onConnectionLost(reason);
        }
    }

    public void onMessageReceived(Message msg) {
        if (msg.corrId != null && requests.containsKey(msg.corrId)) {
            requests.get(msg.corrId).onMessageReceived(msg);
            return;
        }

        for (SarifClientListener listener : listeners) {
            listener.onMessageReceived(msg);
        }
    }

    public class SarifServiceBinder extends Binder {
        private SarifService service;

        public SarifServiceBinder(SarifService service) {
            this.service = service;
        }

        public SarifService getService() {
            return service;
        }
    }

    public interface MessageReceiver {
        public void onMessageReceived(Message msg);
    }
}
