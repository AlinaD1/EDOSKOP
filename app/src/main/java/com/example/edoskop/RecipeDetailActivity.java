package com.example.edoskop;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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
    private DatabaseReference favoritesRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        TextView recipeName = findViewById(R.id.recipeNameDetail);
        TextView ingredients = findViewById(R.id.ingredientsTextView);
        TextView description = findViewById(R.id.descriptionTextView);
        Button addToFavoritesButton = findViewById(R.id.addToFavoritesButton);

        recipeId = getIntent().getStringExtra("recipeId");
        boolean fromFavorites = getIntent().getBooleanExtra("fromFavorites", false);

        if (recipeId == null || recipeId.isEmpty()) {
            Toast.makeText(this, "Ошибка: ID рецепта отсутствует", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Убираем кнопку "Добавить в избранное", если вызов из FavoritesActivity
        if (fromFavorites) {
            addToFavoritesButton.setVisibility(View.GONE);
        }

        recipeRef = FirebaseDatabase.getInstance().getReference("CommonRecipes").child(recipeId);
        favoritesRef = FirebaseDatabase.getInstance().getReference("Favorites");

        loadRecipeDetails(recipeName, ingredients, description);

        if (!fromFavorites) {
            addToFavoritesButton.setOnClickListener(v -> addToFavorites());
        }
    }


    private void loadRecipeDetails(TextView recipeName, TextView ingredients, TextView description) {
        recipeRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Recipe recipe = snapshot.getValue(Recipe.class);
                if (recipe != null) {
                    recipeName.setText(recipe.getName());
                    description.setText(recipe.getDescription());
                    List<String> ingredientsList = recipe.getIngredients();
                    ingredients.setText(ingredientsList != null ? String.join("\n", ingredientsList) : "Ингредиенты отсутствуют");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(RecipeDetailActivity.this, "Ошибка загрузки рецепта", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addToFavorites() {
        favoritesRef.child(recipeId).setValue(true).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Рецепт добавлен в избранное", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Ошибка добавления в избранное", Toast.LENGTH_SHORT).show();
            }
        });
    }
}


