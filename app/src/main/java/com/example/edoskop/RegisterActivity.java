package com.example.edoskop;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import android.content.Intent;

public class RegisterActivity extends AppCompatActivity {

    private EditText usernameEditText, emailEditText, passwordEditText;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;  // Добавляем ссылку на Firebase Database

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        usernameEditText = findViewById(R.id.usernameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference("Users");  // Инициализация базы данных Firebase

        findViewById(R.id.loginButton).setOnClickListener(view -> registerUser());
    }

    private void registerUser() {
        String username = usernameEditText.getText().toString();
        String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Регистрация успешна
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // Сохранение имени пользователя в Firebase
                            mDatabase.child(user.getUid()).setValue(username);

                            // Отправка письма для подтверждения email
                            user.sendEmailVerification()
                                    .addOnCompleteListener(this, emailTask -> {
                                        if (emailTask.isSuccessful()) {
                                            // Если письмо успешно отправлено, показываем сообщение
                                            Toast.makeText(RegisterActivity.this, "Письмо для подтверждения отправлено на ваш email", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(RegisterActivity.this, "Не удалось отправить письмо для подтверждения", Toast.LENGTH_SHORT).show();
                                        }
                                    });

                            // Переход на экран входа
                            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                            startActivity(intent);
                            finish(); // Закрытие текущей активности
                        }
                    } else {
                        // Если регистрация не удалась
                        Toast.makeText(RegisterActivity.this, "Ошибка регистрации", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
