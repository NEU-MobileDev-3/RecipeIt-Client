package com.mobiledev.recipeit.Helpers;

import com.mobiledev.recipeit.Models.RecipeRequest;
import com.mobiledev.recipeit.Models.RecipeResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class RecipeApiClient {
    private final String apiUrl;

    public RecipeApiClient(String apiUrl) {
        this.apiUrl = apiUrl;
    }

    public RecipeResponse uploadRecipe(RecipeRequest req) throws IOException {
        var gson = new com.google.gson.Gson();
        var json = gson.toJson(req);

        var url = new URL(this.apiUrl);
        var conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        conn.setDoOutput(true);

        var os = conn.getOutputStream();
        os.write(json.getBytes(StandardCharsets.UTF_8));
        os.flush();
        os.close();

        var responseStream = conn.getInputStream();
        var reader = new BufferedReader(new InputStreamReader(responseStream));
        var responseBuilder = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {
            responseBuilder.append(line);
        }

        reader.close();
        conn.disconnect();

        var responseText = responseBuilder.toString();

        return gson.fromJson(responseText, RecipeResponse.class);
    }
}
