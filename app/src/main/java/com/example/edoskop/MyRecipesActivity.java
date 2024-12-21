package com.example.edoskop;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MyRecipesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private FirebaseRecyclerAdapter<UserRecipe, RecipeViewHolder> adapter;
    private DatabaseReference recipesDatabase;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_recipes);

        // Инициализация RecyclerView
        recyclerView = findViewById(R.id.recipesRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Инициализация Firebase
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        recipesDatabase = FirebaseDatabase.getInstance().getReference("Recipes").child(userId);

        // Настройка адаптера Firebase
        FirebaseRecyclerOptions<UserRecipe> options = new FirebaseRecyclerOptions.Builder<UserRecipe>()
                .setQuery(recipesDatabase, UserRecipe.class)
                .build();

        adapter = new FirebaseRecyclerAdapter<UserRecipe, RecipeViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull RecipeViewHolder holder, int position, @NonNull UserRecipe model) {
                holder.recipeName.setText(model.getName());
                holder.recipeDescription.setText(model.getDescription());

                // Переход к экрану редактирования при нажатии на рецепт
                holder.itemView.setOnClickListener(v -> {
                    Intent intent = new Intent(MyRecipesActivity.this, EditRecipeActivity.class);
                    intent.putExtra("recipeId", model.getId()); // Передаем ID рецепта
                    startActivity(intent);
                });
            }

            @NonNull
            @Override
            public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recipe_item, parent, false);
                return new RecipeViewHolder(view);
            }
        };

        recyclerView.setAdapter(adapter);

        // Кнопка добавления рецепта
        Button addRecipeButton = findViewById(R.id.addRecipeButton);
        addRecipeButton.setOnClickListener(v -> {
            Intent intent = new Intent(MyRecipesActivity.this, AddRecipeActivity.class);
            startActivity(intent);
        });

        // Кнопка возвращения в профиль
        Button returnToProfileButton = findViewById(R.id.returnToProfileButton);
        returnToProfileButton.setOnClickListener(v -> finish());
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (adapter != null) {
            adapter.startListening();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (adapter != null) {
            adapter.stopListening();
        }
    }

    public static class RecipeViewHolder extends RecyclerView.ViewHolder {
        TextView recipeName;
        TextView recipeDescription;

        public RecipeViewHolder(@NonNull View itemView) {
            super(itemView);
            recipeName = itemView.findViewById(R.id.recipeNameTextView);
            recipeDescription = itemView.findViewById(R.id.recipeDescriptionTextView);
        }
    }
}








