package com.mobiledev.recipeit.Models;

public class RecipeByImageRequest {
    public RecipeByImageRequest(String imgBase64Str) {
        this.imgBase64Str = imgBase64Str;
    }

    private final String imgBase64Str;

    public String getImgBase64Str() {
        return imgBase64Str;
    }
}
