package com.example.edoskop;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class SearchRecipesActivity extends AppCompatActivity {
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_PICK = 2;

    private ImageView capturedImageView;
    private RecyclerView recognizedItemsListView;
    private List<String> recognizedItems;
    private RecognizedItemsAdapter adapter;
    private EditText newIngredientInput;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

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
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        } else {
            Toast.makeText(this, "Камера недоступна", Toast.LENGTH_SHORT).show();
        }
    }

    private void pickImageFromGallery() {
        Intent pickPhotoIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pickPhotoIntent, REQUEST_IMAGE_PICK);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            try {
                Bitmap imageBitmap;
                if (requestCode == REQUEST_IMAGE_CAPTURE) {
                    Bundle extras = data.getExtras();
                    imageBitmap = (Bitmap) extras.get("data");
                } else if (requestCode == REQUEST_IMAGE_PICK) {
                    Uri selectedImage = data.getData();
                    try (InputStream imageStream = getContentResolver().openInputStream(selectedImage)) {
                        imageBitmap = BitmapFactory.decodeStream(imageStream);
                    }
                } else {
                    return;
                }

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
