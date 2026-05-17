package com.example.appvenenos.paginas

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.appvenenos.R
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

class PaginaMapa : Fragment() {

    private lateinit var map: MapView
    private lateinit var myLocationOverlay: MyLocationNewOverlay

    @SuppressLint("MissingPermission")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.pagina_mapa, container, false)

        // INIT OSMDROID
        Configuration.getInstance().userAgentValue = requireContext().packageName
        Configuration.getInstance().load(
            requireContext(),
            PreferenceManager.getDefaultSharedPreferences(requireContext())
        )

        map = root.findViewById(R.id.mapview)
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setMultiTouchControls(true)
        map.overlays.clear()

        // Recoger datos del bundle
        val nombreComun   = arguments?.getString("nombre")    ?: ""
        val nombreFoto    = arguments?.getString("cientifico") ?: ""
        val fecha         = arguments?.getString("fecha")      ?: ""

        // Overlay de Mi Ubicación
        myLocationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(requireContext()), map)
        myLocationOverlay.enableMyLocation()
        myLocationOverlay.isDrawAccuracyEnabled = true
        map.overlays.add(myLocationOverlay)

        // ZOOM inicial — centrar en España para no quedarse en (0,0)
        map.controller.setZoom(6.0)
        map.controller.setCenter(GeoPoint(40.416775, -3.703790)) // Madrid

        if (nombreComun.isNotEmpty()) {
            // Intentar colocar marcador en cuanto haya fix GPS
            myLocationOverlay.runOnFirstFix {
                val myLoc = myLocationOverlay.myLocation
                activity?.runOnUiThread {
                    if (myLoc != null) {
                        colocarMarcadorAnimal(myLoc, nombreComun, nombreFoto, fecha)
                        // Centrar el mapa en la ubicación real una vez obtenida
                        map.controller.setZoom(18.0)
                        map.controller.animateTo(myLoc)
                    }
                }
            }
        }

        return root
    }

    private fun colocarMarcadorAnimal(
        posicion: GeoPoint,
        nombre: String,
        nombreFoto: String,
        fecha: String
    ) {
        val marcador = Marker(map)
        marcador.position = posicion
        marcador.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)

        // Usuario desde SharedPreferences
        val prefs = requireContext().getSharedPreferences("AppVenenos", Context.MODE_PRIVATE)
        val usuario = prefs.getString("usuario_nombre", "Usuario") ?: "Usuario"

        marcador.title   = "⚠️ $nombre"
        // AHORA SE VE: usuario + fecha
        marcador.snippet = "👤 $usuario\n📅 $fecha"

        // Icono: el nombre ya viene formateado con guiones bajos desde el adaptador
        val resId = resources.getIdentifier(nombreFoto, "drawable", requireContext().packageName)
        marcador.icon = if (resId != 0) {
            resources.getDrawable(resId, null)
        } else {
            resources.getDrawable(org.osmdroid.library.R.drawable.marker_default, null)
        }

        map.overlays.add(marcador)
        marcador.showInfoWindow()
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