package com.example.edoskop;

import android.graphics.Bitmap;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import okhttp3.*;

public class NeuralNetworkHelper {

    private static final String API_URL = "https://predict.ultralytics.com";
    private static final String API_KEY = "01b7273e903917a0fdb3a3b6bed1c68f1eae8a703e";
    private static final String MODEL_URL = "https://hub.ultralytics.com/models/oEGPQzz54rE6XMaBcy4K";
    private static final String TAG = "NeuralNetworkHelper";

    private static final ExecutorService executorService = Executors.newSingleThreadExecutor();

    // Создание карты для перевода английских слов в русский
    private static final Map<String, String> translations = new HashMap<>();
    static {
        translations.put("banana", "банан");
        translations.put("Apple", "яблоко");
        translations.put("egg", "яйцо");
        translations.put("flour", "мука");
        translations.put("cheese", "сыр");
    }

    // Преобразование изображения в байтовый массив для отправки на сервер
    private static byte[] bitmapToByteArray(Bitmap bitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
        return outputStream.toByteArray();
    }

    // Распознавание объектов с использованием удаленного API (асинхронно)
    public static Future<List<String>> recognize(Bitmap bitmap) {
        return executorService.submit(new Callable<List<String>>() {
            @Override
            public List<String> call() {
                Set<String> detectedProducts = new HashSet<>();

                try {
                    byte[] imageBytes = bitmapToByteArray(bitmap);

                    OkHttpClient client = new OkHttpClient();

                    // Создаем тело запроса с изображением и параметрами
                    RequestBody fileBody = RequestBody.create(imageBytes, MediaType.parse("image/jpeg"));

                    RequestBody requestBody = new MultipartBody.Builder()
                            .setType(MultipartBody.FORM)
                            .addFormDataPart("file", "image.jpg", fileBody)
                            .addFormDataPart("model", MODEL_URL)
                            .addFormDataPart("imgsz", "640")
                            .addFormDataPart("conf", "0.25")
                            .addFormDataPart("iou", "0.45")
                            .build();

                    // Создаем запрос
                    Request request = new Request.Builder()
                            .url(API_URL)
                            .addHeader("x-api-key", API_KEY)
                            .post(requestBody)
                            .build();

                    Response response = client.newCall(request).execute();

                    if (response.isSuccessful() && response.body() != null) {
                        String responseBody = response.body().string();
                        Log.d(TAG, "Ответ от сервера: " + responseBody);
                        parseResponse(responseBody, detectedProducts);
                    } else {
                        Log.e(TAG, "Ошибка соединения: " + response.code());
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Ошибка при обработке изображения", e);
                }

                return new ArrayList<>(detectedProducts);
            }
        });
    }

    // Разбор JSON-ответа от сервера с переводом
    private static void parseResponse(String response, Set<String> detectedProducts) {
        try {
            JSONObject jsonResponse = new JSONObject(response);
            JSONArray images = jsonResponse.getJSONArray("images");

            for (int i = 0; i < images.length(); i++) {
                JSONObject image = images.getJSONObject(i);
                JSONArray results = image.getJSONArray("results");

                for (int j = 0; j < results.length(); j++) {
                    JSONObject result = results.getJSONObject(j);
                    String className = result.getString("name");

                    // Переводим слово, если оно есть в карте
                    String translatedName = translations.getOrDefault(className, className);

                    detectedProducts.add(translatedName);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при разборе JSON-ответа", e);
        }
    }
}
