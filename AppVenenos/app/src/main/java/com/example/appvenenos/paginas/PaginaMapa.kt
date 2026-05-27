package com.example.appvenenos.paginas

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.preference.PreferenceManager
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
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.tileprovider.tilesource.XYTileSource
import org.osmdroid.util.BoundingBox
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
        val cartoLight = XYTileSource(
            "CartoDB",
            0, 19, 256, ".png",
            arrayOf(
                "https://a.basemaps.cartocdn.com/light_all/",
                "https://b.basemaps.cartocdn.com/light_all/",
                "https://c.basemaps.cartocdn.com/light_all/"
            )
        )
        map.setTileSource(cartoLight)
        map.setMultiTouchControls(true)
        map.minZoomLevel = 4.0
        map.setScrollableAreaLimitDouble(BoundingBox(71.0, 40.0, 25.0, -25.0))
        map.overlays.clear()

        val nombreComun = arguments?.getString("nombre") ?: ""
        val nombreFoto  = arguments?.getString("cientifico") ?: ""
        val fecha       = arguments?.getString("fecha") ?: ""

        myLocationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(requireContext()), map)
        myLocationOverlay.enableMyLocation()
        myLocationOverlay.isDrawAccuracyEnabled = true
        map.overlays.add(myLocationOverlay)

        map.controller.setZoom(12.0)
        map.controller.setCenter(GeoPoint(40.416775, -3.703790))

        val sessionManager = SessionManager(requireContext())
        val token = "Bearer ${sessionManager.fetchAuthToken()}"
        val prefs = requireContext().getSharedPreferences("AppVenenos", Context.MODE_PRIVATE)
        val usuario = prefs.getString("usuario_nombre", "Usuario") ?: "Usuario"
        val isAdmin = prefs.getBoolean("is_admin", false)

        cargarAvistamientos(token, isAdmin)

        if (nombreComun.isNotEmpty()) {
            val ubicacionPorDefecto = GeoPoint(40.416775, -3.703790)
            colocarMarcador(ubicacionPorDefecto, nombreComun, nombreFoto, usuario, fecha, -1, token, isAdmin)
            map.controller.animateTo(ubicacionPorDefecto)

            guardarAvistamiento(token, nombreFoto, 40.416775, -3.703790)

            myLocationOverlay.runOnFirstFix {
                val myLoc = myLocationOverlay.myLocation
                activity?.runOnUiThread {
                    if (myLoc != null) {
                        map.overlays.removeIf { it is Marker }
                        cargarAvistamientos(token, isAdmin)
                        colocarMarcador(myLoc, nombreComun, nombreFoto, usuario, fecha, -1, token, isAdmin)
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

    private fun cargarAvistamientos(token: String, isAdmin: Boolean) {
        ConexionApi.instancia.getAvistamientos(token)
            .enqueue(object : Callback<List<Avistamiento>> {
                override fun onResponse(
                    call: Call<List<Avistamiento>>,
                    response: Response<List<Avistamiento>>
                ) {
                    if (response.isSuccessful) {
                        response.body()?.forEach { av ->
                            val punto = GeoPoint(av.latitud, av.longitud)
                            val foto = av.nombre_cientifico.lowercase()
                            colocarMarcador(punto, av.nombre_comun, foto, av.usuario, av.fecha, av.id, token, isAdmin)
                        }
                        if (::map.isInitialized) map.invalidate()
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
        id: Int,
        token: String,
        isAdmin: Boolean
    ) {
        if (!::map.isInitialized || !map.isAttachedToWindow) return  // ← cambia esto

        val marcador = Marker(map)
        marcador.position = posicion
        marcador.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        marcador.title   = "⚠️ $nombre"
        marcador.snippet = "👤 $usuario\n📅 $fecha"

        val resId = resources.getIdentifier(nombreFoto, "drawable", requireContext().packageName)
        if (resId != 0) {
            val original = BitmapFactory.decodeResource(resources, resId)
            val size = (40 * resources.displayMetrics.density).toInt()
            val scaled = Bitmap.createScaledBitmap(original, size, size, true)
            marcador.icon = BitmapDrawable(resources, scaled)
        } else {
            marcador.icon = resources.getDrawable(org.osmdroid.library.R.drawable.marker_default, null)
        }

        if (isAdmin && id != -1) {
            marcador.setOnMarkerClickListener { m, _ ->
                m.showInfoWindow()
                AlertDialog.Builder(requireContext())
                    .setTitle("⚠️ $nombre")
                    .setMessage("👤 $usuario\n📅 $fecha\n\n¿Eliminar este avistamiento?")
                    .setPositiveButton("Eliminar") { _, _ ->
                        ConexionApi.instancia.eliminarAvistamiento(token, id)
                            .enqueue(object : Callback<Void> {
                                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                                    if (response.isSuccessful) {
                                        activity?.runOnUiThread {
                                            if (::map.isInitialized) {
                                                map.overlays.remove(marcador)
                                                map.invalidate()
                                            }
                                        }
                                    }
                                }
                                override fun onFailure(call: Call<Void>, t: Throwable) {}
                            })
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
                true
            }
        }

        map.overlays.add(marcador)
        map.invalidate()
    }

    override fun onResume() {
        super.onResume()
        if (::map.isInitialized) map.onResume()
    }

    override fun onPause() {
        super.onPause()
        if (::map.isInitialized) map.onPause()
    }
}