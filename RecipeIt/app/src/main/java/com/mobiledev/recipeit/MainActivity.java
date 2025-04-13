package com.mobiledev.recipeit;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.mobiledev.recipeit.Helpers.RecipeApiClient;
import com.mobiledev.recipeit.Models.RecipeByChatRequest;
import com.mobiledev.recipeit.Models.RecipeByImageRequest;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;

public class MainActivity extends AppCompatActivity {
    private static final String API_ENDPOINT = "http://10.0.2.2:4000/api";

    private TextView resultTextView;
    private EditText inputEditText;
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        resultTextView = findViewById(R.id.responseView);
        inputEditText = findViewById(R.id.inputEditText);

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
                Toast.makeText(this, "Profile selected", Toast.LENGTH_SHORT).show();
                return true;
            }

            return false;
        });
    }

    public void onSelectPhoto(View v){
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

    public void submitByChat(View v) {
        var content = inputEditText.getText().toString();
        inputEditText.setText("");

        var req = new RecipeByChatRequest(content);
        performRequest(req);
    }

    private <TReq> void performRequest(TReq req) {
        var client = new RecipeApiClient(API_ENDPOINT);

        new Thread(() -> {
            try {
                var res = client.createRecipe(req);
                var generatedRecipes = res.getGenerated();

                runOnUiThread(() -> resultTextView.setText(generatedRecipes));
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> resultTextView.setText("Error: " + e.getMessage()));
            }
        }).start();
    }

    private void handleImage(Uri imageUri) throws FileNotFoundException {
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

            performRequest(req);
        } catch (Exception e) {
            e.printStackTrace();
            runOnUiThread(() -> resultTextView.setText("Error: " + e.getMessage()));
        }

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