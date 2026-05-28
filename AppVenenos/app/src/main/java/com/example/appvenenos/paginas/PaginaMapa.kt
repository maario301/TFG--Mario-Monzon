package com.example.appvenenos.paginas

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.annotation.SuppressLint

class PaginaMapa : Fragment(), OnMapReadyCallback {

    private var googleMap: GoogleMap? = null
    private var token: String = ""
    private var isAdmin: Boolean = false
    private var nombreComun: String = ""
    private var nombreFoto: String = ""
    private var usuario: String = ""
    private var fecha: String = ""
    private val marcadoresMap = mutableMapOf<Marker, Int>()

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

        val sessionManager = SessionManager(requireContext())
        token = "Bearer ${sessionManager.fetchAuthToken()}"
        val prefs = requireContext().getSharedPreferences("AppVenenos", Context.MODE_PRIVATE)
        usuario = prefs.getString("usuario_nombre", "Usuario") ?: "Usuario"
        isAdmin = prefs.getBoolean("is_admin", false)

        val mapFragment = childFragmentManager.findFragmentById(R.id.mapview) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        map.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(40.416775, -3.703790), 12f))
        if (androidx.core.app.ActivityCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            map.isMyLocationEnabled = true
        }

        cargarAvistamientos()

        if (nombreComun.isNotEmpty()) {
            val ubicacion = LatLng(40.416775, -3.703790)
            colocarMarcador(ubicacion, nombreComun, nombreFoto, usuario, fecha, -1)
            guardarAvistamiento(nombreFoto, 40.416775, -3.703790)
        }

        map.setOnMarkerClickListener { marker ->
            val id = marcadoresMap[marker] ?: return@setOnMarkerClickListener false
            if (isAdmin && id != -1) {
                AlertDialog.Builder(requireContext())
                    .setTitle(marker.title)
                    .setMessage("${marker.snippet}\n\n¿Eliminar este avistamiento?")
                    .setPositiveButton("Eliminar") { _, _ ->
                        ConexionApi.instancia.eliminarAvistamiento(token, id)
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
            } else {
                marker.showInfoWindow()
            }
            true
        }
    }

    private fun cargarAvistamientos() {
        ConexionApi.instancia.getAvistamientos(token)
            .enqueue(object : Callback<List<Avistamiento>> {
                override fun onResponse(call: Call<List<Avistamiento>>, response: Response<List<Avistamiento>>) {
                    if (!isAdded) return  // ← añade esto
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
        if (!isAdded || googleMap == null) return  // ← añade esto

        val map = googleMap ?: return

        val resId = resources.getIdentifier(nombreFoto, "drawable", requireContext().packageName)
        val icon = if (resId != 0) {
            val original = BitmapFactory.decodeResource(resources, resId)
            val size = (40 * resources.displayMetrics.density).toInt()
            val scaled = Bitmap.createScaledBitmap(original, size, size, true)
            BitmapDescriptorFactory.fromBitmap(scaled)
        } else {
            BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
        }

        val marker = map.addMarker(
            MarkerOptions()
                .position(posicion)
                .title("⚠️ $nombre")
                .snippet("👤 $usuario\n📅 $fecha")
                .icon(icon)
        )

        if (marker != null && id != -1) {
            marcadoresMap[marker] = id
        }
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