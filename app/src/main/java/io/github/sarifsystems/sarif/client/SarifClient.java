package io.github.sarifsystems.sarif.client;

import android.net.Uri;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class SarifClient {

    protected String deviceId;
    protected String hostUrl;

    protected Socket connection;
    protected BufferedReader in;
    protected BufferedWriter out;
    protected BlockingQueue<Message> pubQueue;

    protected SarifClientListener listener;

    public SarifClient(String deviceId, SarifClientListener listener) {
        this.deviceId = deviceId;
        this.listener = listener;
        this.pubQueue = new ArrayBlockingQueue<>(10);
    }

    public void connect(String hostUrl) throws IOException {
        if (isConnected()) {
            return;
        }

        this.hostUrl = hostUrl;
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    SarifClient.this.doConnect();
                } catch (Exception e) {
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
                Message msg = pubQueue.take();
                String raw = msg.encode();
                out.write(raw);
                out.newLine();
                out.flush();
            } catch (JSONException e) {
                e.printStackTrace();
            }catch (Exception e) {
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

        pubQueue.add(msg);
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

    protected void doConnect() throws Exception {
        Log.d("SarifClient", "uh, hi");
        Uri host = Uri.parse(hostUrl);
        
        boolean tls = false;
        if (host.getScheme().contains("tls")) {
            tls = true;
        }
        Log.d("SarifClient", "one step further");
        InetAddress addr = InetAddress.getByName(host.getHost());
        int port = host.getPort();
        if (port <= 0) {
            port = (tls ? 23443 : 23100);
        }
        Log.d("SarifClient", "connecting to " + addr + ":" + port);
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

        in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        out = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));

        pubQueue.clear();

        Message msg = new Message();
        msg.action = "proto/hi";
        msg.payload = new JSONObject();
        msg.payload.putOpt("auth", host.getQueryParameter("auth"));
        publish(msg);

        if (listener != null) {
            listener.onConnected();
        }
    }

    protected void readLoop() {
        try {
            while (true) {
                String line = in.readLine();
                Message msg = Message.decode(line);
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