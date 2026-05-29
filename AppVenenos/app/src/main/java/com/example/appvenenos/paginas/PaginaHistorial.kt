package com.example.appvenenos.paginas

import android.content.Context // Importante añadir esto
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.appvenenos.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PaginaHistorial : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val vista = inflater.inflate(R.layout.pagina_historial, container, false)

        val rv = vista.findViewById<RecyclerView>(R.id.rvAnimales)
        rv.layoutManager = LinearLayoutManager(requireContext())

        // 1. Miramos si la cámara guardó algún animal recientemente
        val prefs = requireContext().getSharedPreferences("AppVenenos", Context.MODE_PRIVATE)
        val animalBuscado = prefs.getString("ultimo_animal", null)
        val origenDeteccion = prefs.getString("origen_deteccion", null)

        val token = SessionManager(requireContext()).fetchAuthToken()
        if (token != null) {
            ConexionApi.instancia.getAnimales("Bearer $token").enqueue(object : Callback<List<Animal>> {
                override fun onResponse(call: Call<List<Animal>>, response: Response<List<Animal>>) {
                    if (response.isSuccessful) {
                        val listaCompleta = response.body() ?: emptyList()

                        // 2. Si venimos de la cámara y hay un nombre, filtramos la lista
                        if (animalBuscado != null) {
                            val listaFiltrada = listaCompleta.filter { it.nombre_cientifico == animalBuscado }
                            rv.adapter = AnimalAdaptador(listaFiltrada, origenDeteccion)

                            // Limpiamos las preferencias para que al volver a entrar salga todo el historial
                            prefs.edit().remove("ultimo_animal").remove("origen_deteccion").apply()
                        } else {
                            // Si entramos normal al historial, mostramos todo (origen manual)
                            rv.adapter = AnimalAdaptador(listaCompleta, null)
                        }
                    }
                }
                override fun onFailure(call: Call<List<Animal>>, t: Throwable) {
                    Log.e("API_ERROR", t.message ?: "Error desconocido")
                }
            })
        }
        return vista
    }
}