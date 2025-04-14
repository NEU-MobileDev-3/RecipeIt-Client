package com.mobiledev.recipeit;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import android.widget.SeekBar;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.activity.EdgeToEdge;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.gson.Gson;
import com.mobiledev.recipeit.Adapters.ChatHistoryAdapter;
import com.mobiledev.recipeit.Helpers.DialogHelper;
import com.mobiledev.recipeit.Helpers.RecipeApiClient;
import com.mobiledev.recipeit.Helpers.RecipeHelper;
import com.mobiledev.recipeit.Helpers.UserSessionManager;
import com.mobiledev.recipeit.Models.ChatHistory;
import com.mobiledev.recipeit.Models.RecipeByChatRequest;
import com.mobiledev.recipeit.Models.RecipeByImageRequest;
import com.mobiledev.recipeit.databinding.ActivityMainBinding;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String API_ENDPOINT = "http://10.0.2.2:4000/api";

    private ActivityMainBinding binding;
    private UserSessionManager sessionManager;
    private final List<ChatHistory> chatHistories = new ArrayList<>(
            List.of(
                    ChatHistory.Server("Hello! I am your recipe assistant. How can I help you today?"),
                    ChatHistory.Server("Please upload an image of the ingredients or type your request.")
            )
    );

    private ChatHistoryAdapter chatHistoryAdapter;
    private RecyclerView chatHistoryView;
    private EditText inputEditText;
    private ImageView sendIcon;
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    private ToggleButton veganToggle, glutenFreeToggle, dairyFreeToggle;
    private SeekBar calorieSeekBar, recipeCountSeekBar;

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
        var auth = FirebaseAuth.getInstance();
        var currentUser = auth.getCurrentUser();

        // Check if user is logged in
        if (currentUser == null) {
            // Redirect to login if not logged in
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        // Set welcome message with user's name
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Welcome, " + currentUser.getDisplayName());
        }

        // Initialize UI elements using ViewBinding
        chatHistoryView = findViewById(R.id.chatHistoryView);
        inputEditText = binding.inputEditText;
        sendIcon = binding.sendIcon;

        // Initialize chat adapter and recycler view
        chatHistoryAdapter = new ChatHistoryAdapter(this, chatHistories);
        chatHistoryView.setAdapter(chatHistoryAdapter);
        chatHistoryView.setLayoutManager(new LinearLayoutManager(this));

        // Setup click listeners
        sendIcon.setOnClickListener(v -> submitByChat(v));

        // Register image picker launcher
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        if (imageUri != null) {
                            try {
                                handleImage(imageUri);
                            } catch (FileNotFoundException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                }
        );

        // Food Preferences Options
        veganToggle = findViewById(R.id.veganToggle);
        glutenFreeToggle = findViewById(R.id.glutenFreeToggle);
        dairyFreeToggle = findViewById(R.id.dairyFreeToggle);

        calorieSeekBar = findViewById(R.id.calorieSeekBar);
        recipeCountSeekBar = findViewById(R.id.recipeCountSeekBar);

        // Set up Bottom navigation menu
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                Toast.makeText(this, "Home selected", Toast.LENGTH_SHORT).show();
                return true;
            } else if (id == R.id.nav_favorites) {
                Toast.makeText(this, "Favorites selected", Toast.LENGTH_SHORT).show();
                return true;
            } else if (id == R.id.nav_profile) {
                Intent intent = new Intent(this, ProfileActivity.class);
                startActivity(intent);
            }

            return false;
        });
    }

    public void submitByChat(View v) {
        String content = inputEditText.getText().toString().trim();
        if (content.isEmpty()) {
            return;
        }

        // Clear input field
        inputEditText.setText("");

        // Add user message to chat history
        chatHistories.add(ChatHistory.User(content));
        chatHistoryAdapter.notifyItemInserted(chatHistories.size() - 1);
        scrollToBottom();

        // Collect selected types
        List<String> types = new ArrayList<>();
        if (veganToggle.isChecked()) types.add("vegan");
        if (glutenFreeToggle.isChecked()) types.add("gluten free");
        if (dairyFreeToggle.isChecked()) types.add("dairy free");

        double maxCalories = calorieSeekBar.getProgress();
        int numberOfRecipes = recipeCountSeekBar.getProgress();
        String recipeRequest = RecipeHelper.getRecipeByChat(types, maxCalories, numberOfRecipes, content);

        var req = new RecipeByChatRequest(recipeRequest);
        performRequest(req);
    }

    private void scrollToBottom() {
        if (chatHistoryView != null && chatHistoryAdapter != null && chatHistoryAdapter.getItemCount() > 0) {
            chatHistoryView.smoothScrollToPosition(chatHistoryAdapter.getItemCount() - 1);
        }
    }

    public void onSelectPhoto(View v) {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

    private void handleImage(Uri imageUri) throws FileNotFoundException {
        // Add a loading message
        chatHistories.add(ChatHistory.Server("Processing your image..."));
        chatHistoryAdapter.notifyItemInserted(chatHistories.size() - 1);
        scrollToBottom();

        new Thread(() -> {
            try {
                InputStream inputStream = getContentResolver().openInputStream(imageUri);
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int bytesRead;

                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }

                inputStream.close();

                byte[] imageBytes = outputStream.toByteArray();
                String base64Str = Base64.encodeToString(imageBytes, Base64.NO_WRAP);

                var req = new RecipeByImageRequest(base64Str);
                performRequest(req);
            } catch (IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    chatHistories.add(ChatHistory.Server("Sorry, I couldn't process your image: " + e.getMessage()));
                    chatHistoryAdapter.notifyItemInserted(chatHistories.size() - 1);
                    scrollToBottom();
                });
            }
        }).start();
    }

    private <TReq> void performRequest(TReq req) {
        var client = new RecipeApiClient(API_ENDPOINT);

        new Thread(() -> {
            try {
                var res = client.createRecipe(req);
                var generatedRecipes = res.getGenerated();

                runOnUiThread(() -> {
                    chatHistories.add(ChatHistory.Server(generatedRecipes));
                    chatHistoryAdapter.notifyItemInserted(chatHistories.size() - 1);
                    scrollToBottom();
                    saveHistory();
                });
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    // Show error dialog
                    var errorMessage = "Error: " + e.getMessage();
                    DialogHelper.showErrorDialog(this, "Failed to request recipe", errorMessage);

                    // Also add error to chat
                    chatHistories.add(ChatHistory.Server("Sorry, I couldn't process your request: " + e.getMessage()));
                    chatHistoryAdapter.notifyItemInserted(chatHistories.size() - 1);
                    scrollToBottom();
                });
            }
        }).start();
    }

    private void saveHistory() {
        // Save chat history to a database or file
        // This is a placeholder for the actual implementation
        var trimmedHistories = chatHistories.stream().skip(2);
        var json = new Gson().toJson(trimmedHistories);
        // TODO: Implement actual saving logic
    }

    private void loadHistory() {
        // Load chat history from a database or file
        // This is a placeholder for the actual implementation
        // TODO: Implement actual loading logic
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

    public void onAboutClick(View view) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("About RecipeIt")
                .setMessage("📸 Snap, 🍳 Cook, 😋 Enjoy – Smart Recipes in Seconds!\n\n" +
                        "RecipeIt helps you turn ingredients into creative meals using AI.\n\n" +
                        "➤ Take or upload a photo of your ingredients.\n" +
                        "➤ Get recipe ideas instantly using GenAI.\n" +
                        "➤ Chat with your assistant for more meal suggestions.")
                .setPositiveButton("OK", null)
                .show();
    }
}