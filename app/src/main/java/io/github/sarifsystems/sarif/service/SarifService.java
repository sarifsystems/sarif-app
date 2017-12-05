package io.github.sarifsystems.sarif.service;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.sarifsystems.sarif.MainActivity;
import io.github.sarifsystems.sarif.R;
import io.github.sarifsystems.sarif.client.Message;
import io.github.sarifsystems.sarif.client.SarifClient;
import io.github.sarifsystems.sarif.client.SarifClientListener;

public class SarifService extends Service implements SarifClientListener {

    protected String deviceId;
    protected SarifClient client;
    protected List<SarifClientListener> listeners;
    private Map<String, MessageReceiver> requests = new HashMap<>();
    private List<Message> unhandledMessages = new ArrayList<>();
    private int numRetries = 0;

    private static final String TAG = "SarifService";

    public SarifService() {
        listeners = new ArrayList<SarifClientListener>();
    }

    @Override
    public void onCreate() {
        client = new SarifClient(getDeviceId(), this);
    }

    @Override
    public void onDestroy() {
        client.disconnect();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("SarifService", "onStartCommand");
        connect();
        return Service.START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new SarifServiceBinder();
    }

    public String getDeviceId() {
        if (this.deviceId != null) {
            return this.deviceId;
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        this.deviceId = prefs.getString("pref_device_id", "");
        if (this.deviceId.equals("")) {
            this.deviceId = "android/" + SarifClient.generateId();
            prefs.edit().putString("pref_device_id", this.deviceId).commit();
        }
        return this.deviceId;
    }

    public void connect() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String host = prefs.getString("pref_host", "");

        try {
            client.connect(host);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
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

    public List<Message> getUnhandledMessages() {
        return new ArrayList<>(unhandledMessages);
    }

    public void clearUnhandledMessages() {
        unhandledMessages.clear();
    }

    public void removeListener(SarifClientListener listener) {
        listeners.remove(listener);
    }

    public void onConnected() {
        numRetries = 0;

        Log.d("SarifService", "onConnected");
        subscribe("self", null);

        try {
            Message msg = new Message("push/register");
            msg.payload = new JSONObject();
            msg.payload.putOpt("token", FirebaseInstanceId.getInstance().getToken());
            publish(msg);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, WakefulReceiver.class);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
        alarm.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                AlarmManager.INTERVAL_HALF_HOUR, AlarmManager.INTERVAL_HALF_HOUR, alarmIntent);

        for (SarifClientListener listener : listeners) {
            listener.onConnected();
        }
    }

    public void onConnectionLost(Exception reason) {
        Log.e(TAG, "onConnectionLost: ", reason);

        // Unclean shutdown? Try reconnecting
        if (reason != null) {
            numRetries++;
            if (numRetries <= 3) {
                AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                Intent intent = new Intent(this, WakefulReceiver.class);
                PendingIntent alarmIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
                alarm.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                        SystemClock.elapsedRealtime() + 60 * 100, alarmIntent);
            }
        }

        for (SarifClientListener listener : listeners) {
            listener.onConnectionLost(reason);
        }
    }

    public void onMessageReceived(Message msg) {
        if (msg.corrId != null && requests.containsKey(msg.corrId)) {
            requests.get(msg.corrId).onMessageReceived(msg);
            return;
        }

        if (msg.isAction("proto/ping")) {
            Message reply = new Message("proto/ack");
            try {
                client.reply(msg, reply);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }

        Log.d(TAG, "num receivers: " + listeners.size());
        for (SarifClientListener listener : listeners) {
            listener.onMessageReceived(msg);
        }

        if (listeners.isEmpty()) {
            unhandledMessages.add(msg);

            Intent intent = new Intent(this, MainActivity.class);
            PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            Notification n = new Notification.Builder(this)
                    .setContentTitle("New Sarif message")
                    .setContentText(msg.getText())
                    .setContentIntent(contentIntent)
                    .setSmallIcon(R.drawable.ic_stat_cat_silhouette)
                    .setAutoCancel(true)
                    .build();
            NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            nm.notify(0, n);
        }
    }

    public class SarifServiceBinder extends Binder {
        public SarifService getService() {
            return SarifService.this;
        }
    }

    public interface MessageReceiver {
        public void onMessageReceived(Message msg);
    }
}
