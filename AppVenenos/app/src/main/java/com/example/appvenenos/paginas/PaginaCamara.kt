package com.example.appvenenos.paginas

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.core.content.FileProvider
import java.io.File
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

    // Foto actualmente cargada (de cámara o galería) a la espera de ser analizada
    private var bitmapActual: Bitmap? = null
    private var origenActual: String = ""

    // Si true, al conceder el permiso se abre la cámara; si false, solo se pidió al entrar
    private var abrirCamaraTrasPermiso = false

    // Fichero temporal donde la cámara guarda la foto a resolución completa
    private var fotoTempUri: Uri? = null

    // Solicitud del permiso de cámara en tiempo de ejecución
    private val permisoCamara =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { concedido ->
            if (concedido) {
                if (abrirCamaraTrasPermiso) lanzarCamara()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Necesitas conceder el permiso de cámara para hacer una foto",
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
        val btnCamera: Button = root.findViewById(R.id.btnCamera)
        val btnAnalizar: Button = root.findViewById(R.id.btnAnalizar)

        // GALERÍA: solo elige la foto y la muestra (no analiza todavía)
        btnGallery.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 100)
        }

        // CÁMARA: hace la foto y la muestra (no analiza todavía)
        btnCamera.setOnClickListener {
            // El manifest declara el permiso CAMERA, así que Android exige concederlo en runtime
            if (tienePermisoCamara()) {
                lanzarCamara()
            } else {
                abrirCamaraTrasPermiso = true
                permisoCamara.launch(Manifest.permission.CAMERA)
            }
        }

        // ANALIZAR: analiza la foto que esté cargada en ese momento
        btnAnalizar.setOnClickListener { analizar() }

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
        // Creamos un fichero temporal en la caché de la app y le pasamos su Uri a la
        // cámara con EXTRA_OUTPUT. Así obtenemos la foto a resolución completa en vez
        // del thumbnail minúsculo de data.extras["data"], que era inservible para la IA.
        val ctx = requireContext()
        val archivo = File.createTempFile("foto_", ".jpg", ctx.cacheDir)
        val uri = FileProvider.getUriForFile(ctx, "${ctx.packageName}.fileprovider", archivo)
        fotoTempUri = uri

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_OUTPUT, uri)
            addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        }
        startActivityForResult(intent, 101)
    }

    // Solo carga la foto en pantalla; NO la analiza (eso lo hace el botón Analizar)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            var bitmap: Bitmap? = null
            var origen = ""

            if (requestCode == 100) { // Galería
                val uri: Uri? = data?.data
                bitmap = MediaStore.Images.Media.getBitmap(requireContext().contentResolver, uri)
                origen = "galeria"
            } else if (requestCode == 101) { // Cámara
                // Leemos la foto a resolución completa desde el fichero temporal
                fotoTempUri?.let { uri ->
                    requireContext().contentResolver.openInputStream(uri)?.use { stream ->
                        bitmap = BitmapFactory.decodeStream(stream)
                    }
                }
                origen = "camara"
            }

            if (bitmap != null) {
                bitmapActual = bitmap
                origenActual = origen
                imgPreview.setImageBitmap(bitmap)
                imgPreview.visibility = View.VISIBLE
                txtResultado.text = "Foto lista. Pulsa \"Analizar\"."
            }
        }
    }

    // Analiza la foto cargada y, si reconoce un animal, lleva al historial
    private fun analizar() {
        val bitmap = bitmapActual
        if (bitmap == null) {
            Toast.makeText(
                requireContext(),
                "Primero haz una foto o elige una de la galería",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val resultado = classifier.predecirConDetalle(bitmap)
        val nombreAnimal = resultado.etiqueta
        val porcentaje = (resultado.confianza * 100).toInt()
        txtResultado.text = "Detectado: $nombreAnimal ($porcentaje%)"

        if (nombreAnimal != "Otros") {
            // Guardamos el nombre y el origen (cámara/galería) para el resto del flujo
            val prefs = requireContext().getSharedPreferences("AppVenenos", Context.MODE_PRIVATE)
            prefs.edit()
                .putString("ultimo_animal", nombreAnimal)
                .putString("origen_deteccion", origenActual)
                .apply()

            // Llevamos al usuario al HISTORIAL filtrado por el animal detectado
            (activity as? MainActivity)?.let { main ->
                main.cambiarPagina(PaginaHistorial())
                val navBar = main.findViewById<BottomNavigationView>(R.id.barra_navegacion)
                navBar.selectedItemId = R.id.nav_historial
            }
        } else {
            Toast.makeText(
                requireContext(),
                "No se ha reconocido un animal venenoso (resultado: Otros)",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}
