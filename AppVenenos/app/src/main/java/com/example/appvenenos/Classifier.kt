package com.example.appvenenos // Asegúrate de que coincida con tu paquete real

import android.content.Context
import android.graphics.Bitmap
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.task.vision.classifier.ImageClassifier
import org.tensorflow.lite.task.vision.classifier.ImageClassifier.ImageClassifierOptions

class Classifier(context: Context) {
    private var imageClassifier: ImageClassifier? = null

    init {
        try {
            // Configuramos las opciones para que solo nos dé el mejor resultado
            val options = ImageClassifierOptions.builder()
                .setMaxResults(1)
                .build()

            // Cargamos el modelo que tienes en assets
            imageClassifier = ImageClassifier.createFromFileAndOptions(
                context,
                "model_unquant.tflite",
                options
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun predict(bitmap: Bitmap): String {
        // Convertimos el Bitmap al formato que requiere TFLite
        val image = TensorImage.fromBitmap(bitmap)

        // Realizamos la clasificación
        val results = imageClassifier?.classify(image)

        // Si hay resultados, devolvemos la etiqueta del primero (el más probable)
        return if (!results.isNullOrEmpty() && results[0].categories.isNotEmpty()) {
            results[0].categories[0].label // Ejemplo: "Vipera_latastei"
        } else {
            "Otros"
        }
    }
}