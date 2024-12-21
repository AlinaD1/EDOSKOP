package com.example.edoskop;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class EditProfileActivity extends AppCompatActivity {

    private EditText usernameEditText, passwordEditText, currentPasswordEditText;
    private Button saveButton, backButton, deleteAccountButton;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // Initialize views
        usernameEditText = findViewById(R.id.usernameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        currentPasswordEditText = findViewById(R.id.currentPasswordEditText);
        saveButton = findViewById(R.id.saveButton);
        backButton = findViewById(R.id.backButton);
        deleteAccountButton = findViewById(R.id.deleteAccountButton);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference("Users");

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            // Load current username
            mDatabase.child(user.getUid()).get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult().exists()) {
                    String username = task.getResult().getValue(String.class);
                    usernameEditText.setText(username);
                }
            });
        }

        // Save changes button
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
                // Re-authenticate before updating profile
                user.reauthenticate(EmailAuthProvider.getCredential(user.getEmail(), currentPassword))
                        .addOnCompleteListener(authTask -> {
                            if (authTask.isSuccessful()) {
                                // Update username
                                mDatabase.child(user.getUid()).setValue(newUsername)
                                        .addOnCompleteListener(task -> {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(this, "Имя пользователя обновлено", Toast.LENGTH_SHORT).show();
                                            } else {
                                                Toast.makeText(this, "Ошибка обновления имени пользователя", Toast.LENGTH_SHORT).show();
                                            }
                                        });

                                // Update password
                                if (!TextUtils.isEmpty(newPassword)) {
                                    user.updatePassword(newPassword)
                                            .addOnCompleteListener(task -> {
                                                if (task.isSuccessful()) {
                                                    // Send email for verification
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

        // Back to profile
        backButton.setOnClickListener(view -> {
            startActivity(new Intent(EditProfileActivity.this, ProfileActivity.class));
            finish();
        });

        // Handle account deletion
        deleteAccountButton.setOnClickListener(view -> {
            FirebaseUser userToDelete = mAuth.getCurrentUser();
            if (userToDelete != null) {
                // Confirm account deletion
                new AlertDialog.Builder(this)
                        .setTitle("Удалить аккаунт")
                        .setMessage("Вы уверены, что хотите удалить свой аккаунт?")
                        .setPositiveButton("Да", (dialog, which) -> {
                            // Re-authenticate before deletion
                            String currentPassword = currentPasswordEditText.getText().toString();

                            if (TextUtils.isEmpty(currentPassword)) {
                                Toast.makeText(this, "Введите текущий пароль для подтверждения удаления", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            userToDelete.reauthenticate(EmailAuthProvider.getCredential(userToDelete.getEmail(), currentPassword))
                                    .addOnCompleteListener(authTask -> {
                                        if (authTask.isSuccessful()) {
                                            // Delete account
                                            userToDelete.delete()
                                                    .addOnCompleteListener(deleteTask -> {
                                                        if (deleteTask.isSuccessful()) {
                                                            Toast.makeText(this, "Аккаунт удален", Toast.LENGTH_SHORT).show();
                                                            // Redirect to login activity after deletion
                                                            startActivity(new Intent(EditProfileActivity.this, LoginActivity.class));
                                                            finish();
                                                        } else {
                                                            Toast.makeText(this, "Ошибка при удалении аккаунта", Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                        } else {
                                            Toast.makeText(this, "Не удалось подтвердить пароль для удаления аккаунта", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        })
                        .setNegativeButton("Нет", null)
                        .show();
            }
        });
    }
}
