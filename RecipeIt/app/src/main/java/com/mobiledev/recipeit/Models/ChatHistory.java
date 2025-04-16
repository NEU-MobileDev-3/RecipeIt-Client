package com.mobiledev.recipeit.Models;

import java.time.LocalDateTime;

public class ChatHistory {
    private LocalDateTime createdAt;
    private String message;
    private boolean isServerMessage;
    private boolean isFavorite;
    private boolean isLoading;

    // No-args constructor for Gson serialization
    public ChatHistory() {
        // Required for Gson
    }

    public ChatHistory(LocalDateTime createdAt, String message, boolean isServerMessage, boolean isLoading) {
        this.createdAt = createdAt;
        this.message = message;
        this.isServerMessage = isServerMessage;
        this.isFavorite = false;
        this.isLoading = isLoading;
    }

    public static ChatHistory Server(String message) {
        return new ChatHistory(LocalDateTime.now(), message, true, false);
    }

    public static ChatHistory User(String message) {
        return new ChatHistory(LocalDateTime.now(), message, false, true);
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

    public void setLoading(boolean loading) {
        isLoading = loading;
    }

    public boolean isLoading() {
        return isLoading;
    }
}
