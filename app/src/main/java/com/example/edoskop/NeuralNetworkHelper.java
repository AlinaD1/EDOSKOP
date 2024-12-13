//package com.example.edoskop;

//import android.graphics.Bitmap;
//import java.util.ArrayList;
//import java.util.List;

//public class NeuralNetworkHelper {
  //  public static List<String> recognize(Bitmap image) {
 //       List<String> products = new ArrayList<>();

   //     // Здесь вы вызываете YOLO модель, натренированную на распознавание продуктов
        // Пример: products.add("яблоко"); products.add("банан");

    //    products.add("яблоко");
    //    products.add("банан");
    //    return products;
   // }
//}
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

    // Загружаем модель из assets
    public static void loadModel(Context context) {
        try {
            String modelPath = assetFilePath(context, "yolov8x.pt");
            model = Module.load(modelPath);
        } catch (Exception e) {
            Log.e("NeuralNetworkHelper", "Ошибка загрузки модели", e);
        }
    }

    // Основная функция для распознавания продуктов
    public static List<String> recognize(Bitmap bitmap) {
        List<String> detectedProducts = new ArrayList<>();
        if (model == null) {
            Log.e("NeuralNetworkHelper", "Модель не загружена");
            return detectedProducts;
        }

        // Преобразуем изображение в Tensor
        Tensor inputTensor = TensorImageUtils.bitmapToFloat32Tensor(
                bitmap,
                TensorImageUtils.TORCHVISION_NORM_MEAN_RGB,
                TensorImageUtils.TORCHVISION_NORM_STD_RGB
        );

        // Выполняем инференс
        Tensor outputTensor = model.forward(IValue.from(inputTensor)).toTensor();

        // Пример обработки результатов (зависит от вашей модели)
        float[] outputs = outputTensor.getDataAsFloatArray();
        for (int i = 0; i < outputs.length; i++) {
            // Простая проверка, чтобы выбрать только значимые результаты
            if (outputs[i] > 0.5) {
                detectedProducts.add("Продукт #" + i); // Здесь замените на свои метки
            }
        }

        return detectedProducts;
    }

    // Утилита для копирования файла из assets
    private static String assetFilePath(Context context, String assetName) throws Exception {
        File file = new File(context.getFilesDir(), assetName);
        if (!file.exists()) {
            try (InputStream is = context.getAssets().open(assetName);
                 FileOutputStream os = new FileOutputStream(file)) {
                byte[] buffer = new byte[4 * 1024];
                int read;
                while ((read = is.read(buffer)) != -1) {
                    os.write(buffer, 0, read);
                }
                os.flush();
            }
        }
        return file.getAbsolutePath();
    }
}

