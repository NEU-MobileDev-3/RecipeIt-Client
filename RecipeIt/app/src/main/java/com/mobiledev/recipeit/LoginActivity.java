package com.mobiledev.recipeit;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.mobiledev.recipeit.databinding.ActivityLoginBinding;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private ActivityLoginBinding binding;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance();

        // Add this line to work around reCAPTCHA issues
        auth.useAppLanguage();

        // Setup click listeners
        binding.loginButton.setOnClickListener(v -> loginUser());
        binding.registerPrompt.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            finish();
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Check if user is already signed in
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            // User is already logged in, go to MainActivity
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        }
    }

    private void loginUser() {
        String email = binding.emailInput.getText().toString().trim();
        String password = binding.passwordInput.getText().toString().trim();

        // Validate inputs
        if (TextUtils.isEmpty(email)) {
            binding.emailInput.setError("Email is required");
            binding.emailInput.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            binding.passwordInput.setError("Password is required");
            binding.passwordInput.requestFocus();
            return;
        }

        // Show progress
        binding.progressBar.setVisibility(View.VISIBLE);

        // Authenticate with Firebase
        try {
            auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        binding.progressBar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            // Sign in success
                            Log.d(TAG, "signInWithEmail:success");
                            Toast.makeText(LoginActivity.this, "Login successful", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            // If sign in fails, display a message to the user
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            String errorMessage = task.getException() != null ?
                                    task.getException().getMessage() : "Authentication failed";
                            Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                        }
                    });
        } catch (Exception e) {
            binding.progressBar.setVisibility(View.GONE);
            Log.e(TAG, "Login error", e);
            Toast.makeText(LoginActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}