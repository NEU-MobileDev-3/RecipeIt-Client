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
import com.google.firebase.auth.UserProfileChangeRequest;
import com.mobiledev.recipeit.databinding.ActivityRegisterBinding;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";
    private ActivityRegisterBinding binding;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            binding = ActivityRegisterBinding.inflate(getLayoutInflater());
            setContentView(binding.getRoot());

            // Initialize Firebase Auth
            auth = FirebaseAuth.getInstance();

            // Add this line to work around reCAPTCHA issues
            auth.useAppLanguage();

            // Setup click listeners
            binding.registerButton.setOnClickListener(v -> registerUser());
            binding.loginPrompt.setOnClickListener(v -> {
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                finish();
            });
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate", e);
            Toast.makeText(this, "Error initializing app: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void registerUser() {
        try {
            // Safely get text from input fields
            String name = binding.nameInput.getText() != null ?
                    binding.nameInput.getText().toString().trim() : "";

            String email = binding.emailInput.getText() != null ?
                    binding.emailInput.getText().toString().trim() : "";

            String password = binding.passwordInput.getText() != null ?
                    binding.passwordInput.getText().toString().trim() : "";

            String confirmPassword = binding.confirmPasswordInput.getText() != null ?
                    binding.confirmPasswordInput.getText().toString().trim() : "";

            // Validate inputs
            if (TextUtils.isEmpty(name)) {
                binding.nameInput.setError("Name is required");
                binding.nameInput.requestFocus();
                return;
            }

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

            if (password.length() < 6) {
                binding.passwordInput.setError("Password must be at least 6 characters");
                binding.passwordInput.requestFocus();
                return;
            }

            if (!password.equals(confirmPassword)) {
                binding.confirmPasswordInput.setError("Passwords do not match");
                binding.confirmPasswordInput.requestFocus();
                return;
            }

            // Show progress
            binding.progressBar.setVisibility(View.VISIBLE);

            // Create user with email and password
            auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            // Sign up success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = auth.getCurrentUser();

                            if (user != null) {
                                // Set display name
                                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                        .setDisplayName(name)
                                        .build();

                                user.updateProfile(profileUpdates)
                                        .addOnCompleteListener(task1 -> {
                                            binding.progressBar.setVisibility(View.GONE);
                                            if (task1.isSuccessful()) {
                                                Log.d(TAG, "User profile updated.");
                                                Toast.makeText(RegisterActivity.this,
                                                        "Registration successful", Toast.LENGTH_SHORT).show();
                                                Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                                                startActivity(intent);
                                                finish();
                                            } else {
                                                Log.w(TAG, "updateProfile:failure", task1.getException());
                                                String errorMessage = task1.getException() != null ?
                                                        task1.getException().getMessage() : "Could not update profile";
                                                Toast.makeText(RegisterActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            } else {
                                binding.progressBar.setVisibility(View.GONE);
                                Log.w(TAG, "User is null after successful registration");
                                Toast.makeText(RegisterActivity.this,
                                        "Registration error: Could not get user profile", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            // If sign up fails, display a message to the user
                            binding.progressBar.setVisibility(View.GONE);
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            String errorMessage = task.getException() != null ?
                                    task.getException().getMessage() : "Registration failed";
                            Toast.makeText(RegisterActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        binding.progressBar.setVisibility(View.GONE);
                        Log.e(TAG, "Registration failure", e);
                        Toast.makeText(RegisterActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        } catch (Exception e) {
            binding.progressBar.setVisibility(View.GONE);
            Log.e(TAG, "Registration error", e);
            Toast.makeText(RegisterActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}