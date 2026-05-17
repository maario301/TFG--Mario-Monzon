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
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.text.SimpleDateFormat
import java.util.*

class PaginaMapa : Fragment() {

    private lateinit var map: MapView
    private lateinit var myLocationOverlay: MyLocationNewOverlay

    @SuppressLint("MissingPermission")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.pagina_mapa, container, false)
        Configuration.getInstance().load(requireContext(), PreferenceManager.getDefaultSharedPreferences(requireContext()))

        map = root.findViewById(R.id.mapview)
        map.setMultiTouchControls(true)
        map.overlays.clear()

        // 1. EL PUNTO AZUL REAL (Tu ubicación en casa)
        myLocationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(requireContext()), map)
        myLocationOverlay.enableMyLocation() // Activa el punto azul
        myLocationOverlay.enableFollowLocation() // Centra el mapa en ti automáticamente
        myLocationOverlay.isDrawAccuracyEnabled = true // Dibuja el círculo de precisión
        map.overlays.add(myLocationOverlay)

        // 2. LÓGICA DEL MARCADOR DE ANIMAL (Si vienes de identificar)
        val nombreComun = arguments?.getString("nombre") ?: ""
        val nombreCientifico = arguments?.getString("cientifico") ?: ""

        if (nombreCientifico.isNotEmpty() && nombreCientifico != "Mapa General") {
            // Esperamos un momento a que el GPS dé la posición o usamos una por defecto rápida
            myLocationOverlay.runOnFirstFix {
                val myLoc = myLocationOverlay.myLocation
                activity?.runOnUiThread {
                    if (myLoc != null) {
                        colocarMarcadorAnimal(myLoc, nombreComun, nombreCientifico)
                    }
                }
            }
        }

        map.controller.setZoom(18.0)
        return root
    }

    private fun colocarMarcadorAnimal(posicion: GeoPoint, nombre: String, cientifico: String) {
        val marcadorBicho = Marker(map)
        marcadorBicho.position = posicion
        marcadorBicho.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)

        val prefs = requireContext().getSharedPreferences("AppVenenos", Context.MODE_PRIVATE)
        val usuario = prefs.getString("usuario_nombre", "Usuario")

        marcadorBicho.title = "⚠️ $nombre"
        marcadorBicho.snippet = "Avistado por: $usuario\nZona de riesgo detectada."

        // Cargar imagen del bicho
        val nombreFoto = cientifico.lowercase().replace(" ", "_")
        val resId = resources.getIdentifier(nombreFoto, "drawable", requireContext().packageName)

        if (resId != 0) {
            marcadorBicho.icon = resources.getDrawable(resId, null)
        } else {
            marcadorBicho.icon = resources.getDrawable(org.osmdroid.library.R.drawable.marker_default, null)
        }

        map.overlays.add(marcadorBicho)
        marcadorBicho.showInfoWindow()
        map.invalidate()
    }
}