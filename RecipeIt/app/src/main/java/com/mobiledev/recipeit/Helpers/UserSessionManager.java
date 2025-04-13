package com.mobiledev.recipeit.Helpers;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.mobiledev.recipeit.LoginActivity;

public class UserSessionManager {

    private static final String PREF_NAME = "RecipeItUserPref";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_USER_EMAIL = "userEmail";
    private static final String KEY_USER_NAME = "userName";

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private Context context;

    // Constructor
    public UserSessionManager(Context context) {
        this.context = context;
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    /**
     * Save user data to SharedPreferences when a user logs in
     */
    public void saveUserSession(FirebaseUser user) {
        if (user != null) {
            editor.putString(KEY_USER_ID, user.getUid());
            editor.putString(KEY_USER_EMAIL, user.getEmail());
            editor.putString(KEY_USER_NAME, user.getDisplayName());
            editor.apply();
        }
    }

    /**
     * Check if user is logged in
     */
    public boolean isLoggedIn() {
        return pref.contains(KEY_USER_ID) && FirebaseAuth.getInstance().getCurrentUser() != null;
    }

    /**
     * Get stored user data
     */
    public String getUserId() {
        return pref.getString(KEY_USER_ID, null);
    }

    public String getUserEmail() {
        return pref.getString(KEY_USER_EMAIL, null);
    }

    public String getUserName() {
        return pref.getString(KEY_USER_NAME, null);
    }

    /**
     * Clear user data and logout
     */
    public void logoutUser() {
        // Clear all data from SharedPreferences
        editor.clear();
        editor.apply();

        // Logout from Firebase
        FirebaseAuth.getInstance().signOut();

        // Redirect user to login activity
        Intent intent = new Intent(context, LoginActivity.class);
        // Clear all previous activities
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
    }
}