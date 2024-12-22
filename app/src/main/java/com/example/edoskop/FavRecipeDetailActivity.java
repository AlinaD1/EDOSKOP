package com.example.edoskop;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class FavRecipeDetailActivity extends AppCompatActivity {

    private TextView recipeNameTextView;
    private TextView ingredientsTextView;
    private TextView descriptionTextView;
    private Button deleteFromFavoritesButton;
    private String recipeId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_details_fav_recipes);

        recipeNameTextView = findViewById(R.id.recipeNameDetail);
        ingredientsTextView = findViewById(R.id.ingredientsTextView);
        descriptionTextView = findViewById(R.id.descriptionTextView);
        deleteFromFavoritesButton = findViewById(R.id.DeleteFromFavoritesButton);

        // Получаем ID рецепта, переданное в Intent
        recipeId = getIntent().getStringExtra("recipeId");

        if (recipeId != null) {
            loadRecipeDetails(recipeId);  // Загружаем детали рецепта
        } else {
            Toast.makeText(this, "Ошибка: ID рецепта не найдено", Toast.LENGTH_SHORT).show();
        }

        // Убираем рецепт из избранного
        deleteFromFavoritesButton.setOnClickListener(v -> removeFromFavorites());
    }

    private void loadRecipeDetails(String recipeId) {
        DatabaseReference recipeRef = FirebaseDatabase.getInstance().getReference("CommonRecipes").child(recipeId);

        recipeRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Recipe recipe = snapshot.getValue(Recipe.class); // Получаем объект рецепта
                    if (recipe != null) {
                        // Заполняем TextView данными рецепта
                        recipeNameTextView.setText(recipe.getName());
                        ingredientsTextView.setText(formatIngredients(recipe.getIngredients()));
                        descriptionTextView.setText(recipe.getDescription());
                    }
                } else {
                    Toast.makeText(FavRecipeDetailActivity.this, "Рецепт не найден", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(FavRecipeDetailActivity.this, "Ошибка загрузки рецепта", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String formatIngredients(List<String> ingredients) {
        if (ingredients == null || ingredients.isEmpty()) {
            return "Ингредиенты не указаны";
        }
        StringBuilder ingredientsText = new StringBuilder();
        for (String ingredient : ingredients) {
            ingredientsText.append(ingredient).append("\n");
        }
        return ingredientsText.toString();
    }

    private void removeFromFavorites() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if (userId == null) {
            Toast.makeText(this, "Пользователь не найден", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference favoritesRef = FirebaseDatabase.getInstance().getReference("Favorites").child(userId);

        favoritesRef.child(recipeId).removeValue()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(FavRecipeDetailActivity.this, "Рецепт удален из избранного", Toast.LENGTH_SHORT).show();
                    finish();  // Закрываем текущую активность и возвращаемся в FavoritesActivity
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(FavRecipeDetailActivity.this, "Ошибка при удалении рецепта", Toast.LENGTH_SHORT).show();
                });
    }
}
