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
import androidx.fragment.app.Fragment
import com.example.appvenenos.Classifier
import com.example.appvenenos.MainActivity
import com.example.appvenenos.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class PaginaCamara : Fragment() {

    private lateinit var classifier: Classifier
    private lateinit var imgPreview: ImageView
    private lateinit var txtResultado: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.pagina_camara, container, false)

        classifier = Classifier(requireContext())

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
                    // 1. Guardamos el nombre en SharedPreferences para que el Historial lo sepa
                    val prefs = requireContext().getSharedPreferences("AppVenenos", Context.MODE_PRIVATE)
                    prefs.edit().putString("ultimo_animal", nombreAnimal).apply()

                    // 2. IMPORTANTE: Aquí mandamos al usuario al HISTORIAL
                    (activity as? MainActivity)?.let { main ->
                        // Cambiamos al fragmento de Historial
                        main.cambiarPagina(PaginaHistorial())

                        // 3. Sincronizamos el menú inferior para que marque el icono de Historial
                        val navBar = main.findViewById<BottomNavigationView>(R.id.barra_navegacion)
                        navBar.selectedItemId = R.id.nav_historial
                    }
                }
            }
        }
    }
}