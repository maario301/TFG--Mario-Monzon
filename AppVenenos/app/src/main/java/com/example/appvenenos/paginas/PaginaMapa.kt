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

        // Cargar configuración de OSMDroid
        Configuration.getInstance().load(requireContext(), PreferenceManager.getDefaultSharedPreferences(requireContext()))

        map = root.findViewById(R.id.mapview)
        map.setMultiTouchControls(true)
        map.overlays.clear() // Empezamos de cero cada vez que entramos

        // 1. Obtener datos del Bundle
        val nombreComun = arguments?.getString("nombre") ?: ""
        val nombreCientifico = arguments?.getString("cientifico") ?: ""

        // 2. Obtener Ubicación Real
        val locationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val lastLocation = try {
            locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
        } catch (e: SecurityException) { null }

        val puntoActual = if (lastLocation != null) {
            GeoPoint(lastLocation.latitude, lastLocation.longitude)
        } else {
            GeoPoint(40.4167, -3.7033) // Madrid por defecto
        }

        // 3. Configurar Vista inicial
        map.controller.setZoom(17.0)
        map.controller.setCenter(puntoActual)

        // 4. LÓGICA DEL MARCADOR (Aquí está el arreglo)
        // Solo ponemos marcador si venimos de identificar algo
        if (nombreCientifico.isNotEmpty() && nombreCientifico != "Mapa General") {

            val marcadorBicho = Marker(map)
            marcadorBicho.position = puntoActual
            marcadorBicho.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)

            // Título y detalles
            val prefs = requireContext().getSharedPreferences("AppVenenos", Context.MODE_PRIVATE)
            val usuarioLogueado = prefs.getString("usuario_nombre", "Usuario")
            val fechaActual = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())

            marcadorBicho.title = "⚠️ $nombreComun"
            marcadorBicho.snippet = "Avistado por: $usuarioLogueado\nFecha: $fechaActual\nZona de riesgo detectada."

            // Intentar poner la foto del animal como icono
            val nombreFoto = nombreCientifico.lowercase().replace(" ", "_")
            val resId = resources.getIdentifier(nombreFoto, "drawable", requireContext().packageName)

            if (resId != 0) {
                // Si la foto existe, la ponemos (Opcional: puedes usar un marcador estándar si la foto es muy grande)
                marcadorBicho.icon = resources.getDrawable(resId, null)
            } else {
                // Icono por defecto de OSMDroid si no hay foto
                marcadorBicho.icon = resources.getDrawable(org.osmdroid.library.R.drawable.marker_default, null)
            }

            map.overlays.add(marcadorBicho)

            // Forzar que el cartelito (InfoWindow) se abra solo al cargar
            marcadorBicho.showInfoWindow()
        } else {
            // Si es "Mapa General", ponemos un marcador de "Estás aquí"
            val marcadorYo = Marker(map)
            marcadorYo.position = puntoActual
            marcadorYo.title = "Estás aquí"
            marcadorYo.icon = resources.getDrawable(org.osmdroid.library.R.drawable.person, null)
            map.overlays.add(marcadorYo)
        }

        map.invalidate() // Refrescar mapa
        return root
    }
}