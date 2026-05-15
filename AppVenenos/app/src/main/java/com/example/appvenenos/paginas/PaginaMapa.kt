package com.example.appvenenos.paginas

import android.content.Context
import android.location.LocationManager
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.appcompat.app.AppCompatActivity
import com.example.appvenenos.R
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

class PaginaMapa : AppCompatActivity() {

    private lateinit var map: MapView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Configuración de OSMDroid
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this))
        setContentView(R.layout.activity_pagina_mapa)

        map = findViewById(R.id.mapview)
        map.setMultiTouchControls(true)

        // 1. Recoger datos del Intent (lo que mandamos desde el adaptador)
        val nombreComun = intent.getStringExtra("nombre") ?: "Animal"
        val nombreCientifico = intent.getStringExtra("cientifico") ?: "Desconocido"

        // 2. Obtener ubicación real del GPS
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val lastLocation = try {
            locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
        } catch (e: SecurityException) {
            null
        }

        // Si tenemos ubicación, vamos allí. Si no, a un punto por defecto (ej. Madrid)
        val puntoAvistamiento = if (lastLocation != null) {
            GeoPoint(lastLocation.latitude, lastLocation.longitude)
        } else {
            GeoPoint(40.4167, -3.7033)
        }

        // 3. Centrar el mapa
        map.controller.setZoom(18.0)
        map.controller.setCenter(puntoAvistamiento)

        // 4. Crear el Marcador con la foto
        val marcador = Marker(map)
        marcador.position = puntoAvistamiento
        marcador.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        marcador.title = nombreComun
        marcador.snippet = "Avistado el: ${java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(java.util.Date())}"

        // Cargar la imagen del animal en el marcador
        val nombreFoto = nombreCientifico.lowercase()
        val resId = resources.getIdentifier(nombreFoto, "drawable", packageName)
        if (resId != 0) {
            marcador.icon = getDrawable(resId)
            // Ajustar el tamaño del icono si sale muy grande
            marcador.image = getDrawable(resId)
        }

        map.overlays.add(marcador)
        map.invalidate() // Refrescar mapa
    }
}