package com.example.edoskop;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import org.pytorch.IValue;
import org.pytorch.Module;
import org.pytorch.Tensor;
import org.pytorch.torchvision.TensorImageUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class NeuralNetworkHelper {

    private static Module model;
    private static List<String> classNames = new ArrayList<>();

    // Загрузка модели и имен классов
    public static void loadModel(Context context) {
        try {
            // Загрузка файла имен классов
            loadClassNames(context, "coco.names"); // Имя файла, где хранятся классы
            String modelPath = assetFilePath(context, "yolov8x.torchscript.pt"); // Убедитесь, что это TorchScript
            model = Module.load(modelPath);
            Log.i("NeuralNetworkHelper", "Модель успешно загружена: " + modelPath);
        } catch (Exception e) {
            Log.e("NeuralNetworkHelper", "Ошибка загрузки модели", e);
        }
    }

    // Распознавание объектов
    public static List<String> recognize(Bitmap bitmap) {
        List<String> detectedProducts = new ArrayList<>();
        if (model == null) {
            Log.e("NeuralNetworkHelper", "Модель не загружена. Проверьте вызов loadModel.");
            return detectedProducts;
        }

        try {
            // 1. Изменение размера изображения до 640x640
            Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, 640, 640, true);

            // 2. Преобразование изображения в тензор
            Tensor inputTensor = TensorImageUtils.bitmapToFloat32Tensor(
                    resizedBitmap,
                    TensorImageUtils.TORCHVISION_NORM_MEAN_RGB,
                    TensorImageUtils.TORCHVISION_NORM_STD_RGB
            );

            // 3. Инференс модели YOLO
            Tensor outputTensor = model.forward(IValue.from(inputTensor)).toTensor();

            // 4. Обработка выходных данных YOLO
            float[] outputs = outputTensor.getDataAsFloatArray();
            Log.i("NeuralNetworkHelper", "Размер выходного тензора: " + outputs.length);

            // YOLO возвращает формат (x_center, y_center, width, height, confidence, class_id)
            int predictionLength = 6; // Каждый объект в тензоре YOLO состоит из 6 значений
            for (int i = 0; i < outputs.length; i += predictionLength) {
                float x_center = outputs[i];
                float y_center = outputs[i + 1];
                float width = outputs[i + 2];
                float height = outputs[i + 3];
                float confidence = outputs[i + 4];
                int classIndex = (int) outputs[i + 5]; // Индекс класса

                // Убедимся, что объект соответствует критериям
                if (confidence > 0.8) { // Фильтруем по уверенности
                    if (classIndex >= 0 && classIndex < classNames.size()) {
                        String className = classNames.get(classIndex);
                        Log.i("NeuralNetworkHelper", String.format(
                                "Обнаружен объект: %s (уверенность: %.2f)", className, confidence
                        ));
                        detectedProducts.add(className);
                    } else {
                        Log.w("NeuralNetworkHelper", "Неизвестный класс (индекс класса: " + classIndex + ")");
                    }
                }
            }

        } catch (Exception e) {
            Log.e("NeuralNetworkHelper", "Ошибка обработки изображения", e);
        }

        if (detectedProducts.isEmpty()) {
            Log.w("NeuralNetworkHelper", "Объекты не обнаружены. Возможно, проблема с моделью.");
        }

        return detectedProducts;
    }

        // Загрузка имен классов из файла coco.names
    private static void loadClassNames(Context context, String fileName) {
        try {
            InputStream inputStream = context.getAssets().open(fileName);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                classNames.add(line.trim());
            }
            reader.close();
            Log.i("NeuralNetworkHelper", "Загружено " + classNames.size() + " классов");
        } catch (Exception e) {
            Log.e("NeuralNetworkHelper", "Ошибка при загрузке имен классов", e);
        }
    }

    // Копирование файла из assets
    private static String assetFilePath(Context context, String assetName) throws Exception {
        File file = new File(context.getFilesDir(), assetName);
        if (!file.exists()) {
            Log.i("NeuralNetworkHelper", "Копирование модели из assets...");
            try (InputStream is = context.getAssets().open(assetName);
                 FileOutputStream os = new FileOutputStream(file)) {
                byte[] buffer = new byte[4 * 1024];
                int read;
                while ((read = is.read(buffer)) != -1) {
                    os.write(buffer, 0, read);
                }
                os.flush();
            }
        } else {
            Log.i("NeuralNetworkHelper", "Файл модели уже существует: " + file.getAbsolutePath());
        }
        return file.getAbsolutePath();
    }
}
