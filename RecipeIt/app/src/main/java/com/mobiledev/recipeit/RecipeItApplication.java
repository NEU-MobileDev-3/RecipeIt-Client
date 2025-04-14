package com.mobiledev.recipeit;

import android.app.Application;
import android.util.Log;

import com.google.firebase.FirebaseApp;

public class RecipeItApplication extends Application {
    private static final String TAG = "RecipeItApplication";

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize Firebase
//        try {
//            FirebaseApp.initializeApp(this);
//            Log.d(TAG, "Firebase initialized successfully");
//        } catch (Exception e) {
//            Log.e(TAG, "Error initializing Firebase", e);
//        }
  }
}