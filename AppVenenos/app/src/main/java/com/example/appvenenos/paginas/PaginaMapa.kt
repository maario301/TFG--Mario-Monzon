package com.example.appvenenos.paginas

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.appvenenos.Avistamiento
import com.example.appvenenos.ConexionApi
import com.example.appvenenos.R
import com.example.appvenenos.SessionManager
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PaginaMapa : Fragment() {

    private var mapView: MapView? = null
    private var token: String = ""
    private var isAdmin: Boolean = false
    private var nombreComun: String = ""
    private var nombreFoto: String = ""
    private var usuario: String = ""
    private var fecha: String = ""

    // Mapeamos los marcadores de OpenStreetMap con su ID de base de datos
    private val marcadoresMap = mutableMapOf<Marker, Int>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Cargar obligatoriamente la configuración interna que requiere OpenStreetMap
        Configuration.getInstance().load(
            requireContext(),
            requireContext().getSharedPreferences("osmdroid", Context.MODE_PRIVATE)
        )
        return inflater.inflate(R.layout.pagina_mapa, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        nombreComun = arguments?.getString("nombre") ?: ""
        nombreFoto  = arguments?.getString("cientifico") ?: ""
        fecha       = arguments?.getString("fecha") ?: ""

        val sessionManager = SessionManager(requireContext())
        token = "Bearer ${sessionManager.fetchAuthToken()}"
        val prefs = requireContext().getSharedPreferences("AppVenenos", Context.MODE_PRIVATE)
        usuario = prefs.getString("usuario_nombre", "Usuario") ?: "Usuario"
        isAdmin = prefs.getBoolean("is_admin", false)

        // Inicializamos la vista del mapa de osmdroid
        mapView = view.findViewById(R.id.mapview)
        mapView?.setMultiTouchControls(true) // Permitir zoom con dos dedos

        // Centrar cámara en Madrid por defecto
        val mapController = mapView?.controller
        mapController?.setZoom(12.0)
        val centroDefecto = GeoPoint(40.416775, -3.703790)
        mapController?.setCenter(centroDefecto)

        cargarAvistamientos()

        // Si viene un avistamiento nuevo para registrar
        if (nombreComun.isNotEmpty()) {
            val ubicacion = GeoPoint(40.416775, -3.703790)
            colocarMarcador(ubicacion, nombreComun, nombreFoto, usuario, fecha, -1)
            guardarAvistamiento(nombreFoto, 40.416775, -3.703790)
        }
    }

    private fun cargarAvistamientos() {
        ConexionApi.instancia.getAvistamientos(token)
            .enqueue(object : Callback<List<Avistamiento>> {
                override fun onResponse(call: Call<List<Avistamiento>>, response: Response<List<Avistamiento>>) {
                    if (!isAdded) return
                    if (response.isSuccessful) {
                        response.body()?.forEach { av ->
                            val punto = GeoPoint(av.latitud, av.longitud)
                            val foto = av.nombre_cientifico.lowercase()
                            colocarMarcador(punto, av.nombre_comun, foto, av.usuario, av.fecha, av.id)
                        }
                    }
                }
                override fun onFailure(call: Call<List<Avistamiento>>, t: Throwable) {}
            })
    }

    private fun colocarMarcador(
        posicion: GeoPoint,
        nombre: String,
        nombreFoto: String,
        usuario: String,
        fecha: String,
        id: Int
    ) {
        if (!isAdded || mapView == null) return

        val currentMap = mapView ?: return

        // Crear el marcador nativo de OpenStreetMap
        val marker = Marker(currentMap)
        marker.position = posicion
        marker.title = "⚠️ $nombre"
        marker.subDescription = "👤 $usuario\n📅 $fecha" // En osmdroid usamos subDescription para textos largos

        // Escalar el icono personalizado desde tus drawables
        val resId = resources.getIdentifier(nombreFoto, "drawable", requireContext().packageName)
        if (resId != 0) {
            val original = BitmapFactory.decodeResource(resources, resId)
            val size = (40 * resources.displayMetrics.density).toInt()
            val scaled = Bitmap.createScaledBitmap(original, size, size, true)
            marker.icon = BitmapDrawable(resources, scaled)
        }

        // Configurar la ventana de información al pulsar el marcador
        marker.setOnMarkerClickListener { m, _ ->
            val markerId = marcadoresMap[m] ?: return@setOnMarkerClickListener false

            if (isAdmin && markerId != -1) {
                AlertDialog.Builder(requireContext())
                    .setTitle(m.title)
                    .setMessage("${m.subDescription}\n\n¿Eliminar este avistamiento?")
                    .setPositiveButton("Eliminar") { _, _ ->
                        ConexionApi.instancia.eliminarAvistamiento(token, markerId)
                            .enqueue(object : Callback<Void> {
                                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                                    if (response.isSuccessful) {
                                        activity?.runOnUiThread {
                                            currentMap.overlays.remove(m)
                                            marcadoresMap.remove(m)
                                            currentMap.invalidate() // Fuerza al mapa a refrescarse visualmente
                                        }
                                    }
                                }
                                override fun onFailure(call: Call<Void>, t: Throwable) {}
                            })
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
            } else {
                m.showInfoWindow()
            }
            true
        }

        // Añadir el marcador a la lista de capas del mapa
        currentMap.overlays.add(marker)

        if (id != -1) {
            marcadoresMap[marker] = id
        }

        currentMap.invalidate() // Refrescar el mapa para pintar el nuevo marcador
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

    override fun onResume() {
        super.onResume()
        mapView?.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView?.onPause()
    }
}