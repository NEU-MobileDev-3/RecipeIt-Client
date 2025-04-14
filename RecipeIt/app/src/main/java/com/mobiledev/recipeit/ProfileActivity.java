package com.mobiledev.recipeit;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class ProfileActivity extends AppCompatActivity {
    private TextView profileName;
    private TextView profileEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);

        // Initialize UI components
        profileName = findViewById(R.id.profileNameText);
        profileEmail = findViewById(R.id.profileEmailText);

        // Set up the profile information
        var auth = FirebaseAuth.getInstance();
        var currentUser = auth.getCurrentUser();

        if (currentUser != null) {
            String name = currentUser.getDisplayName();
            String email = currentUser.getEmail();

            profileName.setText(name);
            profileEmail.setText(email);
        } else {
            // Handle the case where the user is not logged in
            profileName.setText("Guest");
            profileEmail.setText("Not logged in");
        }
    }

    public void onLogout(View v) {
        profileName.setText("Guest");
        profileEmail.setText("Not logged in");

        var auth = FirebaseAuth.getInstance();
        auth.signOut();

        // Redirect to login activity
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}