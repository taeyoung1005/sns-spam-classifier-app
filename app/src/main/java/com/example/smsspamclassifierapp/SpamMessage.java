package com.example.smsspamclassifierapp;

public class SpamMessage {
    private String phone_number;
    private String content;
    private String created_at;

    public String getPhoneNumber() {
        return phone_number;
    }

    public String getContent() {
        return content;
    }

    public String getCreatedAt() {
        return created_at;
    }
}
