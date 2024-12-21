package com.example.edoskop;

import java.util.List;

public class UserRecipe {
    private String id;
    private String name;
    private String description;
    private List<String> ingredients;

    public UserRecipe() {}

    public UserRecipe(String id, String name, String description, List<String> ingredients) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.ingredients = ingredients;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getIngredients() {
        return ingredients;
    }
}

