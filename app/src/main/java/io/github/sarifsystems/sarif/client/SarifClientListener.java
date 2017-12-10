package io.github.sarifsystems.sarif.client;

public interface SarifClientListener {
    void onConnected();
    void onMessageReceived(SarifMessage msg);
    void onConnectionLost(Exception e);
}