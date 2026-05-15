package com.example.appvenenos.paginas

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment // IMPORTANTE: Cambiado a Fragment
import com.example.appvenenos.Classifier
import com.example.appvenenos.MainActivity
import com.example.appvenenos.R

class PaginaCamara : Fragment() { // Hereda de Fragment

    private lateinit var classifier: Classifier
    private lateinit var imgPreview: ImageView
    private lateinit var txtResultado: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflamos el layout
        val root = inflater.inflate(R.layout.pagina_camara, container, false)

        // Inicializar la IA (usamos requireContext())
        classifier = Classifier(requireContext())

        // Enlazar componentes usando 'root'
        imgPreview = root.findViewById(R.id.imgPreview)
        txtResultado = root.findViewById(R.id.txtPrediction)
        val btnGallery: Button = root.findViewById(R.id.btnGallery)
        val btnCapture: Button = root.findViewById(R.id.btnCapture)

        btnGallery.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 100)
        }

        btnCapture.setOnClickListener {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(intent, 101)
        }

        return root
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            var bitmap: Bitmap? = null

            if (requestCode == 100) { // Galería
                val uri: Uri? = data?.data
                bitmap = MediaStore.Images.Media.getBitmap(requireContext().contentResolver, uri)
            } else if (requestCode == 101) { // Cámara
                bitmap = data?.extras?.get("data") as Bitmap
            }

            if (bitmap != null) {
                imgPreview.setImageBitmap(bitmap)
                imgPreview.visibility = View.VISIBLE

                val nombreAnimal = classifier.predict(bitmap)
                txtResultado.text = "Detectado: $nombreAnimal"

                if (nombreAnimal != "Otros") {
                    val prefs = requireContext().getSharedPreferences("AppVenenos", Context.MODE_PRIVATE)
                    prefs.edit().putString("ultimo_animal", nombreAnimal).apply()

                    // Notificar a MainActivity que cambie a la pestaña Historial
                    (activity as? MainActivity)?.let {
                        it.cambiarPagina(PaginaHistorial())
                        // Opcional: marcar icono historial en el navBar
                    }
                }
            }
        }
    }
}