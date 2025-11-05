package com.example.grob_simpleaichatbot;

public class Message {
    private final String text;
    private final boolean fromUser;

    public Message(String text, boolean fromUser) {
        this.text = text;
        this.fromUser = fromUser;
    }

    public String getText() {
        return text;
    }

    public boolean isFromUser() {
        return fromUser;
    }
}
