package com.mobiledev.recipeit.Helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.firebase.auth.FirebaseUser;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.mobiledev.recipeit.Models.ChatHistory;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ChatHistoryManager {
    private static final String TAG = "ChatHistoryManager";
    private static final String PREF_NAME = "chat_history_prefs";
    private static final String KEY_HISTORY_PREFIX = "chat_history_";
    private static final String KEY_FAVORITES_PREFIX = "favorites_";
    
    private final SharedPreferences prefs;
    private final Gson gson;
    private final String userId;
    
    public ChatHistoryManager(Context context, FirebaseUser user) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .create();
        userId = user != null ? user.getUid() : "default_user";
    }
    
    public void saveHistory(List<ChatHistory> histories) {
        try {
            String historyJson = gson.toJson(histories);
            prefs.edit().putString(KEY_HISTORY_PREFIX + userId, historyJson).apply();
            Log.d(TAG, "Chat history saved successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error saving chat history", e);
        }
    }
    
    public List<ChatHistory> loadHistory() {
        try {
            String historyJson = prefs.getString(KEY_HISTORY_PREFIX + userId, null);
            if (historyJson == null) {
                return new ArrayList<>();
            }
            
            Type historyListType = new TypeToken<ArrayList<ChatHistory>>(){}.getType();
            return gson.fromJson(historyJson, historyListType);
        } catch (Exception e) {
            Log.e(TAG, "Error loading chat history", e);
            return new ArrayList<>();
        }
    }
    
    public void saveFavorites(List<ChatHistory> favorites) {
        try {
            String favoritesJson = gson.toJson(favorites);
            prefs.edit().putString(KEY_FAVORITES_PREFIX + userId, favoritesJson).apply();
            Log.d(TAG, "Favorites saved successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error saving favorites", e);
        }
    }
    
    public List<ChatHistory> loadFavorites() {
        try {
            String favoritesJson = prefs.getString(KEY_FAVORITES_PREFIX + userId, null);
            if (favoritesJson == null) {
                return new ArrayList<>();
            }
            
            Type favoritesListType = new TypeToken<ArrayList<ChatHistory>>(){}.getType();
            return gson.fromJson(favoritesJson, favoritesListType);
        } catch (Exception e) {
            Log.e(TAG, "Error loading favorites", e);
            return new ArrayList<>();
        }
    }
    
    public void updateItemFavoriteStatus(int position, List<ChatHistory> histories, boolean isFavorite) {
        if (position >= 0 && position < histories.size()) {
            histories.get(position).setFavorite(isFavorite);
            saveHistory(histories);
            
            // Update favorites list
            List<ChatHistory> favorites = histories.stream()
                .filter(ChatHistory::isFavorite)
                .toList();
            saveFavorites(new ArrayList<>(favorites));
        }
    }
    
    public void clearAllHistory() {
        prefs.edit()
            .remove(KEY_HISTORY_PREFIX + userId)
            .remove(KEY_FAVORITES_PREFIX + userId)
            .apply();
    }
}