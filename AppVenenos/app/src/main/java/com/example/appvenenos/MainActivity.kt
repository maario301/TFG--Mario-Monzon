package com.example.appvenenos

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val rvAnimales = findViewById<RecyclerView>(R.id.rvAnimales)
        // Configuración obligatoria del RecyclerView
        rvAnimales.layoutManager = LinearLayoutManager(this)

        ConexionApi.instancia.obtenerAnimales().enqueue(object : Callback<List<Animal>> {
            override fun onResponse(call: Call<List<Animal>>, response: Response<List<Animal>>) {
                if (response.isSuccessful) {
                    val animales = response.body()
                    if (animales != null && animales.isNotEmpty()) {
                        rvAnimales.adapter = AnimalAdaptador(animales)
                        Toast.makeText(this@MainActivity, "Cargados: ${animales.size}", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@MainActivity, "Base de datos vacía", Toast.LENGTH_LONG).show()
                    }
                }
            }

            override fun onFailure(call: Call<List<Animal>>, t: Throwable) {
                Log.e("API_ERROR", t.message ?: "Error")
                Toast.makeText(this@MainActivity, "Sin conexión", Toast.LENGTH_LONG).show()
            }
        })
    }
}