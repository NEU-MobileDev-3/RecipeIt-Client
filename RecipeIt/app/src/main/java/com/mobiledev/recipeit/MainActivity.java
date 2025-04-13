package com.mobiledev.recipeit;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.mobiledev.recipeit.Helpers.RecipeApiClient;
import com.mobiledev.recipeit.Helpers.UserSessionManager;
import com.mobiledev.recipeit.Models.RecipeByChatRequest;
import com.mobiledev.recipeit.Models.RecipeByImageRequest;
import com.mobiledev.recipeit.databinding.ActivityMainBinding;

import java.io.ByteArrayOutputStream;

public class MainActivity extends AppCompatActivity {
    private static final String API_ENDPOINT = "http://10.0.2.2:4000/api";

    private ActivityMainBinding binding;
    private LinearLayout chatLayout;
    private EditText inputEditText;
    private ImageView sendIcon;
    private UserSessionManager sessionManager;
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        // Initialize ViewBinding
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize UserSessionManager
        sessionManager = new UserSessionManager(this);

        // Initialize UI elements using ViewBinding
        chatLayout = binding.chatLayout;
        inputEditText = binding.inputEditText;
        sendIcon = binding.sendIcon;

        // Setup click listeners
        sendIcon.setOnClickListener(v -> sendMessage());

        // Check if user is logged in
        if (!sessionManager.isLoggedIn()) {
            // Redirect to login if not logged in
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        // Set welcome message with user's name
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.setTitle("Welcome, " + currentUser.getDisplayName());
            }
        }

        // Register image picker launcher
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        if (imageUri != null) {
                            handleImage(imageUri);
                        }
                    }
                }
        );
    }

    private void sendMessage() {
        String message = inputEditText.getText().toString().trim();
        if (!message.isEmpty()) {
            // Add user message to chat
            addMessageToChat(message, true);

            // Clear input field
            inputEditText.setText("");

            // Process the message with the API
            processTextQuery(message);
        }
    }

    private void addMessageToChat(String message, boolean isUser) {
        TextView messageBubble = new TextView(this);
        messageBubble.setText(message);
        messageBubble.setPadding(36, 36, 36, 36);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, 24); // bottom margin

        if (isUser) {
            messageBubble.setBackgroundResource(R.drawable.bg_bubble_right);
            messageBubble.setTextColor(getResources().getColor(android.R.color.white));
            params.gravity = android.view.Gravity.END;
        } else {
            messageBubble.setBackgroundResource(R.drawable.bg_bubble_left);
            messageBubble.setTextColor(getResources().getColor(android.R.color.black));
            params.gravity = android.view.Gravity.START;
        }

        messageBubble.setLayoutParams(params);
        chatLayout.addView(messageBubble);

        // Scroll to bottom
        binding.chatScrollView.post(() -> binding.chatScrollView.fullScroll(View.FOCUS_DOWN));
    }

    private void processTextQuery(String query) {
        var client = new RecipeApiClient(API_ENDPOINT);

        new Thread(() -> {
            try {
                var req = new RecipeByChatRequest(query);
                var res = client.createRecipe(req);
                var generatedRecipes = res.getGenerated();

                // Add response to chat
                runOnUiThread(() -> addMessageToChat(generatedRecipes, false));
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() ->
                        addMessageToChat("Sorry, I couldn't process your request: " + e.getMessage(), false)
                );
            }
        }).start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            // Show confirmation dialog
            new AlertDialog.Builder(this)
                    .setTitle("Logout")
                    .setMessage("Are you sure you want to logout?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        sessionManager.logoutUser();
                    })
                    .setNegativeButton("No", null)
                    .show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onSelectPhoto(View v) {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

    private void handleImage(Uri imageUri) {
        var client = new RecipeApiClient(API_ENDPOINT);

        // Add a loading message
        runOnUiThread(() -> addMessageToChat("Processing your image...", false));

        new Thread(() -> {
            try {
                var inputStream = getContentResolver().openInputStream(imageUri);
                var outputStream = new ByteArrayOutputStream();
                var buffer = new byte[1024];
                int bytesRead;

                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }

                inputStream.close();

                var imageBytes = outputStream.toByteArray();
                var base64Str = Base64.encodeToString(imageBytes, Base64.NO_WRAP);

                var req = new RecipeByImageRequest(base64Str);
                var res = client.createRecipe(req);

                var generatedRecipes = res.getGenerated();

                // Add response to chat
                runOnUiThread(() -> addMessageToChat(generatedRecipes, false));
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() ->
                        addMessageToChat("Sorry, I couldn't process your image: " + e.getMessage(), false)
                );
            }
        }).start();
    }
}