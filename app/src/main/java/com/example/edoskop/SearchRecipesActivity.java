package com.example.edoskop;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.EditText;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class SearchRecipesActivity extends AppCompatActivity {
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_PICK = 2;

    private ImageView capturedImageView;
    private RecyclerView recognizedItemsListView;
    private List<String> recognizedItems;
    private RecognizedItemsAdapter adapter;
    private EditText newIngredientInput;  // Поле для ввода нового ингредиента

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_recipes);

        // Инициализация нейронной сети
        NeuralNetworkHelper.loadModel(this);

        // Получение ссылок на элементы UI
        Button openCameraButton = findViewById(R.id.openCameraButton);
        Button uploadFromGalleryButton = findViewById(R.id.uploadFromGalleryButton);
        Button addIngredientButton = findViewById(R.id.addIngredientButton); // Кнопка для добавления ингредиента
        capturedImageView = findViewById(R.id.capturedImageView);
        recognizedItemsListView = findViewById(R.id.recognizedItemsListView);
        newIngredientInput = findViewById(R.id.newIngredientInput); // Поле ввода нового ингредиента

        // Инициализация списка распознанных продуктов и адаптера
        recognizedItems = new ArrayList<>();
        adapter = new RecognizedItemsAdapter(this, recognizedItems);
        recognizedItemsListView.setAdapter(adapter);

        // Обработчики кнопок
        openCameraButton.setOnClickListener(v -> openCamera());
        uploadFromGalleryButton.setOnClickListener(v -> pickImageFromGallery());
        addIngredientButton.setOnClickListener(v -> addNewIngredient()); // Добавление нового ингредиента
    }

    // Добавление нового ингредиента в список распознанных продуктов
    private void addNewIngredient() {
        String newIngredient = newIngredientInput.getText().toString().trim();
        if (!newIngredient.isEmpty()) {
            // Добавляем новый ингредиент в список
            recognizedItems.add(newIngredient);
            adapter.notifyDataSetChanged(); // Обновляем адаптер, чтобы отобразить изменения
            newIngredientInput.setText(""); // Очищаем поле ввода
        } else {
            Toast.makeText(this, "Введите ингредиент", Toast.LENGTH_SHORT).show();
        }
    }

    // Открытие камеры
    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        } else {
            Toast.makeText(this, "Камера недоступна", Toast.LENGTH_SHORT).show();
        }
    }

    // Выбор изображения из галереи
    private void pickImageFromGallery() {
        Intent pickPhotoIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pickPhotoIntent, REQUEST_IMAGE_PICK);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == REQUEST_IMAGE_CAPTURE) {
                Bundle extras = data.getExtras();
                Bitmap imageBitmap = (Bitmap) extras.get("data");
                capturedImageView.setImageBitmap(imageBitmap);
                recognizeProducts(imageBitmap);
            } else if (requestCode == REQUEST_IMAGE_PICK) {
                Uri selectedImage = data.getData();
                try {
                    InputStream imageStream = getContentResolver().openInputStream(selectedImage);
                    Bitmap imageBitmap = BitmapFactory.decodeStream(imageStream);
                    capturedImageView.setImageBitmap(imageBitmap);
                    recognizeProducts(imageBitmap);
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Ошибка загрузки изображения", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void recognizeProducts(Bitmap image) {
        // Распознаем продукты с помощью нейросети
        List<String> detectedProducts = NeuralNetworkHelper.recognize(image);
        if (detectedProducts.isEmpty()) {
            Toast.makeText(this, "Не удалось распознать продукты", Toast.LENGTH_SHORT).show();
        } else {
            recognizedItems.clear();
            recognizedItems.addAll(detectedProducts);
            adapter.notifyDataSetChanged(); // Обновляем UI
        }
    }
}
