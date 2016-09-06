package io.github.sarifsystems.sarif.client;

public interface SarifClientListener {
    void onConnected();
    void onMessageReceived(Message msg);
    void onConnectionLost(Exception e);
}