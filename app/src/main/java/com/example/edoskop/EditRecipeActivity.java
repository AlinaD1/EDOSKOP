package com.example.edoskop;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
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

public class EditRecipeActivity extends AppCompatActivity {

    private EditText editRecipeName, editDescription;
    private LinearLayout ingredientsContainer;
    private Button addIngredientButton, saveChangesButton, deleteRecipeButton, returnToRecipesButton;

    private String recipeId, userId;
    private DatabaseReference recipeRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_recipe);

        editRecipeName = findViewById(R.id.editRecipeName);
        editDescription = findViewById(R.id.editDescription);
        ingredientsContainer = findViewById(R.id.ingredientsContainer);
        addIngredientButton = findViewById(R.id.addIngredientButton);
        saveChangesButton = findViewById(R.id.saveChangesButton);
        deleteRecipeButton = findViewById(R.id.deleteRecipeButton);
        returnToRecipesButton = findViewById(R.id.returnToRecipesButton);

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        recipeId = getIntent().getStringExtra("recipeId");

        if (recipeId == null || recipeId.isEmpty()) {
            Toast.makeText(this, "ID рецепта не найден", Toast.LENGTH_SHORT).show();
            Log.e("EditRecipeActivity", "recipeId отсутствует или пустой");
            finish();
            return;
        }

        recipeRef = FirebaseDatabase.getInstance().getReference("Recipes").child(userId).child(recipeId);
        Log.d("EditRecipeActivity", "Инициализация recipeRef завершена");

        loadRecipeData();

        addIngredientButton.setOnClickListener(v -> addIngredientField());
        saveChangesButton.setOnClickListener(v -> saveChanges());
        deleteRecipeButton.setOnClickListener(v -> deleteRecipe());
        returnToRecipesButton.setOnClickListener(v -> showReturnConfirmationDialog());
    }

    private void loadRecipeData() {
        Log.d("EditRecipeActivity", "Загрузка данных для recipeId: " + recipeId);

        recipeRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                UserRecipe recipe = task.getResult().getValue(UserRecipe.class);
                if (recipe != null) {
                    Log.d("EditRecipeActivity", "Данные рецепта загружены: " + recipe.getName());
                    editRecipeName.setText(recipe.getName());
                    editDescription.setText(recipe.getDescription());
                    ingredientsContainer.removeAllViews();
                    for (String ingredient : recipe.getIngredients()) {
                        addIngredientField(ingredient);
                    }
                } else {
                    Log.e("EditRecipeActivity", "Рецепт null");
                }
            } else {
                Log.e("EditRecipeActivity", "Ошибка загрузки данных", task.getException());
                Toast.makeText(this, "Ошибка загрузки данных", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addIngredientField(String ingredient) {
        EditText ingredientInput = new EditText(this);
        ingredientInput.setHint("Ингредиент");
        ingredientInput.setText(ingredient);
        ingredientInput.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        ingredientsContainer.addView(ingredientInput);
    }

    private void addIngredientField() {
        addIngredientField("");
    }

    private void saveChanges() {
        String newName = editRecipeName.getText().toString();
        String newDescription = editDescription.getText().toString();
        List<String> newIngredients = new ArrayList<>();

        for (int i = 0; i < ingredientsContainer.getChildCount(); i++) {
            View view = ingredientsContainer.getChildAt(i);
            if (view instanceof EditText) {
                String ingredient = ((EditText) view).getText().toString();
                if (!TextUtils.isEmpty(ingredient)) {
                    newIngredients.add(ingredient);
                }
            }
        }

        if (TextUtils.isEmpty(newName) || TextUtils.isEmpty(newDescription) || newIngredients.isEmpty()) {
            Toast.makeText(this, "Все поля должны быть заполнены", Toast.LENGTH_SHORT).show();
            Log.e("EditRecipeActivity", "Пустые поля при сохранении");
            return;
        }

        UserRecipe updatedRecipe = new UserRecipe(recipeId, newName, newDescription, newIngredients);
        recipeRef.setValue(updatedRecipe).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(EditRecipeActivity.this, "Изменения сохранены", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(EditRecipeActivity.this, "Ошибка сохранения изменений", Toast.LENGTH_SHORT).show();
                Log.e("EditRecipeActivity", "Ошибка сохранения изменений", task.getException());
            }
        });
    }

    private void deleteRecipe() {
        recipeRef.removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(EditRecipeActivity.this, "Рецепт удалён", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(EditRecipeActivity.this, "Ошибка удаления рецепта", Toast.LENGTH_SHORT).show();
                Log.e("EditRecipeActivity", "Ошибка удаления рецепта", task.getException());
            }
        });
    }

    private void showReturnConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Вы уверены?")
                .setMessage("Все несохранённые изменения будут потеряны.")
                .setPositiveButton("Выйти", (dialog, which) -> finish())
                .setNegativeButton("Отмена", null)
                .show();
    }

    @Override
    public void onBackPressed() {
        showReturnConfirmationDialog();
    }
}



