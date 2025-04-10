package com.mobiledev.recipeit;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.mobiledev.recipeit.Helpers.RecipeApiClient;
import com.mobiledev.recipeit.Models.RecipeRequest;
import com.mobiledev.recipeit.Models.RecipeResponse;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class MainActivity extends AppCompatActivity {
    private static final String API_ENDPOINT = "http://10.0.2.2:4000/api/recipe/create";

    private TextView resultTextView;
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

    public void onSelectPhoto(View v){
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

    private void handleImage(Uri imageUri) {
        var client = new RecipeApiClient(API_ENDPOINT);

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

                var req = new RecipeRequest(base64Str);
                var res = client.uploadRecipe(req);

                var generatedRecipes = res.getGenerated();
                runOnUiThread(() -> resultTextView.setText(generatedRecipes));
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> resultTextView.setText("Error: " + e.getMessage()));
            }
        }).start();
    }
}