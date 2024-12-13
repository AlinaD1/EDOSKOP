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

public class EditRecipeActivity extends AppCompatActivity {

    private EditText editRecipeName, editDescription;
    private LinearLayout ingredientsContainer;
    private Button addIngredientButton, saveChangesButton;

    private String recipeId;
    private DatabaseReference recipeRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_recipe);

        // Инициализация элементов
        editRecipeName = findViewById(R.id.editRecipeName);
        editDescription = findViewById(R.id.editDescription);
        ingredientsContainer = findViewById(R.id.ingredientsContainer);
        addIngredientButton = findViewById(R.id.addIngredientButton);
        saveChangesButton = findViewById(R.id.saveChangesButton);

        // Получаем ID рецепта
        recipeId = getIntent().getStringExtra("recipeId");
        if (recipeId == null || recipeId.isEmpty()) {
            Toast.makeText(this, "ID рецепта не найден", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Получаем ссылку на рецепт в Firebase
        recipeRef = FirebaseDatabase.getInstance().getReference("Recipes").child(recipeId);

        // Загружаем текущие данные рецепта
        loadRecipeData();

        // Обработчик для добавления ингредиентов
        addIngredientButton.setOnClickListener(v -> addIngredientField());

        // Обработчик для сохранения изменений
        saveChangesButton.setOnClickListener(v -> saveChanges());
    }

    private void loadRecipeData() {
        recipeRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                Recipe recipe = task.getResult().getValue(Recipe.class);
                if (recipe != null) {
                    // Устанавливаем данные рецепта в поля
                    editRecipeName.setText(recipe.getName());
                    editDescription.setText(recipe.getDescription());

                    // Добавляем ингредиенты в контейнер
                    for (String ingredient : recipe.getIngredients()) {
                        addIngredientField(ingredient);
                    }
                }
            } else {
                Toast.makeText(this, "Ошибка загрузки данных", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addIngredientField(String ingredient) {
        // Создаем поле для ввода ингредиента
        EditText ingredientInput = new EditText(this);
        ingredientInput.setHint("Ингредиент");
        ingredientInput.setText(ingredient); // Устанавливаем текст ингредиента, если он передан
        ingredientInput.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        ingredientsContainer.addView(ingredientInput); // Добавляем поле в контейнер
    }

    private void addIngredientField() {
        // Добавляем пустое поле для нового ингредиента
        addIngredientField("");
    }

    private void saveChanges() {
        String newName = editRecipeName.getText().toString();
        String newDescription = editDescription.getText().toString();
        List<String> newIngredients = new ArrayList<>();

        // Считываем все ингредиенты из контейнера
        for (int i = 0; i < ingredientsContainer.getChildCount(); i++) {
            View view = ingredientsContainer.getChildAt(i);
            if (view instanceof EditText) {
                String ingredient = ((EditText) view).getText().toString();
                if (!TextUtils.isEmpty(ingredient)) {
                    newIngredients.add(ingredient);
                }
            }
        }

        // Проверяем, заполнены ли все поля
        if (TextUtils.isEmpty(newName) || TextUtils.isEmpty(newDescription) || newIngredients.isEmpty()) {
            Toast.makeText(this, "Все поля должны быть заполнены", Toast.LENGTH_SHORT).show();
            return;
        }

        // Обновляем данные рецепта
        Recipe updatedRecipe = new Recipe(recipeId, newName, newIngredients, newDescription);
        recipeRef.setValue(updatedRecipe).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(EditRecipeActivity.this, "Изменения сохранены", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(EditRecipeActivity.this, "Ошибка сохранения изменений", Toast.LENGTH_SHORT).show();
            }
        });
    }
}

