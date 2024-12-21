package com.example.edoskop;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class AddRecipeActivity extends AppCompatActivity {

    private EditText recipeNameInput, descriptionInput;
    private LinearLayout ingredientsContainer;
    private Button addIngredientButton, saveButton, returnToRecipesButton;

    private DatabaseReference recipesDatabase;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_recipe);

        recipeNameInput = findViewById(R.id.recipeNameInput);
        descriptionInput = findViewById(R.id.descriptionInput);
        ingredientsContainer = findViewById(R.id.ingredientsContainer);
        addIngredientButton = findViewById(R.id.addIngredientButton);
        saveButton = findViewById(R.id.saveButton);
        returnToRecipesButton = findViewById(R.id.returnToRecipesButton);

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        recipesDatabase = FirebaseDatabase.getInstance().getReference("Recipes").child(userId);

        addIngredientButton.setOnClickListener(v -> addIngredientField());
        saveButton.setOnClickListener(v -> saveRecipe());
        returnToRecipesButton.setOnClickListener(v -> onBackPressed());
    }

    private void addIngredientField() {
        EditText ingredientInput = new EditText(this);
        ingredientInput.setHint("Ингредиент");
        ingredientInput.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        ingredientsContainer.addView(ingredientInput);
    }

    private void saveRecipe() {
        String recipeName = recipeNameInput.getText().toString();
        String description = descriptionInput.getText().toString();
        List<String> ingredients = new ArrayList<>();

        for (int i = 0; i < ingredientsContainer.getChildCount(); i++) {
            View view = ingredientsContainer.getChildAt(i);
            if (view instanceof EditText) {
                String ingredient = ((EditText) view).getText().toString();
                if (!ingredient.isEmpty()) {
                    ingredients.add(ingredient);
                }
            }
        }

        if (recipeName.isEmpty() || description.isEmpty() || ingredients.isEmpty()) {
            Toast.makeText(this, "Все поля должны быть заполнены", Toast.LENGTH_SHORT).show();
            return;
        }

        String recipeId = recipesDatabase.push().getKey();
        if (recipeId != null) {
            UserRecipe recipe = new UserRecipe(recipeId, recipeName, description, ingredients);
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

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle("Вы уверены?")
                .setMessage("Все несохранённые изменения будут потеряны.")
                .setPositiveButton("Выйти", (dialog, which) -> finish())
                .setNegativeButton("Отмена", null)
                .show();
    }
}





