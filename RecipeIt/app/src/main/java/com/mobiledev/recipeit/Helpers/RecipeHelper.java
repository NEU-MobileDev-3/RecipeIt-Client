package com.mobiledev.recipeit.Helpers;

import java.util.Collection;

public class RecipeHelper {
    public static String getRecipeByChat(
            Collection<String> types,
            double maxCalories,
            int numberOfRecipes,
            String query) {
        StringBuilder sb = new StringBuilder();

        sb.append("I want to eat ");
        if (types != null && !types.isEmpty()) {
            sb.append("a ");
            sb.append(String.join(", ", types));
            sb.append(" ");
        }

        sb.append("recipe with ");
        if (maxCalories > 0) {
            sb.append("less than ");
            sb.append(maxCalories);
            sb.append(" calories");
        }

        if (numberOfRecipes > 0) {
            sb.append(" and ");
            sb.append(numberOfRecipes);
            sb.append(" recipes");
        }

        if (query != null && !query.isEmpty()) {
            sb.append(" that includes ");
            sb.append(query);
        }

        sb.append(".");

        return sb.toString();
    }
}
