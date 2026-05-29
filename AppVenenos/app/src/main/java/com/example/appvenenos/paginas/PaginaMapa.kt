package com.example.appvenenos.paginas

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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

    // Solicitud de permiso de ubicación (solo se usa cuando el origen es la cámara)
    private val permisoUbicacion =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { concedido ->
            if (!isAdded) return@registerForActivityResult
            if (concedido) {
                habilitarMiUbicacion()
                obtenerUbicacionYRegistrar()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Sin permiso de ubicación: coloca el marcador manualmente",
                    Toast.LENGTH_LONG
                ).show()
                activarModoManual()
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

        // Si viene un avistamiento nuevo para registrar
        if (nombreComun.isNotEmpty()) {
            if (origen == "camara") {
                // Foto en directo -> usamos la ubicación real del usuario
                if (tienePermisoUbicacion()) {
                    obtenerUbicacionYRegistrar()
                } else {
                    permisoUbicacion.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                }
            } else {
                // Foto de galería (o desconocido) -> el usuario coloca el marcador a mano
                activarModoManual()
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

    // Obtiene la posición GPS actual y registra el avistamiento ahí
    @SuppressLint("MissingPermission")
    private fun obtenerUbicacionYRegistrar() {
        val cliente = LocationServices.getFusedLocationProviderClient(requireContext())
        val cts = CancellationTokenSource()

        cliente.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token)
            .addOnSuccessListener { loc ->
                if (!isAdded) return@addOnSuccessListener
                if (loc != null) {
                    registrarAvistamientoEn(LatLng(loc.latitude, loc.longitude))
                } else {
                    // A veces getCurrentLocation devuelve null: probamos con la última conocida
                    cliente.lastLocation.addOnSuccessListener { ultima ->
                        if (!isAdded) return@addOnSuccessListener
                        if (ultima != null) {
                            registrarAvistamientoEn(LatLng(ultima.latitude, ultima.longitude))
                        } else {
                            Toast.makeText(
                                requireContext(),
                                "No se pudo obtener tu ubicación. Colócala manualmente.",
                                Toast.LENGTH_LONG
                            ).show()
                            activarModoManual()
                        }
                    }
                }
            }
            .addOnFailureListener {
                if (!isAdded) return@addOnFailureListener
                activarModoManual()
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

        // Escalar el icono personalizado desde tus drawables
        val resId = resources.getIdentifier(nombreFoto, "drawable", requireContext().packageName)
        if (resId != 0) {
            val original = BitmapFactory.decodeResource(resources, resId)
            val size = (40 * resources.displayMetrics.density).toInt()
            val scaled = Bitmap.createScaledBitmap(original, size, size, true)
            opciones.icon(BitmapDescriptorFactory.fromBitmap(scaled))
        }

        val marker = map.addMarker(opciones) ?: return

        if (id != -1) {
            marcadoresMap[marker] = id
        }
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
