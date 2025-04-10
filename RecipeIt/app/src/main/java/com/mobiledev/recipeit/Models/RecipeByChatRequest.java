package com.mobiledev.recipeit.Models;

public class RecipeByChatRequest {
    public RecipeByChatRequest(String query) {
        this.query = query;
    }

    private final String query;

    public String getQuery() {
        return query;
    }
}
