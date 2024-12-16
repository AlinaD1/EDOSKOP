package com.example.edoskop;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.graphics.Canvas;

import org.pytorch.IValue;
import org.pytorch.Module;
import org.pytorch.Tensor;
import org.pytorch.torchvision.TensorImageUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;

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

    private static List<float[]> decodePredictions(float[] outputs, float confidenceThreshold) {
        List<float[]> predictions = new ArrayList<>();
        int predictionLength = 6; // x_center, y_center, width, height, confidence, class_index

        for (int i = 0; i < outputs.length; i += predictionLength) {
            float confidence = outputs[i + 4];
            if (confidence > confidenceThreshold) {
                float x_center = outputs[i];
                float y_center = outputs[i + 1];
                float width = outputs[i + 2];
                float height = outputs[i + 3];
                int classIndex = Math.round(outputs[i + 5]); // Округляем класс до целого

                float x1 = x_center - width / 2;
                float y1 = y_center - height / 2;
                float x2 = x_center + width / 2;
                float y2 = y_center + height / 2;

                predictions.add(new float[]{x1, y1, x2, y2, confidence, classIndex});
            }
        }

        return predictions;
    }

    public static List<float[]> nonMaximumSuppression(List<float[]> predictions, float iouThreshold) {
        List<float[]> filteredPredictions = new ArrayList<>();
        predictions.sort((o1, o2) -> Float.compare(o2[4], o1[4])); // Сортировка по confidence

        while (!predictions.isEmpty()) {
            float[] bestPrediction = predictions.remove(0);
            filteredPredictions.add(bestPrediction);

            predictions.removeIf(prediction -> calculateIoU(bestPrediction, prediction) > iouThreshold);
        }

        return filteredPredictions;
    }

    private static float calculateIoU(float[] boxA, float[] boxB) {
        float x1 = Math.max(boxA[0], boxB[0]);
        float y1 = Math.max(boxA[1], boxB[1]);
        float x2 = Math.min(boxA[2], boxB[2]);
        float y2 = Math.min(boxA[3], boxB[3]);

        float intersection = Math.max(0, x2 - x1) * Math.max(0, y2 - y1);
        float areaA = (boxA[2] - boxA[0]) * (boxA[3] - boxA[1]);
        float areaB = (boxB[2] - boxB[0]) * (boxB[3] - boxB[1]);

        float union = areaA + areaB - intersection;
        return intersection / union;
    }

    private static Bitmap addPadding(Bitmap bitmap, int targetSize) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        float scale = Math.min((float) targetSize / width, (float) targetSize / height);
        int newWidth = Math.round(width * scale);
        int newHeight = Math.round(height * scale);

        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
        Bitmap paddedBitmap = Bitmap.createBitmap(targetSize, targetSize, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(paddedBitmap);
        int xOffset = (targetSize - newWidth) / 2;
        int yOffset = (targetSize - newHeight) / 2;
        canvas.drawBitmap(resizedBitmap, xOffset, yOffset, null);

        return paddedBitmap;
    }


    // Распознавание объектов
    public static List<String> recognize(Bitmap bitmap) {
        Set<String> detectedProducts = new HashSet<>();
        if (model == null) {
            Log.e("NeuralNetworkHelper", "Модель не загружена.");
            return new ArrayList<>(detectedProducts);
        }

        try {
            Bitmap paddedBitmap = addPadding(bitmap, 640);
            Tensor inputTensor = TensorImageUtils.bitmapToFloat32Tensor(
                    paddedBitmap,
                    TensorImageUtils.TORCHVISION_NORM_MEAN_RGB,
                    TensorImageUtils.TORCHVISION_NORM_STD_RGB
            );

            Tensor outputTensor = model.forward(IValue.from(inputTensor)).toTensor();
            float[] outputs = outputTensor.getDataAsFloatArray();

            List<float[]> decodedPredictions = decodePredictions(outputs, 0.5f);
            List<float[]> finalPredictions = nonMaximumSuppression(decodedPredictions, 0.4f);

            for (float[] prediction : finalPredictions) {
                int classIndex = (int) prediction[5];
                if (classIndex >= 0 && classIndex < classNames.size()) {
                    String className = classNames.get(classIndex);
                    detectedProducts.add(className);
                }
            }
        } catch (Exception e) {
            Log.e("NeuralNetworkHelper", "Ошибка обработки изображения", e);
        }

        return new ArrayList<>(detectedProducts);
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
