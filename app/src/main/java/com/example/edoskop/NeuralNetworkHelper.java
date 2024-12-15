package com.example.edoskop;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import org.pytorch.IValue;
import org.pytorch.Module;
import org.pytorch.Tensor;
import org.pytorch.torchvision.TensorImageUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class NeuralNetworkHelper {

    private static Module model;

    // Загрузка модели из папки assets
    public static void loadModel(Context context) {
        try {
            String modelPath = assetFilePath(context, "yolov8x.pt");
            model = Module.load(modelPath);
            Log.i("NeuralNetworkHelper", "Модель успешно загружена: " + modelPath);
        } catch (Exception e) {
            Log.e("NeuralNetworkHelper", "Ошибка загрузки модели", e);
        }
    }

    // Распознавание продуктов
    public static List<String> recognize(Bitmap bitmap) {
        List<String> detectedProducts = new ArrayList<>();
        if (model == null) {
            Log.e("NeuralNetworkHelper", "Модель не загружена. Проверьте вызов loadModel.");
            return detectedProducts;
        }

        try {
            // Преобразуем изображение в тензор
            Tensor inputTensor = TensorImageUtils.bitmapToFloat32Tensor(
                    bitmap,
                    TensorImageUtils.TORCHVISION_NORM_MEAN_RGB,
                    TensorImageUtils.TORCHVISION_NORM_STD_RGB
            );

            // Выполняем инференс
            Tensor outputTensor = model.forward(IValue.from(inputTensor)).toTensor();

            // Обработка выходного тензора
            float[] outputs = outputTensor.getDataAsFloatArray();
            Log.i("NeuralNetworkHelper", "Размер выходного тензора: " + outputs.length);

            for (int i = 0; i < outputs.length; i++) {
                // Пример: если значение больше 0.5, считаем это детекцией
                if (outputs[i] > 0.5) {
                    detectedProducts.add("Продукт #" + i);
                    Log.i("NeuralNetworkHelper", "Обнаружен продукт #" + i + " с вероятностью " + outputs[i]);
                }
            }

        } catch (Exception e) {
            Log.e("NeuralNetworkHelper", "Ошибка обработки изображения", e);
        }

        if (detectedProducts.isEmpty()) {
            Log.w("NeuralNetworkHelper", "Продукты не обнаружены. Возможно, проблема с моделью.");
        }

        return detectedProducts;
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
