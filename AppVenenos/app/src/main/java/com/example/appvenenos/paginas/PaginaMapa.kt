package com.example.appvenenos.paginas

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.appvenenos.Avistamiento
import com.example.appvenenos.ConexionApi
import com.example.appvenenos.R
import com.example.appvenenos.SessionManager
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PaginaMapa : Fragment() {

    private lateinit var map: MapView
    private lateinit var myLocationOverlay: MyLocationNewOverlay

    @SuppressLint("MissingPermission")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.pagina_mapa, container, false)

        Configuration.getInstance().userAgentValue = requireContext().packageName
        Configuration.getInstance().load(
            requireContext(),
            PreferenceManager.getDefaultSharedPreferences(requireContext())
        )

        map = root.findViewById(R.id.mapview)
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setMultiTouchControls(true)
        map.overlays.clear()

        val nombreComun = arguments?.getString("nombre") ?: ""
        val nombreFoto  = arguments?.getString("cientifico") ?: ""
        val fecha       = arguments?.getString("fecha") ?: ""

        // Overlay ubicación propia
        myLocationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(requireContext()), map)
        myLocationOverlay.enableMyLocation()
        myLocationOverlay.isDrawAccuracyEnabled = true
        map.overlays.add(myLocationOverlay)

        // Centrado inicial en España
        map.controller.setZoom(6.0)
        map.controller.setCenter(GeoPoint(40.416775, -3.703790))

        // ✅ TOKEN CORRECTO desde SessionManager
        val sessionManager = SessionManager(requireContext())
        val token = "Bearer ${sessionManager.fetchAuthToken()}"
        val prefs = requireContext().getSharedPreferences("AppVenenos", Context.MODE_PRIVATE)
        val usuario = prefs.getString("usuario_nombre", "Usuario") ?: "Usuario"

        // 1. Cargar todos los avistamientos del servidor
        cargarAvistamientos(token)

        // 2. Si venimos de la ficha de un animal, guardar y colocar su marcador
        if (nombreComun.isNotEmpty()) {
            val ubicacionPorDefecto = GeoPoint(40.416775, -3.703790)
            colocarMarcador(ubicacionPorDefecto, nombreComun, nombreFoto, usuario, fecha)
            map.controller.setZoom(18.0)
            map.controller.animateTo(ubicacionPorDefecto)

            // Guardar en el servidor con coordenadas por defecto
            guardarAvistamiento(token, nombreFoto, 40.416775, -3.703790)

            // Cuando llegue GPS real, actualizar
            myLocationOverlay.runOnFirstFix {
                val myLoc = myLocationOverlay.myLocation
                activity?.runOnUiThread {
                    if (myLoc != null) {
                        map.overlays.removeIf { it is Marker }
                        cargarAvistamientos(token)
                        colocarMarcador(myLoc, nombreComun, nombreFoto, usuario, fecha)
                        map.controller.setZoom(18.0)
                        map.controller.animateTo(myLoc)
                        guardarAvistamiento(token, nombreFoto, myLoc.latitude, myLoc.longitude)
                    }
                }
            }
        }

        return root
    }

    private fun guardarAvistamiento(token: String, nombreFoto: String, lat: Double, lon: Double) {
        val datos = mapOf(
            "nombre_cientifico" to nombreFoto.replace("_", " "),
            "latitud" to lat.toString(),
            "longitud" to lon.toString()
        )
        ConexionApi.instancia.guardarAvistamiento(token, datos)
            .enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {}
                override fun onFailure(call: Call<Void>, t: Throwable) {}
            })
    }

    private fun cargarAvistamientos(token: String) {
        ConexionApi.instancia.getAvistamientos(token)
            .enqueue(object : Callback<List<Avistamiento>> {
                override fun onResponse(
                    call: Call<List<Avistamiento>>,
                    response: Response<List<Avistamiento>>
                ) {
                    if (response.isSuccessful) {
                        response.body()?.forEach { av ->
                            val punto = GeoPoint(av.latitud, av.longitud)
                            val foto = av.nombre_cientifico.lowercase().replace(" ", "_")
                            colocarMarcador(punto, av.nombre_comun, foto, av.usuario, av.fecha)
                        }
                        map.invalidate()
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
        fecha: String
    ) {
        val marcador = Marker(map)
        marcador.position = posicion
        marcador.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        marcador.title   = "⚠️ $nombre"
        marcador.snippet = "👤 $usuario\n📅 $fecha"

        val resId = resources.getIdentifier(nombreFoto, "drawable", requireContext().packageName)
        marcador.icon = if (resId != 0) {
            resources.getDrawable(resId, null)
        } else {
            resources.getDrawable(org.osmdroid.library.R.drawable.marker_default, null)
        }

        map.overlays.add(marcador)
        map.invalidate()
    }

    override fun onResume() {
        super.onResume()
        map.onResume()
    }

    override fun onPause() {
        super.onPause()
        map.onPause()
    }
}