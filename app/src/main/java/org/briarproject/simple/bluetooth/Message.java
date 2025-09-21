package org.briarproject.simple.bluetooth;

public class Message {
    private final String content;
    private final String sender;
    private final boolean isSentByMe;
    private final long timestamp;
    
    public Message(String content, String sender, boolean isSentByMe) {
        this.content = content;
        this.sender = sender;
        this.isSentByMe = isSentByMe;
        this.timestamp = System.currentTimeMillis();
    }
    
    public String getContent() {
        return content;
    }
    
    public String getSender() {
        return sender;
    }
    
    public boolean isSentByMe() {
        return isSentByMe;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
}