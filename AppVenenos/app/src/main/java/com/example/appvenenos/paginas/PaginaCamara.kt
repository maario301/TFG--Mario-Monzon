package com.example.appvenenos.paginas

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
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
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.appvenenos.Classifier
import com.example.appvenenos.MainActivity
import com.example.appvenenos.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class PaginaCamara : Fragment() {

    private lateinit var classifier: Classifier
    private lateinit var imgPreview: ImageView
    private lateinit var txtResultado: TextView

    // Si true, al conceder el permiso se abre la cámara; si false, solo se pidió al entrar
    private var abrirCamaraTrasPermiso = false

    // Solicitud del permiso de cámara en tiempo de ejecución
    private val permisoCamara =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { concedido ->
            if (concedido) {
                if (abrirCamaraTrasPermiso) lanzarCamara()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Necesitas conceder el permiso de cámara para analizar una foto",
                    Toast.LENGTH_LONG
                ).show()
            }
            abrirCamaraTrasPermiso = false
        }

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
            // Como el manifest declara el permiso CAMERA, Android exige concederlo
            // en runtime antes de abrir la cámara (si no, SecurityException).
            if (tienePermisoCamara()) {
                lanzarCamara()
            } else {
                abrirCamaraTrasPermiso = true
                permisoCamara.launch(Manifest.permission.CAMERA)
            }
        }

        // Pedimos el permiso de cámara nada más entrar en esta página
        if (!tienePermisoCamara()) {
            abrirCamaraTrasPermiso = false
            permisoCamara.launch(Manifest.permission.CAMERA)
        }

        return root
    }

    private fun tienePermisoCamara(): Boolean =
        ContextCompat.checkSelfPermission(
            requireContext(), Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

    private fun lanzarCamara() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, 101)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            var bitmap: Bitmap? = null

            if (requestCode == 100) { // Galería
                val uri: Uri? = data?.data
                bitmap = MediaStore.Images.Media.getBitmap(requireContext().contentResolver, uri)
            } else if (requestCode == 101) { // Cámara
                bitmap = data?.extras?.get("data") as? Bitmap
            }

            if (bitmap != null) {
                imgPreview.setImageBitmap(bitmap)
                imgPreview.visibility = View.VISIBLE

                val nombreAnimal = classifier.predict(bitmap)
                txtResultado.text = "Detectado: $nombreAnimal"

                if (nombreAnimal != "Otros") {
                    // 1. Guardamos el nombre y el origen (cámara/galería) para que el flujo lo sepa
                    val origen = if (requestCode == 100) "galeria" else "camara"
                    val prefs = requireContext().getSharedPreferences("AppVenenos", Context.MODE_PRIVATE)
                    prefs.edit()
                        .putString("ultimo_animal", nombreAnimal)
                        .putString("origen_deteccion", origen)
                        .apply()

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