package com.example.appvenenos.paginas

import android.annotation.SuppressLint
import android.content.Context
import android.location.LocationManager
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
import java.text.SimpleDateFormat
import java.util.*

class PaginaMapa : Fragment() {

    private lateinit var map: MapView

    @SuppressLint("MissingPermission")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.pagina_mapa, container, false)

        Configuration.getInstance().load(requireContext(), PreferenceManager.getDefaultSharedPreferences(requireContext()))

        map = root.findViewById(R.id.mapview)
        map.setMultiTouchControls(true)

        val nombreComun = arguments?.getString("nombre") ?: "Mapa General"
        val nombreCientifico = arguments?.getString("cientifico") ?: ""

        val locationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val lastLocation = try {
            locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
        } catch (e: SecurityException) {
            null
        }

        val puntoAvistamiento = if (lastLocation != null) {
            GeoPoint(lastLocation.latitude, lastLocation.longitude)
        } else {
            GeoPoint(40.4167, -3.7033) // Madrid por defecto
        }

        map.controller.setZoom(18.0)
        map.controller.setCenter(puntoAvistamiento)

        // 5. Crear el Marcador Personalizado
        if (nombreCientifico.isNotEmpty()) {
            val marcador = Marker(map)
            marcador.position = puntoAvistamiento
            marcador.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)

            val prefs = requireContext().getSharedPreferences("AppVenenos", Context.MODE_PRIVATE)
            val usuarioLogueado = prefs.getString("usuario_nombre", "Usuario Anónimo")
            val fechaActual = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())

            marcador.title = nombreComun
            marcador.snippet = "Avistado por: $usuarioLogueado\nFecha: $fechaActual"

            val nombreFoto = nombreCientifico.lowercase()
            val resId = resources.getIdentifier(nombreFoto, "drawable", requireContext().packageName)
            if (resId != 0) {
                val drawableOriginal = requireContext().getDrawable(resId)
                marcador.icon = drawableOriginal
                marcador.image = drawableOriginal
            }

            map.overlays.add(marcador)
        }

        map.invalidate()
        return root // <--- ESTO ES LO QUE TE FALTABA
    }

    override fun onResume() { super.onResume(); map.onResume() }
    override fun onPause() { super.onPause(); map.onPause() }
}