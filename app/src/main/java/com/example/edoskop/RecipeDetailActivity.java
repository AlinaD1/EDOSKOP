package com.example.edoskop;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.List;


public class RecipeDetailActivity extends AppCompatActivity {

    private String recipeId;
    private DatabaseReference recipeRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        TextView recipeName = findViewById(R.id.recipeNameDetail);
        TextView ingredients = findViewById(R.id.ingredientsDetail);
        TextView description = findViewById(R.id.descriptionDetail);

        Button editRecipeButton = findViewById(R.id.editRecipeButton);
        Button deleteRecipeButton = findViewById(R.id.deleteRecipeButton);

        // Получаем ID рецепта из Intent
        recipeId = getIntent().getStringExtra("recipeId");
        Log.d("RecipeDetailActivity", "Recipe ID: " + recipeId);

        recipeRef = FirebaseDatabase.getInstance().getReference("Recipes").child(recipeId);

        // Загрузка данных рецепта
        recipeRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Recipe recipe = snapshot.getValue(Recipe.class);
                if (recipe != null) {
                    recipeName.setText(recipe.getName());
                    description.setText(recipe.getDescription());

                    // Преобразование списка ингредиентов в строку
                    List<String> ingredientsList = recipe.getIngredients();
                    if (ingredientsList != null) {
                        ingredients.setText(TextUtils.join("\n", ingredientsList)); // Отображаем каждый ингредиент на новой строке
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(RecipeDetailActivity.this, "Ошибка загрузки", Toast.LENGTH_SHORT).show();
            }
        });


        // Установка обработчиков событий кнопок
        editRecipeButton.setOnClickListener(v -> editRecipe());
        deleteRecipeButton.setOnClickListener(v -> deleteRecipe());
    }

    private void editRecipe() {
        Log.d("RecipeDetailActivity", "Edit button clicked");
        Intent intent = new Intent(RecipeDetailActivity.this, EditRecipeActivity.class);
        intent.putExtra("recipeId", recipeId);
        startActivity(intent);
    }

    private void deleteRecipe() {
        Log.d("RecipeDetailActivity", "Delete button clicked");
        recipeRef.removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(RecipeDetailActivity.this, "Рецепт удален", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(RecipeDetailActivity.this, "Ошибка удаления", Toast.LENGTH_SHORT).show();
                Log.e("RecipeDetailActivity", "Ошибка удаления рецепта");
            }
        });
    }
}

