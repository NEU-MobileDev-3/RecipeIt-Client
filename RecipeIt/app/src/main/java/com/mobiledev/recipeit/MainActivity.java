package com.mobiledev.recipeit;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.Manifest;

import androidx.activity.EdgeToEdge;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.mobiledev.recipeit.Adapters.ChatHistoryAdapter;
import com.mobiledev.recipeit.Fragments.FavoritesFragment;
import com.mobiledev.recipeit.Fragments.ProfileFragment;
import com.mobiledev.recipeit.Helpers.ChatHistoryManager;
import com.mobiledev.recipeit.Helpers.DialogHelper;
import com.mobiledev.recipeit.Helpers.RecipeApiClient;
import com.mobiledev.recipeit.Helpers.RecipeHelper;
import com.mobiledev.recipeit.Models.ChatHistory;
import com.mobiledev.recipeit.Models.RecipeByChatRequest;
import com.mobiledev.recipeit.Models.RecipeByImageRequest;
import com.mobiledev.recipeit.databinding.ActivityMainBinding;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final String API_ENDPOINT = "https://recipeit-server.onrender.com/api";

    private ActivityMainBinding binding;
    private FirebaseAuth auth;
    private ChatHistoryManager historyManager;
    private final List<ChatHistory> chatHistories = new ArrayList<>();
    private final List<ChatHistory> welcomeMessages = List.of(
            ChatHistory.Server("Hello! I am your recipe assistant. How can I help you today?"),
            ChatHistory.Server("Please upload an image of the ingredients or type your request.")
    );

    private ChatHistoryAdapter chatHistoryAdapter;
    private RecyclerView chatHistoryView;
    private EditText inputEditText;
    private ImageView sendIcon;
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private ActivityResultLauncher<Intent> cameraLauncher;

    private  ActivityResultLauncher<String> cameraPermissionLauncher;

    private ToggleButton veganToggle, glutenFreeToggle, dairyFreeToggle;
    private SeekBar calorieSeekBar, recipeCountSeekBar;
    private Fragment currentFragment = null;

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

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();

        // Check if user is logged in
        if (currentUser == null) {
            // Redirect to login if not logged in
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        // Initialize history manager
        historyManager = new ChatHistoryManager(this, currentUser);

        // Set welcome message with user's name
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Welcome, " + currentUser.getDisplayName());
        }

        // Initialize UI elements using ViewBinding
        chatHistoryView = findViewById(R.id.chatHistoryView);
        inputEditText = binding.inputEditText;
        sendIcon = binding.sendIcon;

        // Load chat history
        loadHistory();

        // Initialize chat adapter and recycler view
        chatHistoryAdapter = new ChatHistoryAdapter(this, chatHistories);
        chatHistoryView.setAdapter(chatHistoryAdapter);
        chatHistoryView.setLayoutManager(new LinearLayoutManager(this));
        
        // Setup favorite click handler
        chatHistoryAdapter.setOnFavoriteClickListener((position, isFavorite) -> {
            // Update favorite status in manager
            historyManager.updateItemFavoriteStatus(position, chatHistories, isFavorite);
        });

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

        //Register for Taking Photo (Camera Launcher)
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Bitmap photo = (Bitmap) result.getData().getExtras().get("data");
                        Uri photoUri;
                        try {
                            photoUri = saveBitmapToUri(photo);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        try {
                            handleImage(photoUri);
                        } catch (FileNotFoundException e) {
                            throw new RuntimeException(e);
                        }

                    }
                }
        );

        //Checking if the permission is available, if it is then the camera opens, otherwise the message is shown.
        cameraPermissionLauncher =
                registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                    if (isGranted) {
                        openCamera(); // method that launches the camera intent
                    } else {
                        Toast.makeText(this, "Camera permission is required", Toast.LENGTH_SHORT).show();
                    }
                });


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
                showHomeScreen();
                return true;
            } else if (id == R.id.nav_favorites) {
                showFavoritesScreen();
                return true;
            } else if (id == R.id.nav_profile) {
                showProfileScreen();
                return true;
            }

            return false;
        });
    }

    private void showHomeScreen() {
        // Remove any active fragments and show main chat UI
        if (currentFragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .remove(currentFragment)
                    .commit();
            currentFragment = null;
        }

        // Show main chat UI
        binding.mainChatLayout.setVisibility(View.VISIBLE);
    }

    private void showFavoritesScreen() {
        // Hide main chat UI
        binding.mainChatLayout.setVisibility(View.GONE);

        // Show favorites fragment
        var favoritesFragment = new FavoritesFragment();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, favoritesFragment)
                .commit();
        currentFragment = favoritesFragment;
    }

    private void showProfileScreen() {
        // Hide main chat UI
        binding.mainChatLayout.setVisibility(View.GONE);

        // Show favorites fragment
        var profileFragment = new ProfileFragment();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, profileFragment)
                .commit();
        currentFragment = profileFragment;
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

    public void showImagePickerDialog(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose Option");

        String[] options = {"Take Picture", "Choose from Gallery"};
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                takePhoto(); // Camera
            } else if (which == 1) {
                onSelectPhoto(view); // Gallery
            }
        });

        builder.show();
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }

    public void takePhoto() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            openCamera();
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private Uri saveBitmapToUri(Bitmap bitmap) throws IOException {
        File file = new File(getCacheDir(), "photo.jpg");
        FileOutputStream out = new FileOutputStream(file);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
        out.flush();
        out.close();

        return FileProvider.getUriForFile(this, getPackageName() + ".provider", file);
    }

    public void onSelectPhoto(View v) {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

    public void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraLauncher.launch(intent);
    }

    private void handleImage(Uri imageUri) throws FileNotFoundException {
        // Add a loading message
        var loadingMessage = ChatHistory.Server("Processing your image...");
        loadingMessage.setLoading(true);

        chatHistories.add(loadingMessage);
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
                    saveHistory();
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
                    chatHistories.get(chatHistories.size() - 1).setLoading(false);
                    chatHistoryAdapter.notifyItemChanged(chatHistories.size() - 1);

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

                    chatHistories.get(chatHistories.size() - 1).setLoading(false);
                    chatHistoryAdapter.notifyItemChanged(chatHistories.size() - 1);

                    // Also add error to chat
                    chatHistories.add(ChatHistory.Server("Sorry, I couldn't process your request: " + e.getMessage()));
                    chatHistoryAdapter.notifyItemInserted(chatHistories.size() - 1);
                    scrollToBottom();
                    saveHistory();
                });
            }
        }).start();
    }

    private void saveHistory() {
        try {
            // Skip welcome messages when saving
            historyManager.saveHistory(chatHistories);
            
            // Save favorite messages separately
            List<ChatHistory> favorites = new ArrayList<>();

            for (ChatHistory chatHistory : chatHistories) {
                if (chatHistory.isFavorite()) {
                    favorites.add(chatHistory);
                }
            }

            historyManager.saveFavorites(new ArrayList<>(favorites));
            
            Log.d(TAG, "Chat history saved successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error saving chat history", e);
        }
    }

    private void loadHistory() {
        try {
            // Clear current list
            chatHistories.clear();
            
            // Load history from storage
            List<ChatHistory> savedHistory = historyManager.loadHistory();
            
            // If we have saved history, use it
            if (!savedHistory.isEmpty()) {
                chatHistories.addAll(savedHistory);
            } else {
                // Otherwise add welcome messages
                chatHistories.addAll(welcomeMessages);
            }
            
            Log.d(TAG, "Loaded " + chatHistories.size() + " chat history items");
        } catch (Exception e) {
            Log.e(TAG, "Error loading chat history", e);
            // Fallback to welcome messages
            chatHistories.clear();
            chatHistories.addAll(welcomeMessages);
        }
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
                        auth.signOut();
                        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                    })
                    .setNegativeButton("No", null)
                    .show();
            return true;
        } else if (item.getItemId() == R.id.action_clear_history) {
            // Show confirmation dialog for clearing chat history
            new AlertDialog.Builder(this)
                    .setTitle("Clear History")
                    .setMessage("Are you sure you want to clear all chat history?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        chatHistories.clear();
                        chatHistories.addAll(welcomeMessages);
                        chatHistoryAdapter.notifyDataSetChanged();
                        historyManager.clearAllHistory();
                        Toast.makeText(this, "Chat history cleared", Toast.LENGTH_SHORT).show();
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