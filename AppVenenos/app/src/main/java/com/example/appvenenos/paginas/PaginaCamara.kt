package com.example.appvenenos.paginas

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.appvenenos.Classifier
import com.example.appvenenos.MainActivity
import com.example.appvenenos.R
import android.content.Context
class PaginaCamara : AppCompatActivity() {

    private lateinit var classifier: Classifier
    private lateinit var imgPreview: ImageView
    private lateinit var txtResultado: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pagina_camara) // Asegúrate de que este es el nombre de tu XML

        // Inicializar la IA
        classifier = Classifier(this)

        // Enlazar con el XML (IDs de tu captura image_2ad91a.png)
        imgPreview = findViewById(R.id.imgPreview)
        txtResultado = findViewById(R.id.txtPrediction)
        val btnGallery: Button = findViewById(R.id.btnGallery)
        val btnCapture: Button = findViewById(R.id.btnCapture)

        btnGallery.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 100)
        }

        btnCapture.setOnClickListener {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(intent, 101)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            var bitmap: Bitmap? = null

            if (requestCode == 100) { // De la Galería
                val uri: Uri? = data?.data
                bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
            } else if (requestCode == 101) { // De la Cámara
                bitmap = data?.extras?.get("data") as Bitmap
            }

            if (bitmap != null) {
                imgPreview.setImageBitmap(bitmap)
                imgPreview.visibility = View.VISIBLE

                // 1. La IA saca el nombre científico (con guion bajo)
                val nombreAnimal = classifier.predict(bitmap)
                txtResultado.text = "Detectado: $nombreAnimal"

                // Dentro de onActivityResult, donde detectas el animal
                if (nombreAnimal != "Otros") {
                    // 1. Guardamos el nombre en las preferencias para que el Fragment lo lea al abrirse
                    val prefs = getSharedPreferences("AppVenenos", Context.MODE_PRIVATE)
                    prefs.edit().putString("ultimo_animal", nombreAnimal).apply()
                    finish() // Esto cierra la cámara y activa el onResume de la MainActivity
                    // 2. Avisamos a la Activity principal que cambie a la pestaña de Historial
                    // Asumiendo que tu Activity principal se llama MainActivity
                    val intent = Intent(this, MainActivity::class.java)
                    intent.putExtra("ir_a_historial", true)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    startActivity(intent)
                }
            }
        }
    }
}