package com.mobiledev.recipeit.Models;

import java.time.LocalDateTime;

public class ChatHistory {
    private LocalDateTime createdAt;
    private String message;
    private boolean isServerMessage;
    private boolean isFavorite;

    // No-args constructor for Gson serialization
    public ChatHistory() {
        // Required for Gson
    }

    public ChatHistory(LocalDateTime createdAt, String message, boolean isServerMessage) {
        this.createdAt = createdAt;
        this.message = message;
        this.isServerMessage = isServerMessage;
        this.isFavorite = false;
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
    
    public boolean isFavorite() {
        return isFavorite;
    }
    
    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }
}
