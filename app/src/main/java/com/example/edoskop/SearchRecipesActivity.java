package com.example.edoskop;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class SearchRecipesActivity extends AppCompatActivity {
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_PICK = 2;

    private ImageView capturedImageView;
    private ListView recognizedItemsListView;
    private List<String> recognizedItems;
    private RecognizedItemsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_recipes);

        // Initialize neural network
        NeuralNetworkHelper.loadModel(this);

        Button openCameraButton = findViewById(R.id.openCameraButton);
        Button uploadFromGalleryButton = findViewById(R.id.uploadFromGalleryButton);
        capturedImageView = findViewById(R.id.capturedImageView);
        recognizedItemsListView = findViewById(R.id.recognizedItemsListView);

        recognizedItems = new ArrayList<>();
        adapter = new RecognizedItemsAdapter(this, recognizedItems);
        recognizedItemsListView.setAdapter(adapter);

        openCameraButton.setOnClickListener(v -> openCamera());
        uploadFromGalleryButton.setOnClickListener(v -> pickImageFromGallery());
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
        // Recognize products with the neural network
        List<String> detectedProducts = NeuralNetworkHelper.recognize(image);
        if (detectedProducts.isEmpty()) {
            Toast.makeText(this, "Не удалось распознать продукты", Toast.LENGTH_SHORT).show();
        } else {
            recognizedItems.clear();
            recognizedItems.addAll(detectedProducts);
            adapter.notifyDataSetChanged(); // Update UI
        }
    }
}





