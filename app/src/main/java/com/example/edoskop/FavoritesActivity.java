package com.example.edoskop;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class FavoritesActivity extends AppCompatActivity {

    private RecyclerView favoritesListView;
    private RecipeAdapter recipeAdapter;
    private List<Recipe> favoriteRecipes = new ArrayList<>();
    private DatabaseReference favoritesRef;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        // Инициализация RecyclerView
        favoritesListView = findViewById(R.id.favoritesListView);
        favoritesListView.setLayoutManager(new LinearLayoutManager(this));

        recipeAdapter = new RecipeAdapter(this, favoriteRecipes, recipe -> {
            // Переход к FavRecipeDetailActivity
            Intent intent = new Intent(FavoritesActivity.this, FavRecipeDetailActivity.class);
            intent.putExtra("recipeId", recipe.getId());
            startActivity(intent);
        });

        favoritesListView.setAdapter(recipeAdapter);

        // Получение текущего пользователя
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        if (userId == null) {
            Toast.makeText(this, "Пользователь не найден", Toast.LENGTH_SHORT).show();
            return;
        }

        // Ссылка на узел избранного
        favoritesRef = FirebaseDatabase.getInstance().getReference("Favorites");

        loadFavoriteRecipes();  // Загрузка избранных рецептов
    }

    private void loadFavoriteRecipes() {
        // Очистка списка перед загрузкой
        favoriteRecipes.clear();

        favoritesRef.child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot recipeSnapshot : snapshot.getChildren()) {
                    String recipeId = recipeSnapshot.getKey();
                    if (recipeId != null) {
                        loadRecipeDetails(recipeId);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FavoritesActivity", "Ошибка загрузки избранного", error.toException());
                Toast.makeText(FavoritesActivity.this, "Ошибка загрузки данных", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadRecipeDetails(String recipeId) {
        DatabaseReference recipeRef = FirebaseDatabase.getInstance().getReference("CommonRecipes").child(recipeId);

        recipeRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Recipe recipe = snapshot.getValue(Recipe.class);
                if (recipe != null) {
                    recipe.setId(recipeId);  // Устанавливаем ID в объект рецепта

                    // Проверяем, нет ли уже такого рецепта в списке по ID
                    boolean isAlreadyAdded = false;
                    for (Recipe r : favoriteRecipes) {
                        if (r.getId().equals(recipe.getId())) {
                            isAlreadyAdded = true;
                            break;
                        }
                    }

                    // Если рецепт еще не добавлен в список, добавляем его
                    if (!isAlreadyAdded) {
                        favoriteRecipes.add(recipe);  // Добавляем рецепт в список
                        recipeAdapter.notifyDataSetChanged();  // Обновляем адаптер
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FavoritesActivity", "Ошибка загрузки рецепта", error.toException());
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadFavoriteRecipes();  // Перезагружаем список рецептов при возврате в активность
    }
}
