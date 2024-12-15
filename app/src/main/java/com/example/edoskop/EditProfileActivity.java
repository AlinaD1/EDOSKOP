package com.example.edoskop;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class EditProfileActivity extends AppCompatActivity {

    private EditText usernameEditText, passwordEditText, currentPasswordEditText;
    private Button saveButton, backButton;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // Инициализация элементов
        usernameEditText = findViewById(R.id.usernameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        currentPasswordEditText = findViewById(R.id.currentPasswordEditText);
        saveButton = findViewById(R.id.saveButton);
        backButton = findViewById(R.id.backButton);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference("Users");

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            // Загрузка текущего имени пользователя
            mDatabase.child(user.getUid()).get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult().exists()) {
                    String username = task.getResult().getValue(String.class);
                    usernameEditText.setText(username);
                }
            });
        }

        // Сохранение изменений
        saveButton.setOnClickListener(view -> {
            String newUsername = usernameEditText.getText().toString();
            String newPassword = passwordEditText.getText().toString();
            String currentPassword = currentPasswordEditText.getText().toString();

            if (TextUtils.isEmpty(newUsername)) {
                Toast.makeText(this, "Введите имя пользователя", Toast.LENGTH_SHORT).show();
                return;
            }

            if (TextUtils.isEmpty(currentPassword)) {
                Toast.makeText(this, "Введите текущий пароль для подтверждения изменений", Toast.LENGTH_SHORT).show();
                return;
            }

            if (user != null) {
                // Повторная аутентификация перед изменением пароля
                user.reauthenticate(EmailAuthProvider.getCredential(user.getEmail(), currentPassword))
                        .addOnCompleteListener(authTask -> {
                            if (authTask.isSuccessful()) {
                                // Обновление имени пользователя
                                mDatabase.child(user.getUid()).setValue(newUsername)
                                        .addOnCompleteListener(task -> {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(this, "Имя пользователя обновлено", Toast.LENGTH_SHORT).show();
                                            } else {
                                                Toast.makeText(this, "Ошибка обновления имени пользователя", Toast.LENGTH_SHORT).show();
                                            }
                                        });

                                // Обновление пароля
                                if (!TextUtils.isEmpty(newPassword)) {
                                    user.updatePassword(newPassword)
                                            .addOnCompleteListener(task -> {
                                                if (task.isSuccessful()) {
                                                    // Отправляем письмо для подтверждения изменений
                                                    user.sendEmailVerification()
                                                            .addOnCompleteListener(emailTask -> {
                                                                if (emailTask.isSuccessful()) {
                                                                    Toast.makeText(this, "Пароль обновлен. Проверьте вашу почту для подтверждения изменений.", Toast.LENGTH_LONG).show();
                                                                } else {
                                                                    Toast.makeText(this, "Ошибка отправки письма для подтверждения", Toast.LENGTH_SHORT).show();
                                                                }
                                                            });
                                                } else {
                                                    Toast.makeText(this, "Ошибка обновления пароля", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                }
                            } else {
                                Toast.makeText(this, "Не удалось подтвердить пароль. Проверьте текущий пароль и попробуйте снова.", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

        // Возврат в профиль
        backButton.setOnClickListener(view -> {
            startActivity(new Intent(EditProfileActivity.this, ProfileActivity.class));
            finish();
        });
    }
}
