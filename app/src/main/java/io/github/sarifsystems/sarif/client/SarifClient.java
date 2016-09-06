package io.github.sarifsystems.sarif.client;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Random;

public class SarifClient {

    protected String deviceId;

    protected Socket connection;
    protected BufferedReader in;
    protected BufferedWriter out;

    protected SarifClientListener listener;

    public SarifClient(String deviceId, SarifClientListener listener) {
        this.deviceId = deviceId;
        this.listener = listener;
    }

    public void connect() throws IOException {
        if (isConnected()) {
            return;
        }

        Thread reader = new Thread(new Runnable() {
            @Override
            public void run() {
                SarifClient.this.readLoop();
            }
        });
        reader.start();
    }

    public void disconnect() {
        disconnect(null);
    }

    public void disconnect(Exception reason) {
        if (!isConnected()) {
            return;
        }

        try {
            connection.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        connection = null;
        in = null;
        out = null;
        if (listener != null) {
            listener.onConnectionLost(reason);
        }
    }

    public boolean isConnected() {
        return connection != null;
    }

    public void publish(Message msg) throws JSONException, IOException {
        if (!isConnected()) {
            throw new IOException("Not connected to Sarif");
        }
        if (msg.version == null) {
            msg.version = "0.5";
        }
        if (msg.id == null) {
            msg.id = generateId();
        }
        if (msg.source == null) {
            msg.source = this.deviceId;
        }

        String raw = msg.encode();

        try {
            out.write(raw);
            out.newLine();
            out.flush();
        } catch (IOException e) {
            this.disconnect(e);
        }
    }

    public void subscribe(String device, String action) throws JSONException, IOException {
        if (device == null || device.isEmpty()) {
            subscribe("self", action);
        } else if (device.equals("self")) {
            device = deviceId;
        }

        Message msg = new Message();
        msg.action = "proto/sub";
        msg.payload = new JSONObject();
        msg.payload.putOpt("device", device);
        msg.payload.putOpt("action", action);

        publish(msg);
    }

    public static String generateId() {
        final String alphanum = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz";
        Random rnd = new Random();

        StringBuilder sb = new StringBuilder(8);
        for (int i = 0; i < 8; i++)
            sb.append(alphanum.charAt(rnd.nextInt(alphanum.length())));
        return sb.toString();
    }

    protected void readLoop() {
        try {
            InetAddress addr = InetAddress.getByName("192.168.56.101");
            connection = new Socket(addr, 23100);

            in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));

            if (listener != null) {
                listener.onConnected();
            }

            while (true) {
                String line = in.readLine();
                Message msg = Message.decode(line);
                if (listener != null) {
                    listener.onMessageReceived(msg);
                }
            }
        } catch (Exception e) {
            disconnect(e);
        }
    }
}