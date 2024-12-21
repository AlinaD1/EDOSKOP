package com.example.edoskop;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MyRecipesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private FirebaseRecyclerAdapter<Recipe, RecipeViewHolder> adapter;
    private DatabaseReference recipesDatabase;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_recipes);

        recyclerView = findViewById(R.id.recipesRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        recipesDatabase = FirebaseDatabase.getInstance().getReference("Recipes");

        FirebaseRecyclerOptions<Recipe> options = new FirebaseRecyclerOptions.Builder<Recipe>()
                .setQuery(recipesDatabase.orderByChild("userId").equalTo(userId), Recipe.class)
                .build();

        adapter = new FirebaseRecyclerAdapter<Recipe, RecipeViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull RecipeViewHolder holder, int position, @NonNull Recipe model) {
                holder.recipeName.setText(model.getName());

                // Обработка нажатий на рецепт
                holder.itemView.setOnClickListener(v -> {
                    Intent intent = new Intent(MyRecipesActivity.this, RecipeDetailActivity.class);
                    intent.putExtra("recipeId", model.getId());
                    startActivity(intent);
                });

                // Удаление рецепта
                holder.deleteButton.setOnClickListener(v -> {
                    new MaterialAlertDialogBuilder(MyRecipesActivity.this)
                            .setTitle("Удалить рецепт")
                            .setMessage("Вы уверены, что хотите удалить этот рецепт?")
                            .setPositiveButton("Да", (dialog, which) -> {
                                String recipeId = getRef(position).getKey();
                                if (recipeId != null) {
                                    recipesDatabase.child(recipeId).removeValue().addOnSuccessListener(aVoid ->
                                            Toast.makeText(MyRecipesActivity.this, "Рецепт удален", Toast.LENGTH_SHORT).show());
                                }
                            })
                            .setNegativeButton("Нет", null)
                            .show();
                });

                // Редактирование рецепта
                holder.editButton.setOnClickListener(v -> {
                    Intent intent = new Intent(MyRecipesActivity.this, EditRecipeActivity.class);
                    intent.putExtra("recipeId", model.getId());
                    startActivity(intent);
                });
            }

            @NonNull
            @Override
            public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recipe_item_with_buttons, parent, false);
                return new RecipeViewHolder(view);
            }
        };

        recyclerView.setAdapter(adapter);

        // Кнопка добавления рецепта
        MaterialButton addRecipeButton = findViewById(R.id.addRecipeButton);
        addRecipeButton.setOnClickListener(v -> {
            Intent intent = new Intent(MyRecipesActivity.this, AddRecipeActivity.class);
            startActivity(intent);
        });
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
        MaterialButton deleteButton;
        MaterialButton editButton;

        public RecipeViewHolder(@NonNull View itemView) {
            super(itemView);
            recipeName = itemView.findViewById(R.id.recipeNameTextView);
            deleteButton = itemView.findViewById(R.id.deleteButton);
            editButton = itemView.findViewById(R.id.editButton);
        }
    }
}




