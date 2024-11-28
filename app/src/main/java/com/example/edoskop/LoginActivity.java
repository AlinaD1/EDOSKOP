package com.example.edoskop;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
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

        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        mAuth = FirebaseAuth.getInstance();
        forgotPasswordButton = findViewById(R.id.forgotPasswordButton); // Инициализация кнопки

        findViewById(R.id.loginButton).setOnClickListener(view -> loginUser());
        findViewById(R.id.registerButton).setOnClickListener(view -> startActivity(new Intent(LoginActivity.this, RegisterActivity.class)));

        // Обработчик нажатия на кнопку "Забыли пароль?"
        forgotPasswordButton.setOnClickListener(view -> resetPassword());
    }

    private void loginUser() {
        String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        startActivity(new Intent(LoginActivity.this, ProfileActivity.class));
                        finish();
                    } else {
                        Toast.makeText(LoginActivity.this, "Ошибка авторизации", Toast.LENGTH_SHORT).show();
                    }
                });
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


