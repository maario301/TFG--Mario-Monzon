package com.example.appvenenos // Asegúrate de que coincida con tu paquete real

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import org.tensorflow.lite.Interpreter
import java.io.BufferedReader
import java.io.FileInputStream
import java.io.InputStreamReader
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

/**
 * Resultado de una clasificación: la etiqueta ganadora y su confianza (0..1).
 */
data class Prediccion(val etiqueta: String, val confianza: Float)

/**
 * Clasificador de animales venenosos basado en un modelo de Teachable Machine
 * (model_unquant.tflite). Usa el Interpreter crudo de TFLite porque el modelo NO
 * trae metadatos embebidos, así que aplicamos a mano el mismo preprocesado que
 * usa Teachable Machine: redimensionar a la entrada del modelo y normalizar a [-1, 1].
 */
class Classifier(context: Context) {

    private var interpreter: Interpreter? = null
    private val etiquetas: List<String> = cargarEtiquetas(context)

    // Dimensiones de la entrada del modelo (se leen del propio modelo en runtime)
    private var anchoEntrada = 224
    private var altoEntrada = 224
    private var numClases = 0

    // Si la mejor probabilidad no supera este umbral, lo damos por "Otros".
    // Así la categoría comodín funciona de verdad en vez de aceptar cualquier cosa.
    private val umbralConfianza = 0.70f

    init {
        try {
            val modelo = cargarModelo(context, "model_unquant.tflite")
            val opciones = Interpreter.Options().apply { setNumThreads(4) }
            interpreter = Interpreter(modelo, opciones)

            // Leemos la forma real de la entrada/salida del modelo
            interpreter?.let { itp ->
                val formaEntrada = itp.getInputTensor(0).shape() // [1, alto, ancho, 3]
                if (formaEntrada.size == 4) {
                    altoEntrada = formaEntrada[1]
                    anchoEntrada = formaEntrada[2]
                }
                val formaSalida = itp.getOutputTensor(0).shape() // [1, numClases]
                numClases = formaSalida[formaSalida.size - 1]
            }

            Log.d(TAG, "Modelo cargado. Entrada=${anchoEntrada}x${altoEntrada}, " +
                    "clases modelo=$numClases, etiquetas=${etiquetas.size}")

            if (numClases != etiquetas.size) {
                Log.w(TAG, "¡OJO! El modelo tiene $numClases salidas pero labels.txt " +
                        "tiene ${etiquetas.size} líneas. Deben coincidir y estar en el mismo orden.")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error cargando el modelo TFLite", e)
            interpreter = null
        }
    }

    /**
     * Clasifica un bitmap y devuelve la etiqueta más probable.
     * Aplica el umbral de confianza: por debajo del umbral devuelve "Otros".
     */
    fun predict(bitmap: Bitmap): String = predecirConDetalle(bitmap).etiqueta

    /**
     * Igual que predict() pero devuelve también la confianza, y registra en el Log
     * las 3 mejores predicciones para poder depurar qué está "viendo" el modelo.
     */
    fun predecirConDetalle(bitmap: Bitmap): Prediccion {
        val itp = interpreter ?: run {
            Log.e(TAG, "El interpreter es null (el modelo no cargó). Devuelvo 'Otros'.")
            return Prediccion("Otros", 0f)
        }

        val entrada = prepararEntrada(bitmap)
        val salida = Array(1) { FloatArray(numClases) }
        itp.run(entrada, salida)

        val probabilidades = salida[0]

        // Top-3 para los logs (depuración)
        val ranking = probabilidades
            .mapIndexed { i, p -> i to p }
            .sortedByDescending { it.second }

        val top3 = ranking.take(3).joinToString(", ") { (i, p) ->
            val nombre = etiquetas.getOrElse(i) { "clase_$i" }
            "$nombre=${"%.1f".format(p * 100)}%"
        }
        Log.d(TAG, "Top-3: $top3")

        val (mejorIdx, mejorProb) = ranking.first()
        val mejorEtiqueta = etiquetas.getOrElse(mejorIdx) { "Otros" }

        return if (mejorProb >= umbralConfianza && mejorEtiqueta != "Otros") {
            Prediccion(mejorEtiqueta, mejorProb)
        } else {
            Log.d(TAG, "Por debajo del umbral ($umbralConfianza) o clase Otros -> 'Otros'")
            Prediccion("Otros", mejorProb)
        }
    }

    /**
     * Redimensiona el bitmap a la entrada del modelo y lo normaliza a [-1, 1],
     * que es exactamente lo que espera un modelo de Teachable Machine.
     */
    private fun prepararEntrada(bitmap: Bitmap): ByteBuffer {
        val redimensionado = Bitmap.createScaledBitmap(bitmap, anchoEntrada, altoEntrada, true)

        val buffer = ByteBuffer.allocateDirect(4 * anchoEntrada * altoEntrada * 3)
        buffer.order(ByteOrder.nativeOrder())

        val pixeles = IntArray(anchoEntrada * altoEntrada)
        redimensionado.getPixels(pixeles, 0, anchoEntrada, 0, 0, anchoEntrada, altoEntrada)

        for (pixel in pixeles) {
            val r = (pixel shr 16 and 0xFF)
            val g = (pixel shr 8 and 0xFF)
            val b = (pixel and 0xFF)
            // Normalización Teachable Machine: (valor / 127.5) - 1  -> rango [-1, 1]
            buffer.putFloat(r / 127.5f - 1f)
            buffer.putFloat(g / 127.5f - 1f)
            buffer.putFloat(b / 127.5f - 1f)
        }
        buffer.rewind()
        return buffer
    }

    /** Carga el .tflite de assets como buffer mapeado en memoria. */
    private fun cargarModelo(context: Context, nombre: String): MappedByteBuffer {
        val fd = context.assets.openFd(nombre)
        FileInputStream(fd.fileDescriptor).use { fis ->
            val canal = fis.channel
            return canal.map(FileChannel.MapMode.READ_ONLY, fd.startOffset, fd.declaredLength)
        }
    }

    /**
     * Lee labels.txt de assets. Soporta tanto el formato simple ("Naja_naja")
     * como el de Teachable Machine con índice delante ("0 Naja_naja").
     */
    private fun cargarEtiquetas(context: Context): List<String> {
        val lista = mutableListOf<String>()
        try {
            BufferedReader(InputStreamReader(context.assets.open("labels.txt"))).useLines { lineas ->
                lineas.forEach { linea ->
                    val limpia = linea.trim()
                    if (limpia.isNotEmpty()) {
                        // Si viene como "0 Naja_naja" quitamos el índice inicial
                        val sinIndice = limpia.replaceFirst(Regex("^\\d+\\s+"), "")
                        lista.add(sinIndice)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "No se pudo leer labels.txt", e)
        }
        return lista
    }

    companion object {
        private const val TAG = "ClassifierVenoMap"
    }
}
