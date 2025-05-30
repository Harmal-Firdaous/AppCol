package com.example.appco;

public class Message {
    private String senderId;
    private String text;
    private long timestamp;

    public Message() {} // Needed for Firebase

    public Message(String senderId, String text, long timestamp) {
        this.senderId = senderId;
        this.text = text;
        this.timestamp = timestamp;
    }

    public String getSenderId() { return senderId; }
    public String getText() { return text; }
    public long getTimestamp() { return timestamp; }
}
