package org.briarproject.simple.bluetooth;

public interface MessageListener {
    void onMessageReceived(String message, String senderAddress);
    void onMessageSent(String message);
    void onConnectionEstablished(String deviceAddress);
    void onConnectionLost(String deviceAddress);
}