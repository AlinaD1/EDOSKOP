package com.example.edoskop;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import android.widget.Button;  // Убедитесь, что импортирован нужный класс

public class ProfileActivity extends AppCompatActivity {

    private TextView welcomeTextView;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private Button logoutButton;  // Добавляем переменную для кнопки выхода

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Инициализация элементов интерфейса и Firebase
        welcomeTextView = findViewById(R.id.welcomeTextView);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference("Users");

        FirebaseDatabase.getInstance().getReference("Users");

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            mDatabase.child(user.getUid()).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    String username = task.getResult().getValue(String.class);
                    welcomeTextView.setText("Добро пожаловать, " + username + "!");
                }
            });
        } // Загрузка имени пользователя из базы данных

        // Обработка переходов по кнопкам
        findViewById(R.id.favoritesButton).setOnClickListener(view ->
                startActivity(new Intent(ProfileActivity.this, FavoritesActivity.class))
        );

        findViewById(R.id.myRecipesButton).setOnClickListener(view ->
                startActivity(new Intent(ProfileActivity.this, MyRecipesActivity.class))
        );

        findViewById(R.id.searchRecipesButton).setOnClickListener(view ->
                startActivity(new Intent(ProfileActivity.this, SearchRecipesActivity.class))
        );

        findViewById(R.id.editProfileButton).setOnClickListener(view ->
                startActivity(new Intent(ProfileActivity.this, EditProfileActivity.class))
        );

        // Инициализация и обработка кнопки выхода
        logoutButton = findViewById(R.id.logoutButton);  // Находим кнопку "Выйти"
        logoutButton.setOnClickListener(view -> {
            FirebaseAuth.getInstance().signOut();  // Выход из аккаунта Firebase
            Toast.makeText(ProfileActivity.this, "Выход выполнен", Toast.LENGTH_SHORT).show();
            redirectToLogin();  // Переход на экран логина
        });
    }

    private void redirectToLogin() {
        Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Очищаем историю переходов
        startActivity(intent);
        finish();
    }
}

