package com.example.edoskop;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class SearchRecipesActivity extends AppCompatActivity {
    private ImageView capturedImageView;
    private RecyclerView recognizedItemsListView;
    private List<String> recognizedItems;
    private RecognizedItemsAdapter adapter;
    private EditText newIngredientInput;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Bitmap imageBitmap = (Bitmap) result.getData().getExtras().get("data");
                    if (imageBitmap != null) {
                        capturedImageView.setImageBitmap(imageBitmap);
                        recognizeProducts(imageBitmap);
                    }
                }
            }
    );

    private final ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri selectedImage = result.getData().getData();
                    try (InputStream imageStream = getContentResolver().openInputStream(selectedImage)) {
                        Bitmap imageBitmap = BitmapFactory.decodeStream(imageStream);
                        if (imageBitmap != null) {
                            capturedImageView.setImageBitmap(imageBitmap);
                            recognizeProducts(imageBitmap);
                        } else {
                            Toast.makeText(this, "Не удалось загрузить изображение", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Ошибка загрузки изображения", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_recipes);

        Button openCameraButton = findViewById(R.id.openCameraButton);
        Button uploadFromGalleryButton = findViewById(R.id.uploadFromGalleryButton);
        Button addIngredientButton = findViewById(R.id.addIngredientButton);
        Button searchRecipesButton = findViewById(R.id.searchRecipesButton); // Добавили кнопку поиска

        capturedImageView = findViewById(R.id.capturedImageView);
        recognizedItemsListView = findViewById(R.id.recognizedItemsListView);
        newIngredientInput = findViewById(R.id.newIngredientInput);

        recognizedItems = new ArrayList<>();
        adapter = new RecognizedItemsAdapter(this, recognizedItems);
        recognizedItemsListView.setAdapter(adapter);

        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, 100);
        }

        openCameraButton.setOnClickListener(v -> openCamera());
        uploadFromGalleryButton.setOnClickListener(v -> pickImageFromGallery());
        addIngredientButton.setOnClickListener(v -> addNewIngredient());
        searchRecipesButton.setOnClickListener(v -> searchRecipes()); // Обработчик для кнопки поиска
    }

    private void addNewIngredient() {
        String newIngredient = newIngredientInput.getText().toString().trim();
        if (!newIngredient.isEmpty()) {
            recognizedItems.add(newIngredient);
            adapter.notifyDataSetChanged();
            newIngredientInput.setText("");
        } else {
            Toast.makeText(this, "Введите ингредиент", Toast.LENGTH_SHORT).show();
        }
    }

    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            cameraLauncher.launch(takePictureIntent);
        } else {
            Toast.makeText(this, "Камера недоступна", Toast.LENGTH_SHORT).show();
        }
    }

    private void pickImageFromGallery() {
        Intent pickPhotoIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(pickPhotoIntent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Разрешение на использование камеры предоставлено", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Камера не будет работать без разрешения", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void recognizeProducts(Bitmap image) {
        executorService.submit(() -> {
            try {
                Future<List<String>> future = NeuralNetworkHelper.recognize(image);
                List<String> detectedProducts = future.get();

                runOnUiThread(() -> {
                    if (detectedProducts.isEmpty()) {
                        Toast.makeText(this, "Не удалось распознать продукты", Toast.LENGTH_SHORT).show();
                    } else {
                        recognizedItems.clear();
                        recognizedItems.addAll(detectedProducts);
                        adapter.notifyDataSetChanged();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() ->
                        Toast.makeText(this, "Ошибка при распознавании продуктов", Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    private void searchRecipes() {
        if (recognizedItems.isEmpty()) {
            Toast.makeText(this, "Список ингредиентов пуст", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, RecipeResultsActivity.class);
        intent.putStringArrayListExtra("recognizedItems", new ArrayList<>(recognizedItems));
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}
