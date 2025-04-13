package com.mobiledev.recipeit.Helpers;

import com.mobiledev.recipeit.Models.RecipeByChatRequest;
import com.mobiledev.recipeit.Models.RecipeByImageRequest;
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

    private <TReq> RecipeResponse createRecipeImpl(TReq req, String endpoint) throws IOException {
        var gson = new com.google.gson.Gson();
        var json = gson.toJson(req);

        var url = new URL(String.format("%s%s", this.apiUrl, endpoint));
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

    public <TReq> RecipeResponse createRecipe(TReq req) throws IOException {

        if (req instanceof RecipeByImageRequest)
            return createRecipeImpl(req, "/recipe/create/image");

        if (req instanceof RecipeByChatRequest)
            return createRecipeImpl(req, "/recipe/create/text");

        throw new NullPointerException();
    }
}
