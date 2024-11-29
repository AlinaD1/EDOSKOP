package com.example.edoskop;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import org.tensorflow.lite.Interpreter;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

public class NeuralNetworkHelper {

    private static Interpreter tflite;

    // Загружаем модель нейросети
    public static void loadModel(Context context) {
        try {
            MappedByteBuffer model = loadModelFile(context);
            tflite = new Interpreter(model);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Загружаем модель из ресурсов
    private static MappedByteBuffer loadModelFile(Context context) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(context.getAssets().openFd("edoscope.pt").getFileDescriptor());
        FileChannel fileChannel = fileInputStream.getChannel();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, fileChannel.size());
    }

    // Преобразуем изображение в формат для модели
    public static Bitmap resizeImage(Bitmap originalImage) {
        int size = 224; // Пример размера, который подходит для модели
        return Bitmap.createScaledBitmap(originalImage, size, size, false);
    }

    // Распознаем объекты на изображении
    public static List<String> recognize(Bitmap image) {
        List<String> recognizedItems = new ArrayList<>();

        // Преобразуем Bitmap в ByteBuffer
        ByteBuffer byteBuffer = convertBitmapToByteBuffer(image);

        // Подготовим массив для результатов
        float[][] result = new float[1][NUM_CLASSES]; // Здесь NUM_CLASSES — количество классов в модели

        // Выполнить распознавание
        tflite.run(byteBuffer, result);

        // Обработка результатов
        // Например, результат будет содержать вероятности для каждого класса
        for (int i = 0; i < NUM_CLASSES; i++) {
            if (result[0][i] > THRESHOLD) { // Если вероятность выше порога
                recognizedItems.add(getClassLabel(i));
            }
        }

        return recognizedItems;
    }

    // Преобразование Bitmap в ByteBuffer
    private static ByteBuffer convertBitmapToByteBuffer(Bitmap image) {
        ByteBuffer buffer = ByteBuffer.allocateDirect(4 * 224 * 224 * 3);
        buffer.order(java.nio.ByteOrder.nativeOrder());
        int[] intValues = new int[224 * 224];
        image.getPixels(intValues, 0, 224, 0, 0, 224, 224);

        // Заполняем ByteBuffer значениями пикселей
        for (int i = 0; i < 224; i++) {
            for (int j = 0; j < 224; j++) {
                int pixelValue = intValues[i * 224 + j];
                buffer.putFloat(((pixelValue >> 16) & 0xFF) / 255.0f); // R
                buffer.putFloat(((pixelValue >> 8) & 0xFF) / 255.0f);  // G
                buffer.putFloat((pixelValue & 0xFF) / 255.0f);          // B
            }
        }
        return buffer;
    }

    // Возвращаем метку для класса (к примеру, может быть список меток)
    private static String getClassLabel(int index) {
        String[] labels = {"Apple", "Banana", "Carrot", "Tomato"}; // Пример меток
        return labels[index];
    }

    private static final int NUM_CLASSES = 4; // Число классов в модели
    private static final float THRESHOLD = 0.5f; // Порог вероятности
}



