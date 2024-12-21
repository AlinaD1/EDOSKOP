package com.example.edoskop;

import java.io.Serializable;
import java.util.List;

public class Recipe implements Serializable {
    private String id;
    private String name;
    private List<String> ingredients; // Список ингредиентов
    private String description;

    public Recipe() {}

    public Recipe(String id, String name, List<String> ingredients, String description) {
        this.id = id;
        this.name = name;
        this.ingredients = ingredients;
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id; // Устанавливаем значение ID
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name; // Устанавливаем значение имени
    }

    public List<String> getIngredients() {
        return ingredients;
    }

    public void setIngredients(List<String> ingredients) {
        this.ingredients = ingredients; // Устанавливаем список ингредиентов
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description; // Устанавливаем описание
    }
}



