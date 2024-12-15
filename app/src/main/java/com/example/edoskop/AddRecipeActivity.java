package com.example.edoskop;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class AddRecipeActivity extends AppCompatActivity {

    private EditText recipeNameInput, descriptionInput;
    private LinearLayout ingredientsContainer;
    private Button addIngredientButton, saveButton;

    private DatabaseReference recipesDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_recipe);

        // Инициализация UI-элементов
        recipeNameInput = findViewById(R.id.recipeNameInput);
        descriptionInput = findViewById(R.id.descriptionInput);
        ingredientsContainer = findViewById(R.id.ingredientsContainer);
        addIngredientButton = findViewById(R.id.addIngredientButton);
        saveButton = findViewById(R.id.saveButton);

        // Инициализация Firebase Database
        recipesDatabase = FirebaseDatabase.getInstance().getReference("Recipes");

        // Добавление первого поля для ингредиентов
        addIngredientField();

        // Обработчик для кнопки добавления ингредиентов
        addIngredientButton.setOnClickListener(v -> addIngredientField());

        // Обработчик для кнопки сохранения
        saveButton.setOnClickListener(v -> saveRecipe());
    }

    private void addIngredientField() {
        // Создаем новое текстовое поле для ингредиента
        EditText ingredientInput = new EditText(this);
        ingredientInput.setHint("Ингредиент");
        ingredientInput.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        ingredientsContainer.addView(ingredientInput); // Добавляем поле в контейнер
    }

    private void saveRecipe() {
        // Получаем данные из полей
        String recipeName = recipeNameInput.getText().toString();
        String description = descriptionInput.getText().toString();
        List<String> ingredients = new ArrayList<>();

        // Считываем все ингредиенты из контейнера
        for (int i = 0; i < ingredientsContainer.getChildCount(); i++) {
            View view = ingredientsContainer.getChildAt(i);
            if (view instanceof EditText) {
                String ingredient = ((EditText) view).getText().toString();
                if (!TextUtils.isEmpty(ingredient)) {
                    ingredients.add(ingredient);
                }
            }
        }

        // Проверка на заполненность полей
        if (TextUtils.isEmpty(recipeName) || TextUtils.isEmpty(description) || ingredients.isEmpty()) {
            Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show();
            return;
        }

        // Генерация уникального ID для рецепта
        String recipeId = recipesDatabase.push().getKey();
        if (recipeId != null) {
            // Сохранение рецепта в Firebase
            Recipe recipe = new Recipe(recipeId, recipeName, ingredients, description);
            recipesDatabase.child(recipeId).setValue(recipe).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(AddRecipeActivity.this, "Рецепт сохранён", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(AddRecipeActivity.this, "Ошибка сохранения", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}

