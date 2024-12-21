package com.example.edoskop;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class RecipeResultsActivity extends AppCompatActivity {
    private RecyclerView recipeListView;
    private List<Recipe> matchingRecipes;
    private RecipeAdapter recipeAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_results);

        recipeListView = findViewById(R.id.recipeListView);
        recipeListView.setLayoutManager(new LinearLayoutManager(this));

        matchingRecipes = new ArrayList<>();
        recipeAdapter = new RecipeAdapter(this, matchingRecipes, this::openRecipeDetail);
        recipeListView.setAdapter(recipeAdapter);

        List<String> recognizedItems = getIntent().getStringArrayListExtra("recognizedItems");
        if (recognizedItems != null && !recognizedItems.isEmpty()) {
            searchRecipesInFirebase(recognizedItems);
        } else {
            Toast.makeText(this, "Ингредиенты не распознаны", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void searchRecipesInFirebase(List<String> ingredients) {
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("CommonRecipes");
        databaseRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DataSnapshot snapshot = task.getResult();
                if (snapshot.exists()) {
                    matchingRecipes.clear();
                    for (DataSnapshot recipeSnapshot : snapshot.getChildren()) {
                        Recipe recipe = recipeSnapshot.getValue(Recipe.class);
                        if (recipe != null && ingredients.containsAll(recipe.getIngredients())) {
                            matchingRecipes.add(recipe);
                        }
                    }

                    if (matchingRecipes.isEmpty()) {
                        Toast.makeText(this, "Нет рецептов, соответствующих всем ингредиентам", Toast.LENGTH_SHORT).show();
                    }

                    recipeAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(this, "Нет данных о рецептах", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Ошибка подключения к Firebase", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openRecipeDetail(Recipe recipe) {
        Intent intent = new Intent(this, RecipeDetailActivity.class);
        intent.putExtra("recipeId", recipe.getId());
        startActivity(intent);
    }
}
