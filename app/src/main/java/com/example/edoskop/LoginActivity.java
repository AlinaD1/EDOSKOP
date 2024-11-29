package com.example.edoskop;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import android.content.Intent;
import android.widget.Button;

public class LoginActivity extends AppCompatActivity {

    private EditText emailEditText, passwordEditText;
    private FirebaseAuth mAuth;
    private Button forgotPasswordButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        // Проверяем, авторизован ли пользователь
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            navigateToProfile();  // Переход на главный экран, если уже вошел
        } else {
            setContentView(R.layout.activity_login);  // Загружаем экран логина

            emailEditText = findViewById(R.id.emailEditText);
            passwordEditText = findViewById(R.id.passwordEditText);

            findViewById(R.id.loginButton).setOnClickListener(view -> loginUser());
            findViewById(R.id.registerButton).setOnClickListener(view ->
                    startActivity(new Intent(LoginActivity.this, RegisterActivity.class)));
        }

        forgotPasswordButton = findViewById(R.id.forgotPasswordButton); // Инициализация кнопки
        // Обработчик нажатия на кнопку "Забыли пароль?"
        forgotPasswordButton.setOnClickListener(view -> resetPassword());
    }
    private void loginUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Введите все поля", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        navigateToProfile();  // Переход на экран профиля
                    } else {
                        Toast.makeText(LoginActivity.this, "Ошибка авторизации", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void navigateToProfile() {
        Intent intent = new Intent(LoginActivity.this, ProfileActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);  // Очищаем историю переходов
        startActivity(intent);
        finish();
    }
    // Функция для сброса пароля
    private void resetPassword() {
        String email = emailEditText.getText().toString();

        if (email.isEmpty()) {
            Toast.makeText(LoginActivity.this, "Пожалуйста, введите ваш email", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(LoginActivity.this, "Инструкции по сбросу пароля отправлены на ваш email", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(LoginActivity.this, "Ошибка при отправке письма для сброса пароля", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}

