package com.example.edoskop;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

public class AddRecipeActivity extends Activity {

    private ImageView recipeImageView;
    private EditText recipeNameEditText;
    private EditText descriptionEditText;
    private ListView ingredientsListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_recipe);

        // Инициализация элементов интерфейса
        recipeImageView = findViewById(R.id.recipeImageView);
        recipeNameEditText = findViewById(R.id.recipeNameEditText);
        descriptionEditText = findViewById(R.id.descriptionEditText);
        ingredientsListView = findViewById(R.id.ingredientsListView);

        Button saveButton = findViewById(R.id.saveButton);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Логика для сохранения рецепта
                String recipeName = recipeNameEditText.getText().toString();
                String description = descriptionEditText.getText().toString();

                if (!recipeName.isEmpty() && !description.isEmpty()) {
                    // Ваши действия для сохранения рецепта (например, в базу данных или на сервере)
                    Toast.makeText(AddRecipeActivity.this, "Рецепт сохранен", Toast.LENGTH_SHORT).show();
                    finish(); // Закрываем экран добавления рецепта и возвращаемся обратно
                } else {
                    Toast.makeText(AddRecipeActivity.this, "Заполните все поля", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
