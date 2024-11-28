package com.example.edoskop;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;

public class MyRecipesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_recipes);

        // Кнопка выхода
        MaterialButton logoutButton = findViewById(R.id.logoutButton);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Выход из аккаунта Firebase
                FirebaseAuth.getInstance().signOut();

                // Уведомление и переход на экран логина
                Toast.makeText(MyRecipesActivity.this, "Выход выполнен", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MyRecipesActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish(); // Закрываем текущую активность
            }
        });

        // Кнопка добавления рецепта
        MaterialButton addRecipeButton = findViewById(R.id.addRecipeButton);
        addRecipeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Переход на экран добавления рецепта
                Intent intent = new Intent(MyRecipesActivity.this, AddRecipeActivity.class);
                startActivity(intent);
            }
        });
    }
}
