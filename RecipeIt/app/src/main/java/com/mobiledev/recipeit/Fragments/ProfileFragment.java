package com.mobiledev.recipeit.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.mobiledev.recipeit.LoginActivity;
import com.mobiledev.recipeit.R;

public class ProfileFragment extends Fragment {
    private TextView profileName;
    private TextView profileEmail;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize UI components
        profileName = view.findViewById(R.id.profileNameText);
        profileEmail = view.findViewById(R.id.profileEmailText);

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
        Intent intent = new Intent(getContext(), LoginActivity.class);
        startActivity(intent);
    }
}
