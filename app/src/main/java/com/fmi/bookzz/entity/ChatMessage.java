package com.fmi.bookzz.entity;

public class ChatMessage {
    private String text;
    private boolean isSender;

    public ChatMessage(String text, boolean isSender) {
        this.text = text;
        this.isSender = isSender;
    }

    public ChatMessage() {
    }

    public String getText() {
        return text;
    }

    public boolean isSender() {
        return isSender;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setSender(boolean sender) {
        isSender = sender;
    }
}
