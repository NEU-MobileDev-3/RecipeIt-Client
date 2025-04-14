package com.mobiledev.recipeit;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashActivity extends AppCompatActivity {
    private static final String TAG = "SplashActivity";
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
//
//        // Ensure Firebase is initialized
//        try {
//            if (!FirebaseApp.getApps(this).isEmpty()) {
//                // Initialize Firebase Auth
//                auth = FirebaseAuth.getInstance();
//                Log.d(TAG, "Firebase Auth initialized successfully");
//            } else {
//                Log.e(TAG, "Firebase not initialized. Starting login activity.");
//                startLoginActivity();
//                return;
//            }
//        } catch (Exception e) {
//            Log.e(TAG, "Error accessing Firebase", e);
//            startLoginActivity();
//            return;
//        }
//
         new Handler().postDelayed(() -> {    Intent intent = new Intent(SplashActivity.this, MainActivity.class);    startActivity(intent);    finish();}, 3000); // 3 seconds

//        new Handler().postDelayed(() -> {
//            try {
//                // Check if user is already signed in
//                FirebaseUser currentUser = auth.getCurrentUser();
//                Intent intent;
//
//                if (currentUser != null) {
//                    // User is already logged in, go to MainActivity
//                    intent = new Intent(SplashActivity.this, MainActivity.class);
//                } else {
//                    // User is not logged in, go to LoginActivity
//                    intent = new Intent(SplashActivity.this, LoginActivity.class);
//                }
//
//                startActivity(intent);
//                finish();
//            } catch (Exception e) {
//                Log.e(TAG, "Error checking user state", e);
//                startLoginActivity();
//            }
//        }, 3000); // 3 seconds
    }

    private void startLoginActivity() {
        Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}