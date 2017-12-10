package io.github.sarifsystems.sarif.client;

import android.net.Uri;

import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class SarifClient {

    protected String deviceId;
    protected String hostUrl;

    protected boolean isConnecting;
    protected Socket connection;
    protected BufferedReader in;
    protected BufferedWriter out;
    protected BlockingQueue<SarifMessage> pubQueue;

    protected SarifClientListener listener;

    public SarifClient(String deviceId, SarifClientListener listener) {
        this.deviceId = deviceId;
        this.listener = listener;
        this.pubQueue = new ArrayBlockingQueue<>(10);
    }

    public void connect(String hostUrl) throws IOException {
        if (isConnected()) {
            SarifMessage keepAlive = new SarifMessage();
            keepAlive.action = "$keepalive";
            pubQueue.add(keepAlive);
            return;
        }
        if (isConnecting) {
            return;
        }
        isConnecting = true;

        this.hostUrl = hostUrl;
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    SarifClient.this.doConnect();
                } catch (Exception e) {
                    isConnecting = false;
                    disconnect(e);
                    return;
                }

                Thread reader = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        SarifClient.this.readLoop();
                    }
                });
                reader.start();

                SarifClient.this.writeLoop();
            }
        });
        t.start();
    }

    private void writeLoop() {
        while (true) {
            try {
                SarifMessage msg = pubQueue.take();
                if (msg.isAction("$keepalive")) {
                    out.write(" ");
                } else {
                    String raw = SarifMessage.gson.toJson(msg);
                    out.write(raw);
                    out.newLine();
                }
                out.flush();
            } catch (Exception e) {
                this.disconnect(e);
            }
        }
    }

    public void disconnect() {
        disconnect(null);
    }

    public void disconnect(Exception reason) {
        if (reason != null) {
            reason.printStackTrace();
        }
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

    public void publish(SarifMessage msg) throws IOException {
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

        pubQueue.add(msg);
    }

    public void reply(SarifMessage orig, SarifMessage reply) throws IOException {
        if (reply.destination == null) {
            reply.destination = orig.source;
        }
        if (reply.corrId == null) {
            reply.corrId = orig.id;
        }
        publish(reply);
    }

    public void subscribe(String device, String action) throws IOException {
        if (device == null || device.isEmpty()) {
            subscribe("self", action);
        } else if (device.equals("self")) {
            device = deviceId;
        }

        JsonObject p = new JsonObject();
        p.addProperty("device", device);
        p.addProperty("action", action);
        SarifMessage msg = new SarifMessage("proto/sub", p);
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

    protected void doConnect() throws Exception {
        Uri host = Uri.parse(hostUrl);
        
        boolean tls = false;
        if (host.getScheme().contains("tls")) {
            tls = true;
        }
        InetAddress addr = InetAddress.getByName(host.getHost());
        int port = host.getPort();
        if (port <= 0) {
            port = (tls ? 23443 : 23100);
        }
        if (tls) {
            // TODO: Oh god, yes, we seriously need to fix this
            TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                @Override
                public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
                    // Not implemented
                }

                @Override
                public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
                    // Not implemented
                }
            }};
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new SecureRandom());
            connection = sc.getSocketFactory().createSocket(addr, port);
        } else {
            connection = new Socket(addr, port);
        }
        isConnecting = false;

        in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        out = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));

        pubQueue.clear();

        JsonObject p = new JsonObject();
        p.addProperty("auth", host.getQueryParameter("auth"));
        SarifMessage msg = new SarifMessage("proto/hi", p);
        publish(msg);

        if (listener != null) {
            listener.onConnected();
        }
    }

    protected void readLoop() {
        try {
            while (true) {
                String line = in.readLine();
                if (line == null) {
                    throw new IOException("EOF received");
                }
                if (line.isEmpty() || line.equals(" ")) {
                    continue;
                }
                SarifMessage msg = SarifMessage.gson.fromJson(line, SarifMessage.class);
                if (listener != null) {
                    listener.onMessageReceived(msg);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            disconnect(e);
        }
    }
}