package com.example.appvenenos.paginas

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.appvenenos.Avistamiento
import com.example.appvenenos.ConexionApi
import com.example.appvenenos.R
import com.example.appvenenos.SessionManager
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.CancellationTokenSource
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PaginaMapa : Fragment(), OnMapReadyCallback {

    private var googleMap: GoogleMap? = null
    private var token: String = ""
    private var isAdmin: Boolean = false
    private var nombreComun: String = ""
    private var nombreFoto: String = ""
    private var usuario: String = ""
    private var fecha: String = ""
    private var origen: String = ""   // "camara" -> GPS real | "galeria"/otros -> manual

    // Vistas para el modo manual
    private var txtInstruccion: TextView? = null
    private var btnConfirmar: Button? = null
    private var marcadorBorrador: Marker? = null

    // Mapeamos los marcadores de Google Maps con su ID de base de datos
    private val marcadoresMap = mutableMapOf<Marker, Int>()

    // Solicitud de permiso de ubicación: solo sirve para centrar el mapa cerca del
    // usuario como comodidad. La colocación del marcador siempre es manual.
    private val permisoUbicacion =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { concedido ->
            if (!isAdded) return@registerForActivityResult
            if (concedido) {
                habilitarMiUbicacion()
                centrarYProponerUbicacion()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Sin ubicación: toca el mapa para colocar el marcador donde quieras",
                    Toast.LENGTH_LONG
                ).show()
                // El modo manual ya está activo, así que el usuario puede tocar el mapa
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.pagina_mapa, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        nombreComun = arguments?.getString("nombre") ?: ""
        nombreFoto  = arguments?.getString("cientifico") ?: ""
        fecha       = arguments?.getString("fecha") ?: ""
        origen      = arguments?.getString("origen") ?: ""

        txtInstruccion = view.findViewById(R.id.txtInstruccion)
        btnConfirmar = view.findViewById(R.id.btnConfirmarUbicacion)

        val sessionManager = SessionManager(requireContext())
        token = "Bearer ${sessionManager.fetchAuthToken()}"
        val prefs = requireContext().getSharedPreferences("AppVenenos", Context.MODE_PRIVATE)
        usuario = prefs.getString("usuario_nombre", "Usuario") ?: "Usuario"
        isAdmin = prefs.getBoolean("is_admin", false)

        // Inicializamos el mapa de Google de forma asíncrona
        val mapFragment = childFragmentManager
            .findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    // Se ejecuta cuando el mapa de Google ya está listo para usarse
    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        // Centrar cámara en Madrid por defecto
        val centroDefecto = LatLng(40.416775, -3.703790)
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(centroDefecto, 12f))

        // Gestionamos nosotros el clic en los marcadores ya existentes
        map.setOnMarkerClickListener { marker -> onMarkerClick(marker) }

        // Si ya tenemos permiso, mostramos el punto azul de "mi ubicación"
        if (tienePermisoUbicacion()) habilitarMiUbicacion()

        cargarAvistamientos()

        // Si viene un avistamiento nuevo para registrar (venga de cámara o de galería),
        // el usuario SIEMPRE coloca el marcador donde quiera tocando el mapa.
        if (nombreComun.isNotEmpty()) {
            activarModoManual()
            // Si tenemos permiso de ubicación, centramos el mapa cerca del usuario y le
            // dejamos un marcador de partida que puede arrastrar o reubicar tocando el mapa.
            if (tienePermisoUbicacion()) {
                centrarYProponerUbicacion()
            } else {
                permisoUbicacion.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    private fun tienePermisoUbicacion(): Boolean =
        ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

    @SuppressLint("MissingPermission")
    private fun habilitarMiUbicacion() {
        googleMap?.isMyLocationEnabled = true
    }

    // Centra el mapa en la ubicación del usuario y deja un marcador de partida editable.
    // No registra nada: solo es un punto de partida cómodo; el usuario decide dónde dejarlo.
    @SuppressLint("MissingPermission")
    private fun centrarYProponerUbicacion() {
        val map = googleMap ?: return
        val cliente = LocationServices.getFusedLocationProviderClient(requireContext())
        val cts = CancellationTokenSource()

        cliente.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token)
            .addOnSuccessListener { loc ->
                if (!isAdded) return@addOnSuccessListener
                val punto = when {
                    loc != null -> LatLng(loc.latitude, loc.longitude)
                    else -> return@addOnSuccessListener
                }
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(punto, 16f))
                // Marcador de partida: se puede arrastrar o reubicar tocando el mapa
                if (marcadorBorrador == null) {
                    marcadorBorrador = map.addMarker(
                        MarkerOptions()
                            .position(punto)
                            .draggable(true)
                            .title("Arrastra o toca el mapa para ajustar")
                    )
                } else {
                    marcadorBorrador?.position = punto
                }
                btnConfirmar?.isEnabled = true
            }
    }

    // Activa el modo manual: el usuario toca el mapa para elegir la ubicación
    private fun activarModoManual() {
        val map = googleMap ?: return

        txtInstruccion?.visibility = View.VISIBLE
        btnConfirmar?.visibility = View.VISIBLE
        btnConfirmar?.isEnabled = false

        map.setOnMapClickListener { latLng ->
            if (marcadorBorrador == null) {
                marcadorBorrador = map.addMarker(
                    MarkerOptions()
                        .position(latLng)
                        .draggable(true)
                        .title("Arrastra para ajustar")
                )
            } else {
                marcadorBorrador?.position = latLng
            }
            btnConfirmar?.isEnabled = true
        }

        btnConfirmar?.setOnClickListener {
            val pos = marcadorBorrador?.position ?: return@setOnClickListener
            // Quitamos el marcador provisional y el modo manual
            marcadorBorrador?.remove()
            marcadorBorrador = null
            map.setOnMapClickListener(null)
            txtInstruccion?.visibility = View.GONE
            btnConfirmar?.visibility = View.GONE

            registrarAvistamientoEn(pos)
        }
    }

    // Coloca el marcador definitivo, centra la cámara y guarda en el backend
    private fun registrarAvistamientoEn(posicion: LatLng) {
        val map = googleMap ?: return
        colocarMarcador(posicion, nombreComun, nombreFoto, usuario, fecha, -1)
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(posicion, 16f))
        guardarAvistamiento(nombreFoto, posicion.latitude, posicion.longitude)
    }

    private fun cargarAvistamientos() {
        ConexionApi.instancia.getAvistamientos(token)
            .enqueue(object : Callback<List<Avistamiento>> {
                override fun onResponse(call: Call<List<Avistamiento>>, response: Response<List<Avistamiento>>) {
                    if (!isAdded) return
                    if (response.isSuccessful) {
                        response.body()?.forEach { av ->
                            val punto = LatLng(av.latitud, av.longitud)
                            val foto = av.nombre_cientifico.lowercase()
                            colocarMarcador(punto, av.nombre_comun, foto, av.usuario, av.fecha, av.id)
                        }
                    }
                }
                override fun onFailure(call: Call<List<Avistamiento>>, t: Throwable) {}
            })
    }

    private fun colocarMarcador(
        posicion: LatLng,
        nombre: String,
        nombreFoto: String,
        usuario: String,
        fecha: String,
        id: Int
    ) {
        if (!isAdded) return
        val map = googleMap ?: return

        val opciones = MarkerOptions()
            .position(posicion)
            .title("⚠️ $nombre")
            .snippet("👤 $usuario\n📅 $fecha")

        // Construimos un pin (chincheta) con la foto del animal recortada dentro
        val resId = resources.getIdentifier(nombreFoto, "drawable", requireContext().packageName)
        if (resId != 0) {
            val original = BitmapFactory.decodeResource(resources, resId)
            if (original != null) {
                opciones.icon(BitmapDescriptorFactory.fromBitmap(crearIconoPin(original)))
            }
        }

        val marker = map.addMarker(opciones) ?: return

        if (id != -1) {
            marcadoresMap[marker] = id
        }
    }

    // Genera un pin de mapa (círculo rojo + punta + borde blanco) con la foto recortada en círculo
    private fun crearIconoPin(foto: Bitmap): Bitmap {
        val d = resources.displayMetrics.density
        val r = 24f * d                 // radio de la cabeza del pin
        val puntaAlto = 16f * d         // alto de la punta inferior
        val anchoAnillo = 4f * d        // grosor del borde blanco
        val width = (2 * r).toInt()
        val height = (2 * r + puntaAlto).toInt()
        val cx = r

        val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)

        val pinPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#D32F2F")   // rojo (peligro)
            style = Paint.Style.FILL
        }

        // Cabeza (círculo) + punta (triángulo) = forma de chincheta
        canvas.drawCircle(cx, r, r, pinPaint)
        val punta = Path().apply {
            moveTo(cx - r, r)
            lineTo(cx + r, r)
            lineTo(cx, height.toFloat())
            close()
        }
        canvas.drawPath(punta, pinPaint)

        // Anillo blanco
        val blancoR = r - anchoAnillo
        val blancoPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            style = Paint.Style.FILL
        }
        canvas.drawCircle(cx, r, blancoR, blancoPaint)

        // Foto del animal recortada en círculo dentro del anillo
        val fotoR = blancoR - 2f * d
        val tam = (2 * fotoR).toInt()
        if (tam > 0) {
            val fotoEscalada = Bitmap.createScaledBitmap(foto, tam, tam, true)
            val save = canvas.save()
            val recorte = Path().apply { addCircle(cx, r, fotoR, Path.Direction.CW) }
            canvas.clipPath(recorte)
            canvas.drawBitmap(fotoEscalada, cx - fotoR, r - fotoR, null)
            canvas.restoreToCount(save)
        }

        return bmp
    }

    // Lógica al pulsar un marcador (devuelve true si consumimos el evento)
    private fun onMarkerClick(marker: Marker): Boolean {
        val markerId = marcadoresMap[marker]

        if (isAdmin && markerId != null && markerId != -1) {
            AlertDialog.Builder(requireContext())
                .setTitle(marker.title)
                .setMessage("${marker.snippet}\n\n¿Eliminar este avistamiento?")
                .setPositiveButton("Eliminar") { _, _ ->
                    ConexionApi.instancia.eliminarAvistamiento(token, markerId)
                        .enqueue(object : Callback<Void> {
                            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                                if (response.isSuccessful) {
                                    activity?.runOnUiThread {
                                        marker.remove()
                                        marcadoresMap.remove(marker)
                                    }
                                }
                            }
                            override fun onFailure(call: Call<Void>, t: Throwable) {}
                        })
                }
                .setNegativeButton("Cancelar", null)
                .show()
            return true
        }

        // Para usuarios normales, mostrar la ventana de información
        marker.showInfoWindow()
        return true
    }

    private fun guardarAvistamiento(nombreFoto: String, lat: Double, lon: Double) {
        val datos = mapOf(
            "nombre_cientifico" to nombreFoto,
            "latitud" to lat.toString(),
            "longitud" to lon.toString()
        )
        ConexionApi.instancia.guardarAvistamiento(token, datos)
            .enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {}
                override fun onFailure(call: Call<Void>, t: Throwable) {}
            })
    }
}
