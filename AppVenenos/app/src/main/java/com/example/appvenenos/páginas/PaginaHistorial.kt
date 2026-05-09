package com.example.appvenenos.páginas

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.appvenenos.* import retrofit2.Call
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

        val token = SessionManager(requireContext()).fetchAuthToken()
        if (token != null) {
            ConexionApi.instancia.getAnimales("Bearer $token").enqueue(object : Callback<List<Animal>> {
                override fun onResponse(call: Call<List<Animal>>, response: Response<List<Animal>>) {
                    if (response.isSuccessful) {
                        rv.adapter = AnimalAdaptador(response.body() ?: emptyList())
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