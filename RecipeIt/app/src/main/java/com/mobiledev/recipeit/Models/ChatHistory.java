package com.mobiledev.recipeit.Models;

import java.time.LocalDateTime;

public class ChatHistory {
    private final LocalDateTime createdAt;
    private final String message;
    private final boolean isServerMessage;

    public ChatHistory(LocalDateTime createdAt, String message, boolean isServerMessage) {
        this.createdAt = createdAt;
        this.message = message;
        this.isServerMessage = isServerMessage;
    }

    public static ChatHistory Server(String message) {
        return new ChatHistory(LocalDateTime.now(), message, true);
    }

    public static ChatHistory User(String message) {
        return new ChatHistory(LocalDateTime.now(), message, false);
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getMessage() {
        return message;
    }

    public boolean isServerMessage() {
        return isServerMessage;
    }
}
