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
import java.util.List;

public class FavoritesActivity extends AppCompatActivity {
    private RecyclerView favoritesListView;
    private List<Recipe> favoriteRecipes;
    private RecipeAdapter recipeAdapter;
    private DatabaseReference favoritesRef;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        favoritesRef = FirebaseDatabase.getInstance().getReference("Favorites").child(userId);

        favoritesListView = findViewById(R.id.favoritesListView);
        favoritesListView.setLayoutManager(new LinearLayoutManager(this));

        favoriteRecipes = new ArrayList<>();
        recipeAdapter = new RecipeAdapter(this, favoriteRecipes, this::openRecipeDetail);
        favoritesListView.setAdapter(recipeAdapter);

        loadFavoriteRecipes();
    }

    private void loadFavoriteRecipes() {
        favoritesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                favoriteRecipes.clear();
                for (DataSnapshot recipeSnapshot : snapshot.getChildren()) {
                    String recipeId = recipeSnapshot.getKey();
                    DatabaseReference recipeRef = FirebaseDatabase.getInstance().getReference("CommonRecipes").child(recipeId);

                    recipeRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            Recipe recipe = dataSnapshot.getValue(Recipe.class);
                            if (recipe != null) {
                                favoriteRecipes.add(recipe);
                                recipeAdapter.notifyDataSetChanged();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.e("FavoritesActivity", "Ошибка загрузки рецепта", error.toException());
                        }
                    });
                }

                if (favoriteRecipes.isEmpty()) {
                    Toast.makeText(FavoritesActivity.this, "Избранных рецептов нет", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(FavoritesActivity.this, "Ошибка загрузки избранного", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openRecipeDetail(Recipe recipe) {
        Intent intent = new Intent(this, RecipeDetailActivity.class);
        intent.putExtra("recipeId", recipe.getId());
        intent.putExtra("fromFavorites", true);
        startActivity(intent);
    }
}
